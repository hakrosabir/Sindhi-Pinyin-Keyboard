package org.sindhipinyin.keyboard.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.AtomicFile
import org.sindhipinyin.engine.Candidate
import org.sindhipinyin.engine.CandidateEngine
import org.sindhipinyin.engine.LearnedEntry
import org.sindhipinyin.engine.RomanMode
import java.io.File
import java.text.Normalizer
import java.util.Locale

/** The IME invokes DB operations on its worker. No key events or timestamps are stored. */
class KeyboardRepository private constructor(context: Context) {
    private val app = context.applicationContext
    val preferences = KeyboardPreferences(app)
    private val database = DictionaryDatabase(app)
    private val mappingFile = AtomicFile(File(app.filesDir, "mapping.tsv"))
    @Volatile private var engine: CandidateEngine? = null
    private data class Cache(val mode: RomanMode, val revision: Long, val entries: List<LearnedEntry>)
    @Volatile private var localCache: Cache? = null

    private fun asset(name: String): String = app.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }

    @Synchronized fun mapping(): String = if (mappingFile.baseFile.exists()) {
        runCatching { mappingFile.openRead().bufferedReader(Charsets.UTF_8).use { it.readText() } }.getOrElse { asset("mapping.tsv") }
    } else asset("mapping.tsv")

    private fun getEngine(): CandidateEngine {
        engine?.let { return it }
        return initializeEngine()
    }

    @Synchronized private fun initializeEngine(): CandidateEngine {
        engine?.let { return it }
        val lexicon = asset("lexicon.tsv")
        val phrases = asset("phrases.tsv")
        val dictionary = asset("dictionaries/sd.dic")
        val conversations = asset("conversations.tsv")
        return runCatching { CandidateEngine(mapping(), lexicon, phrases, dictionary, conversations) }
            .getOrElse { CandidateEngine(asset("mapping.tsv"), lexicon, phrases, dictionary, conversations) }
            .also { engine = it }
    }

    /** No database access: used to resolve a space pressed before async candidates arrive. */
    fun baseCandidates(roman: String, previous: String = "", includeLocal: Boolean = true): List<Candidate> {
        val mode = preferences.romanMode
        val cached = if (includeLocal) localCache?.takeIf { it.mode == mode && it.revision == preferences.learningRevision }?.entries.orEmpty() else emptyList()
        return getEngine().candidates(roman, mode, previous, cached)
    }

    fun candidates(roman: String, previous: String): List<Candidate> {
        val mode = preferences.romanMode
        val entries = synchronized(this) {
            readEntries(mode, preferences.learning).also { localCache = Cache(mode, preferences.learningRevision, it) }
        }
        return getEngine().candidates(roman, mode, previous, entries)
    }

    @Synchronized fun importMapping(tsv: String) {
        require(tsv.toByteArray(Charsets.UTF_8).size <= 131072) { "Mapping must be at most 128 KiB." }
        CandidateEngine.validateMapping(tsv)?.let { throw IllegalArgumentException(it) }
        val replacement = CandidateEngine(tsv, asset("lexicon.tsv"), asset("phrases.tsv"), asset("dictionaries/sd.dic"), asset("conversations.tsv"))
        val stream = mappingFile.startWrite()
        try {
            stream.write(tsv.toByteArray(Charsets.UTF_8))
            mappingFile.finishWrite(stream)
        } catch (error: Exception) {
            mappingFile.failWrite(stream)
            throw error
        }
        engine = replacement
    }

    @Synchronized fun resetMapping() { mappingFile.delete(); engine = null }

    private fun normalize(roman: String, mode: RomanMode): String {
        val nfc = Normalizer.normalize(roman.trim(), Normalizer.Form.NFC)
        return if (mode == RomanMode.EVERYDAY) nfc.lowercase(Locale.ROOT) else nfc
    }

    @Synchronized fun learn(roman: String, text: String, previous: String,
        expectedRevision: Long = preferences.learningRevision,
        expectedMode: RomanMode = preferences.romanMode) {
        if (!preferences.learning || expectedRevision != preferences.learningRevision || expectedMode != preferences.romanMode || roman.isBlank() || text.isBlank() || roman == text) return
        if (roman.length > 64 || text.length > 128 || previous.length > 64) return
        if (text.none { it in '\u0600'..'\u06ff' }) return
        upsert(roman, text, previous, manual = false)
    }

    @Synchronized fun addWord(roman: String, text: String) {
        require(roman.isNotBlank() && roman.length <= 64) { "Enter a Roman spelling of 1–64 characters." }
        require(text.isNotBlank() && text.length <= 128) { "Enter a word or short phrase of 1–128 characters." }
        require(text.any { it in '\u0600'..'\u06ff' }) { "The output must contain Sindhi script." }
        require((roman + text).none { Character.isISOControl(it) || it in '\u202a'..'\u202e' || it in '\u2066'..'\u2069' }) { "Control characters are not allowed." }
        upsert(roman, Normalizer.normalize(text.trim(), Normalizer.Form.NFC), "", manual = true)
    }

    private fun upsert(roman: String, text: String, previous: String, manual: Boolean) {
        val db = database.writableDatabase
        val mode = preferences.romanMode
        val alias = normalize(roman, mode)
        db.beginTransaction()
        try {
            // Store one aggregate word entry and optionally one previous-word pair.
            val contexts = if (previous.isBlank() || manual) listOf("") else listOf("", previous)
            for (context in contexts) {
                db.execSQL("INSERT OR IGNORE INTO entries(mode,roman,text,context,count,manual) VALUES(?,?,?,?,0,?)",
                    arrayOf<Any>(mode.name, alias, text, context, if (manual) 1 else 0))
                db.execSQL("UPDATE entries SET count=min(count+1,100000), manual=max(manual,?) WHERE mode=? AND roman=? AND text=? AND context=?",
                    arrayOf<Any>(if (manual) 1 else 0, mode.name, alias, text, context))
            }
            db.execSQL("DELETE FROM entries WHERE rowid IN (SELECT rowid FROM entries ORDER BY manual DESC,count DESC,rowid DESC LIMIT -1 OFFSET 1000)")
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        localCache = Cache(mode, preferences.learningRevision, readEntries(mode, preferences.learning))
    }

    private fun readEntries(mode: RomanMode, includeLearned: Boolean): List<LearnedEntry> {
        val result = mutableListOf<LearnedEntry>()
        database.readableDatabase.rawQuery(
            "SELECT roman,text,context,count FROM entries WHERE mode=? AND (?=1 OR manual=1) ORDER BY manual DESC,count DESC LIMIT 1000",
            arrayOf(mode.name, if (includeLearned) "1" else "0")
        ).use { c -> while (c.moveToNext()) result += LearnedEntry(c.getString(0), c.getString(1), c.getString(2), c.getInt(3)) }
        return result
    }

    @Synchronized fun dictionary(): List<DictionaryEntry> {
        val result = mutableListOf<DictionaryEntry>()
        database.readableDatabase.rawQuery("SELECT rowid,mode,roman,text,count,manual FROM entries WHERE context='' ORDER BY manual DESC,count DESC LIMIT 1000", null).use { c ->
            while (c.moveToNext()) result += DictionaryEntry(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getInt(5) == 1)
        }
        return result
    }

    @Synchronized fun deleteWord(entry: DictionaryEntry) {
        database.writableDatabase.delete("entries", "mode=? AND roman=? AND text=?", arrayOf(entry.mode, entry.roman, entry.text))
        localCache = null
    }

    /** Also turns learning off so queued selection work cannot recreate erased data. */
    @Synchronized fun clearLearning() {
        localCache = null
        preferences.learning = false
        database.writableDatabase.delete("entries", "manual=0", null)
        database.writableDatabase.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
    }

    @Synchronized fun clearDictionary() {
        localCache = null
        preferences.learning = false
        database.writableDatabase.delete("entries", null, null)
        database.writableDatabase.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
    }

    private class DictionaryDatabase(context: Context) : SQLiteOpenHelper(context, "dictionary.db", null, 1) {
        override fun onConfigure(db: SQLiteDatabase) { db.execSQL("PRAGMA secure_delete=ON") }
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE entries(mode TEXT NOT NULL,roman TEXT NOT NULL,text TEXT NOT NULL,context TEXT NOT NULL,count INTEGER NOT NULL,manual INTEGER NOT NULL DEFAULT 0,UNIQUE(mode,roman,text,context))")
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // Add explicit migrations before increasing the schema version. Never silently drop user words.
            error("No migration from $oldVersion to $newVersion")
        }
    }

    companion object {
        @Volatile private var instance: KeyboardRepository? = null
        fun get(context: Context): KeyboardRepository = instance ?: synchronized(this) {
            instance ?: KeyboardRepository(context).also { instance = it }
        }
    }
}

data class DictionaryEntry(val id: Long, val mode: String, val roman: String, val text: String, val count: Int, val manual: Boolean)
