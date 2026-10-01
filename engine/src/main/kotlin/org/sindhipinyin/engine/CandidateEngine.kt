package org.sindhipinyin.engine

import java.text.Normalizer
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln

enum class RomanMode { EVERYDAY, SCHOLARLY }
enum class CandidateKind { EXACT, DICTIONARY, TRANSLITERATION, COMPLETION, CORRECTION, NEXT_WORD, PHRASE, RAW }
data class Candidate(val text: String, val kind: CandidateKind, val roman: String = "")
data class LearnedEntry(val roman: String, val text: String, val context: String, val count: Int)

/** Offline, deterministic candidate generation. Does not retain input or perform I/O. */
class CandidateEngine(mappingTsv: String, lexiconTsv: String, phrasesTsv: String, scriptDictionary: String = "", conversationTsv: String = "") {
    private val conversations = ConversationModel(conversationTsv)
    private val phoneticDictionary = PhoneticDictionary(scriptDictionary)
    val dictionarySize: Int get() = phoneticDictionary.size
    private data class Mapping(val roman: String, val text: String, val priority: Int)
    private data class Word(val roman: String, val text: String, val frequency: Int)
    private data class Phrase(val context: String, val roman: String, val text: String, val frequency: Int)
    private data class Scored(val candidate: Candidate, val score: Int)
    private data class Path(val text: String, val penalty: Int)

    private val mappings: Map<RomanMode, Map<Char, List<Mapping>>>
    private val words: Map<RomanMode, List<Word>>
    private val exact: Map<RomanMode, Map<String, List<Word>>>
    private val phrases: List<Phrase>

    init {
        val mappingRows = parseMapping(mappingTsv)
        mappings = RomanMode.entries.associateWith { mode ->
            mappingRows.filter { it[0] == "BOTH" || it[0] == mode.name }
                .map { Mapping(normalize(it[1], mode), nfc(it[2]), it[3].toInt()) }
                .sortedWith(compareByDescending<Mapping> { it.roman.length }.thenByDescending { it.priority })
                .groupBy { it.roman.first() }
        }
        val lexiconRows = rows(lexiconTsv, "mode\troman\ttext\tfrequency\tnote", MAX_WORDS)
        lexiconRows.forEach { fields ->
            require(fields[0] in setOf("BOTH", "EVERYDAY", "SCHOLARLY")) { "Invalid lexicon mode" }
            require(fields[1].isNotBlank() && fields[1].length <= MAX_INPUT && safeText(fields[1])) { "Invalid lexicon romanization" }
            require(validScriptText(fields[2], allowSpace = true) && fields[2].length <= MAX_TEXT) { "Invalid lexicon output" }
            require(fields[3].toIntOrNull() in 0..1000) { "Invalid lexicon frequency" }
        }
        words = RomanMode.entries.associateWith { mode ->
            lexiconRows.filter { it[0] == "BOTH" || it[0] == mode.name }
                .map { Word(normalize(it[1], mode), nfc(it[2]), it[3].toInt()) }
                .sortedByDescending { it.frequency }
        }
        exact = words.mapValues { (_, value) -> value.groupBy { it.roman } }
        phrases = rows(phrasesTsv, "context\troman\ttext\tfrequency\tnote", MAX_PHRASES).map {
            require(it[0].length <= MAX_TEXT && safeText(it[0])) { "Invalid phrase context" }
            require(it[1].length <= MAX_INPUT && safeText(it[1])) { "Invalid phrase romanization" }
            require(validScriptText(it[2], allowSpace = true) && it[2].length <= MAX_TEXT) { "Invalid phrase output" }
            require(it[3].toIntOrNull() in 0..1000) { "Invalid phrase frequency" }
            Phrase(nfc(it[0]), it[1], nfc(it[2]), it[3].toInt())
        }.sortedByDescending { it.frequency }
    }

    /**
     * The literal input is always the last candidate. Selecting RAW must commit its text verbatim.
     * Empty input predicts only from the explicit previous context; callers must clear it on editor
     * changes and suppress learning/prediction in private fields. Learned entries should be scoped
     * to the current mode by the caller. Oversized input gets a literal candidate, never truncation.
     */
    fun candidates(
        roman: String,
        mode: RomanMode,
        previous: String = "",
        learned: List<LearnedEntry> = emptyList(),
    ): List<Candidate> {
        if (roman.length > MAX_INPUT || !safeText(roman)) return listOf(Candidate(roman, CandidateKind.RAW, roman))
        val input = normalize(roman, mode)
        val history = nfc(previous.takeLast(MAX_TEXT)).trim()
        val context = history.substringAfterLast(' ')
        val local = learned.asSequence().take(MAX_LEARNED).filter {
            it.count > 0 && it.roman.length <= MAX_INPUT && it.text.length <= MAX_TEXT && it.context.length <= MAX_TEXT &&
                safeText(it.roman) && validScriptText(it.text, allowSpace = true)
        }.toList()
        val ranked = mutableListOf<Scored>()
        fun add(text: String, kind: CandidateKind, key: String, score: Int) {
            if (text.isNotBlank()) ranked += Scored(Candidate(text, kind, key), score)
        }
        if (input.isEmpty()) {
            // Deliberately do not show personal words without an explicit same-session context.
            if (context.isEmpty()) return emptyList()
            local.filter { nfc(it.context).trim() == context }.forEach {
                add(it.text, predictionKind(it.text), it.roman, 10000 + learnedBoost(it.count))
            }
            phrases.filter { it.context == context }.forEach {
                add(it.text, predictionKind(it.text), it.roman, 7000 + it.frequency)
            }
            if (mode == RomanMode.EVERYDAY) conversations.suggestions("", history, emptySet()).forEachIndexed { index, candidate ->
                add(candidate.text, candidate.kind, candidate.roman, 9000 - index)
            }
            return finish(ranked)
        }
        val modeWords = words.getValue(mode)
        val exactWords = exact.getValue(mode)[input].orEmpty()
        exactWords.forEach { add(it.text, CandidateKind.EXACT, it.roman, 10000 + it.frequency + conversations.contextBoost(history, it.text)) }
        local.filter { normalize(it.roman, mode) == input }.forEach {
            val contextBoost = if (context.isNotEmpty() && nfc(it.context).trim() == context) 400 else 0
            add(it.text, if (' ' in it.text) CandidateKind.PHRASE else CandidateKind.EXACT, it.roman, 11000 + learnedBoost(it.count) + contextBoost)
        }
        if (mode == RomanMode.EVERYDAY) {
            // Context-conditioned exact aliases: e.g. ma after Sindh means in, not I.
            phrases.filter { it.context == context && context.isNotEmpty() && normalize(it.roman, mode) == input && ' ' !in it.text }.forEach {
                add(it.text, CandidateKind.EXACT, it.roman, 13000 + it.frequency)
            }
            phoneticDictionary.matches(input).forEachIndexed { index, text ->
                add(text, CandidateKind.DICTIONARY, roman, 9000 - index * 12 + conversations.contextBoost(history, text))
            }
            conversations.suggestions(input, history, exactWords.map { it.text }.toSet()).forEachIndexed { index, candidate ->
                add(candidate.text, candidate.kind, candidate.roman, 8200 - index)
            }
        }
        modeWords.asSequence().filter { it.roman.startsWith(input) && it.roman != input }.take(8).forEach {
            add(it.text, CandidateKind.COMPLETION, it.roman, 7000 + it.frequency)
        }
        local.asSequence().filter { normalize(it.roman, mode).startsWith(input) && normalize(it.roman, mode) != input }
            .sortedByDescending { it.count }.take(4).forEach {
                add(it.text, CandidateKind.COMPLETION, it.roman, 8000 + learnedBoost(it.count))
            }
        // Draft phrases have everyday spellings only, so they are not used as scholarly matches.
        if (mode == RomanMode.EVERYDAY) phrases.asSequence().filter {
            it.roman.isNotEmpty() && normalize(it.roman, mode).startsWith(input) &&
                (it.context.isEmpty() || it.context == context)
        }.take(4).forEach {
            add(it.text, CandidateKind.PHRASE, it.roman, 6500 + it.frequency)
        }
        transliterate(input, mode).take(4).forEachIndexed { index, text ->
            add(text, CandidateKind.TRANSLITERATION, roman, 5000 - index)
        }
        // Suggestions only: the service must not silently autocorrect. Case/diacritic distinctions
        // are meaningful in scholarly mode; do not offer fuzzy spelling corrections there.
        if (mode == RomanMode.EVERYDAY && input.length >= 3 && exactWords.isEmpty()) {
            modeWords.asSequence().filter { oneEditAway(input, it.roman) }.take(3).forEach {
                add(it.text, CandidateKind.CORRECTION, it.roman, 6000 + it.frequency)
            }
        }
        val result = finish(ranked.filter { it.candidate.text != roman }, MAX_CANDIDATES - 1).toMutableList()
        // Prefix matches must never crowd out the fallback needed to write an unfamiliar word.
        val fallback = ranked.firstOrNull { it.candidate.kind == CandidateKind.TRANSLITERATION && it.candidate.text != roman }?.candidate
        if (fallback != null && result.none { it.text == fallback.text }) {
            if (result.size >= MAX_CANDIDATES - 1) result.removeAt(result.lastIndex)
            result += fallback
        }
        return result + Candidate(roman, CandidateKind.RAW, roman)
    }

    private fun transliterate(input: String, mode: RomanMode): List<String> {
        val beams = Array(input.length + 1) { mutableListOf<Path>() }
        beams[0] += Path("", 0)
        val table = mappings.getValue(mode)
        for (position in input.indices) {
            val states = beams[position].sortedBy { it.penalty }.distinctBy { it.text }.take(BEAM_WIDTH)
            if (states.isEmpty()) continue
            // A vertical bar separates tokens without output, e.g. s|h versus sh.
            val matches = table[input[position]].orEmpty().filter { input.startsWith(it.roman, position) }.take(12)
            for (path in states) {
                if (input[position] == '|') {
                    offer(beams[position + 1], Path(path.text, path.penalty))
                } else if (matches.isEmpty()) {
                    val count = Character.charCount(input.codePointAt(position))
                    offer(beams[position + count], Path(path.text + input.substring(position, position + count), path.penalty + 100))
                } else {
                    for (mapping in matches) {
                        // Prefer longer known tokens; ambiguity is retained by the bounded beam.
                        offer(beams[position + mapping.roman.length], Path(path.text + mapping.text,
                            path.penalty + 100 - mapping.priority))
                    }
                }
            }
        }
        return beams.last().sortedBy { it.penalty }.map { nfc(it.text) }.filter { it.isNotBlank() }
            .distinct().take(BEAM_WIDTH)
    }

    private fun offer(beam: MutableList<Path>, value: Path) {
        val old = beam.indexOfFirst { it.text == value.text }
        if (old >= 0 && beam[old].penalty <= value.penalty) return
        if (old >= 0) beam.removeAt(old)
        beam += value
        if (beam.size > BEAM_WIDTH) {
            val worst = beam.indices.maxByOrNull { beam[it].penalty }!!
            beam.removeAt(worst)
        }
    }

    private fun finish(ranked: List<Scored>, limit: Int = MAX_CANDIDATES): List<Candidate> =
        ranked.sortedByDescending { it.score }.distinctBy { it.candidate.text }.take(limit).map { it.candidate }

    companion object {
        const val MAX_INPUT = 64
        const val MAX_CANDIDATES = 16
        private const val MAX_TEXT = 128
        private const val MAX_WORDS = 10000
        private const val MAX_PHRASES = 2000
        private const val MAX_LEARNED = 512
        private const val BEAM_WIDTH = 12
        private const val MAX_TSV = 2_000_000

        /** Null means valid. The same parser is used by the constructor and mapping import. */
        fun validateMapping(tsv: String): String? = try {
            parseMapping(tsv)
            null
        } catch (error: IllegalArgumentException) {
            error.message ?: "Invalid mapping"
        }

        private fun parseMapping(tsv: String): List<List<String>> {
            val parsed = rows(tsv, "mode\troman\ttext\tpriority\tnote", 1024)
            require(parsed.isNotEmpty()) { "Mapping must contain at least one row" }
            val seen = mutableSetOf<String>()
            parsed.forEachIndexed { index, fields ->
                val label = "Mapping row ${index + 2}"
                require(fields[0] in setOf("BOTH", "EVERYDAY", "SCHOLARLY")) { "$label: mode must be BOTH, EVERYDAY or SCHOLARLY" }
                require(fields[1].isNotEmpty() && fields[1].length <= 12 && fields[1].all {
                    it in 'A'..'Z' || it in 'a'..'z' || it in '\u00c0'..'\u024f' ||
                        it in '\u1e00'..'\u1eff' || it in '\u0300'..'\u036f' || it == '\u02bb' || it == '\''
                } && fields[1].any { Character.isLetter(it) || it == '\'' }) { "$label: invalid Roman token" }
                require(fields[2].length <= 12 && validScriptText(fields[2], allowSpace = false)) { "$label: output must be Arabic-script letters or marks" }
                require(fields[3].toIntOrNull() in 0..99) { "$label: priority must be 0..99" }
                require(fields[4].length <= 240 && safeText(fields[4])) { "$label: invalid note" }
                for (mode in RomanMode.entries.filter { fields[0] == "BOTH" || fields[0] == it.name }) {
                    require(seen.add("${mode.name}\t${normalize(fields[1], mode)}\t${nfc(fields[2])}")) { "$label: duplicate normalized mapping" }
                }
            }
            return parsed
        }

        private fun rows(tsv: String, header: String, maxRows: Int): List<List<String>> {
            require(tsv.length <= MAX_TSV) { "TSV exceeds the 2 MB character limit" }
            val meaningful = tsv.removePrefix("\uFEFF").lineSequence().filter { it.isNotBlank() && !it.startsWith('#') }.toList()
            require(meaningful.firstOrNull() == header) { "Expected TSV header: $header" }
            require(meaningful.size <= maxRows + 1) { "Too many TSV rows; maximum $maxRows" }
            return meaningful.drop(1).mapIndexed { index, line ->
                val fields = line.split('\t')
                require(fields.size == 5) { "TSV row ${index + 2}: expected 5 tab-separated fields" }
                require(fields.all { safeText(it) }) { "TSV row ${index + 2}: control characters are not allowed" }
                fields
            }
        }

        private fun nfc(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFC)
        private fun normalize(value: String, mode: RomanMode): String =
            nfc(if (mode == RomanMode.EVERYDAY) value.lowercase(Locale.ROOT) else value)

        private fun safeText(value: String): Boolean = value.none {
            Character.isISOControl(it) || Character.getType(it) == Character.FORMAT.toInt() ||
                Character.getType(it) == Character.SURROGATE.toInt()
        }

        private fun validScriptText(value: String, allowSpace: Boolean): Boolean =
            value.isNotBlank() && value.all {
                (allowSpace && (it == ' ' || it == '\u06fd' || it == '\u06fe')) || (it in '\u0600'..'\u06ff' &&
                    (Character.isLetter(it) || Character.getType(it) in setOf(Character.NON_SPACING_MARK.toInt(), Character.COMBINING_SPACING_MARK.toInt())))
            }

        private fun predictionKind(text: String): CandidateKind = if (' ' in text) CandidateKind.PHRASE else CandidateKind.NEXT_WORD
        private fun learnedBoost(count: Int): Int = (ln(count.coerceAtMost(1_000_000).toDouble() + 1) * 100).toInt()

        /** Levenshtein distance <= 1, plus adjacent transposition; bounded O(n). */
        private fun oneEditAway(a: String, b: String): Boolean {
            if (a == b || abs(a.length - b.length) > 1) return false
            if (a.length == b.length) {
                val differences = a.indices.filter { a[it] != b[it] }
                return differences.size == 1 || (differences.size == 2 && differences[1] == differences[0] + 1 &&
                    a[differences[0]] == b[differences[1]] && a[differences[1]] == b[differences[0]])
            }
            val short = if (a.length < b.length) a else b
            val long = if (a.length < b.length) b else a
            var index = 0
            while (index < short.length && short[index] == long[index]) index++
            return short.substring(index) == long.substring(index + 1)
        }
    }
}
