package org.sindhipinyin.keyboard

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import org.sindhipinyin.engine.RomanMode
import org.sindhipinyin.keyboard.data.KeyboardRepository
import java.util.concurrent.Executors

/** Native, offline onboarding, practice, dictionary and settings. */
class MainActivity : Activity() {
    private val repository by lazy { KeyboardRepository.get(this) }
    private val io = Executors.newSingleThreadExecutor()
    private lateinit var page: LinearLayout
    private var tab = "Home"
    private var inkColor = Color.BLACK
    private var pageColor = Color.WHITE
    private var accent = Color.rgb(8, 127, 120)
    private var cardColor = Color.WHITE
    private var mutedColor = Color.GRAY
    private var selectedColor = Color.LTGRAY
    private var uiRevision = 0L

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        tab = state?.getString("tab") ?: "Home"
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("tab", tab)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() { io.shutdown(); super.onDestroy() }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun render() {
        uiRevision++
        val dark = repository.preferences.theme == "dark" || (repository.preferences.theme == "system" &&
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
        inkColor = Color.parseColor(if (dark) "#F5F6FA" else "#20242D")
        pageColor = Color.parseColor(if (dark) "#191D25" else "#F3F5F9")
        accent = Color.parseColor(if (dark) "#A9C7FF" else "#2458C5")
        cardColor = Color.parseColor(if (dark) "#272D38" else "#FFFFFF")
        mutedColor = Color.parseColor(if (dark) "#BCC3CF" else "#586273")
        selectedColor = Color.parseColor(if (dark) "#273C5D" else "#DCE8FF")
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = if (dark) window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            else window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(pageColor)
            setPadding(dp(16), dp(12), dp(16), dp(8))
            setOnApplyWindowInsetsListener { view, insets ->
                view.setPadding(dp(16) + insets.systemWindowInsetLeft, dp(12) + insets.systemWindowInsetTop,
                    dp(16) + insets.systemWindowInsetRight, dp(8) + insets.systemWindowInsetBottom)
                insets
            }
        }
        root.addView(label("سنڌي  ·  Sindhi Pinyin", 25, true))
        root.addView(label("Offline. Personal. Naturally Sindhi.", 14).apply { setTextColor(mutedColor) })
        val navigation = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val row = LinearLayout(this)
        listOf("Home", "Guide", "Dictionary", "Settings").forEach { name ->
            row.addView(button(name) { tab = name; render() }.apply {
                isSelected = tab == name
                background = rounded(if (isSelected) selectedColor else cardColor)
                setTextColor(if (isSelected) accent else mutedColor)
                contentDescription = if (isSelected) "$name, selected" else name
            }, LinearLayout.LayoutParams(-2, dp(48)).apply { setMargins(0, dp(4), dp(6), dp(4)) })
        }
        navigation.addView(row)
        root.addView(navigation)
        val scroll = ScrollView(this)
        page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(12), 0, dp(24)) }
        scroll.addView(page)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        when (tab) { "Guide" -> guide(); "Dictionary" -> dictionary(); "Settings" -> settings(); else -> home() }
    }

    private fun label(text: String, size: Int = 16, bold: Boolean = false): TextView = TextView(this).apply {
        this.text = text
        textSize = size.toFloat()
        setTextColor(inkColor)
        setPadding(0, dp(6), 0, dp(8))
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        textDirection = View.TEXT_DIRECTION_FIRST_STRONG
        setLineSpacing(dp(2).toFloat(), 1.08f)
    }

    private fun button(text: String, action: () -> Unit): Button = Button(this).apply {
        this.text = text
        isAllCaps = false
        minHeight = dp(48)
        setTextColor(accent)
        textSize = 15f
        stateListAnimator = null
        setPadding(dp(16), dp(8), dp(16), dp(8))
        background = android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(selectedColor), rounded(cardColor), rounded(Color.WHITE))
        setOnClickListener { action() }
    }

    private fun heading(text: String) { page.addView(label(text, 22, true)) }
    private fun paragraph(text: String) { page.addView(label(text)) }
    private fun action(text: String, click: () -> Unit) { page.addView(button(text, click), LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(4), 0, dp(6)) }) }
    private fun field(hint: String, rtl: Boolean = false): EditText = EditText(this).apply {
        this.hint = hint
        setTextColor(inkColor)
        setHintTextColor(mutedColor)
        background = rounded(cardColor)
        setPadding(dp(16), dp(12), dp(16), dp(12))
        minHeight = dp(56)
        textDirection = if (rtl) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        maxLines = 4
        if (android.os.Build.VERSION.SDK_INT >= 26) importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
    }

    private fun rounded(color: Int) = android.graphics.drawable.GradientDrawable().apply { setColor(color); cornerRadius = dp(14).toFloat() }

    private fun home() {
        heading("Write Sindhi, naturally")
        paragraph("Type Roman Sindhi and choose a Sindhi word in the candidate bar. Everyday and scholarly draft modes work offline.")
        paragraph("Test build 0.5.0-alpha • A refreshed keyboard with clear word choices, familiar key spacing, and balanced light and dark themes. Suggested readings and scholarly mappings need Sindhi expert review.")
        action("1. Enable Sindhi Pinyin Keyboard") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        action("2. Choose your keyboard") { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() }
        paragraph("Android displays its standard keyboard warning. This app has no Internet permission or analytics. Learning is off until you enable it.")
        heading("Try it here")
        paragraph("Try: twan jo nalo sha ahe\nOr: ma Qambar sindh ma rahndo ahyna\nSpace accepts the checked Sindhi suggestion. Tap another candidate to change the spelling. The globe opens Android’s keyboard picker.")
        page.addView(field("Type here with your new keyboard…", true))
        action("Open typing guide") { tab = "Guide"; render() }
    }

    private fun guide() {
        heading("Roman to Sindhi")
        paragraph("1. Choose Roman Sindhi with the Mode key.\n2. Type normally. Sindhi text changes as you type.\n3. Space accepts the checked candidate so you can continue a conversation. Tap another spelling when needed.\n4. Select the Roman candidate to keep English text. Corrections and longer completions need a tap.\n5. Use Mode for direct Sindhi script or English; use 🌐 for another installed keyboard.")
        paragraph("Examples in the draft seed lexicon: sindh → سنڌ, sindhi → سنڌي, kitab → ڪتاب. Sindhi spelling and Roman aliases need native-speaker review.")
        heading("Two Roman modes")
        paragraph("Everyday accepts convenient spellings and offers alternatives. Scholarly draft preserves diacritics and case; it is a strict token scheme, not yet a verified complete ALA–LC implementation. The ā… key opens marked letters. Use | to separate tokens where a digraph is ambiguous.")
        paragraph("Use ?123 for punctuation and digits. In direct script mode, the page key reveals more Sindhi letters; the marks page adds diacritics. Android shapes the letters—never reverse their stored order.")
        action("Show current editable mapping") {
            backgroundTask({ repository.mapping() }) { mapping ->
                val content = TextView(this).apply { text = mapping; textSize = 15f; setPadding(dp(12), dp(12), dp(12), dp(12)); setTextIsSelectable(true) }
                val scroll = ScrollView(this).apply { addView(content) }
                AlertDialog.Builder(this).setTitle("Mapping TSV — draft").setView(scroll).setPositiveButton("Close", null).show()
            }
        }
        heading("Practice without saving")
        paragraph("Type a Roman spelling below. The practice preview uses only the bundled lexicon and current mapping; it never saves words.")
        val practice = field("Try sindhi or kitab…").apply {
            imeOptions = android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }
        page.addView(practice)
        val preview = label("Your Sindhi candidates will appear here.", 21)
        preview.textDirection = View.TEXT_DIRECTION_FIRST_STRONG_RTL
        page.addView(preview)
        val revision = uiRevision
        practice.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString().orEmpty().take(64)
                backgroundTask({ repository.baseCandidates(input, includeLocal = false) }) { candidates ->
                    if (revision == uiRevision && practice.text.toString().take(64) == input) {
                        preview.text = candidates.filter { it.kind != org.sindhipinyin.engine.CandidateKind.RAW }.take(3).joinToString("  •  ") { it.text }
                            .ifEmpty { "Type a Roman Sindhi word." }
                    }
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })
        paragraph("For practice with the full keyboard candidate bar, use the Home screen’s typing field.")
        paragraph("Type one Roman word, then tap candidate 1, 2, 3 or 4 to insert your preferred Sindhi spelling. Swipe the row for more numbered choices, or tap ▾ to expand. Space accepts the checked word. Sentence suggestions stay in a separate row. Try ma for مان and ماءُ. English offers common completions, spelling alternatives and contractions; these require a tap.")
    }

    private fun dictionary() {
        heading("Your local dictionary")
        paragraph("Save names, words or short phrases deliberately. Entries belong to the currently selected Roman mode. Manual entries work even when automatic learning is off. Up to 1,000 aggregate rows are retained.")
        val roman = field("Roman spelling")
        val script = field("Sindhi word or short phrase", true)
        page.addView(roman); page.addView(script)
        action("Save word / phrase") {
            val alias = roman.text.toString(); val output = script.text.toString()
            backgroundTask({ repository.addWord(alias, output) }) { render(); toast("Saved on this phone") }
        }
        action("Refresh entries") { render() }
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        page.addView(container)
        val revision = uiRevision
        backgroundTask({ repository.dictionary() }) { entries ->
            if (revision != uiRevision) return@backgroundTask
            if (entries.isEmpty()) container.addView(label("No saved words yet."))
            entries.forEach { entry ->
                container.addView(label("${entry.roman} → ${entry.text}\n${entry.mode.lowercase()} · ${if (entry.manual) "saved by you" else "learned locally"}"))
                container.addView(button("Remove ${entry.roman}") { backgroundTask({ repository.deleteWord(entry) }) { render() } })
            }
        }
    }

    private fun settings() {
        heading("Make it yours")
        paragraph("Roman mode")
        val modes = RadioGroup(this)
        RomanMode.entries.forEach { mode ->
            modes.addView(RadioButton(this).apply {
                text = if (mode == RomanMode.EVERYDAY) "Everyday spellings" else "Scholarly — strict draft"
                setTextColor(inkColor)
                isChecked = repository.preferences.romanMode == mode
                minHeight = dp(48)
                setOnClickListener { repository.preferences.romanMode = mode }
            })
        }
        page.addView(modes)
        paragraph("Theme")
        val themes = RadioGroup(this)
        listOf("system", "light", "dark").forEach { value ->
            themes.addView(RadioButton(this).apply {
                text = value.replaceFirstChar { it.uppercase() }; setTextColor(inkColor)
                isChecked = repository.preferences.theme == value; minHeight = dp(48)
                setOnClickListener { repository.preferences.theme = value; render() }
            })
        }
        page.addView(themes)
        toggle("Key vibration (respects system settings)", repository.preferences.vibration) { repository.preferences.vibration = it }
        toggle("Key sounds (respects system settings)", repository.preferences.sound) { repository.preferences.sound = it }
        heading("Privacy and learning")
        paragraph("Optional learning stores selected Sindhi words, Roman spellings, usage counts and previous-word pairs only on this phone. This may include names and private words. It does not record raw key events or send data anywhere. Manual words are managed separately.")
        toggle("Learn words locally — optional", repository.preferences.learning) { repository.preferences.learning = it }
        action("Erase learned words and turn learning off") {
            confirm("Erase learned words?", "Manually saved entries stay. Learning will be switched off.") {
                backgroundTask({ repository.clearLearning() }) { render(); toast("Learned words erased") }
            }
        }
        action("Erase all saved and learned words") {
            confirm("Erase your entire local dictionary?", "This removes manual entries and learned entries, and turns learning off.") {
                backgroundTask({ repository.clearDictionary() }) { render(); toast("Local dictionary erased") }
            }
        }
        heading("Editable mapping")
        paragraph("Export a TSV copy, edit it as UTF-8, then import it. Import validates size, columns and tokens before replacing the mapping. The bundled mapping can always be restored. Export contains mappings only, never your learned dictionary.")
        action("Export mapping TSV") { startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "text/tab-separated-values"; putExtra(Intent.EXTRA_TITLE, "sindhi-mapping.tsv") }, EXPORT_MAPPING) }
        action("Import mapping TSV") { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "*/*" }, IMPORT_MAPPING) }
        action("Restore bundled mapping") { backgroundTask({ repository.resetMapping() }) { toast("Bundled mapping restored") } }
        action("Read privacy policy") { AlertDialog.Builder(this).setTitle("Privacy — test build").setMessage(PRIVACY).setPositiveButton("Close", null).show() }
        action("Offline dictionary credits and license") {
            backgroundTask({ assets.open("dictionaries/LGPL.txt").bufferedReader().use { it.readText() } }) { license ->
                val body = TextView(this).apply {
                    text = "Sindhi Spellchecking Dictionary, 2011.01.10, maintainer nizamani. Source: extensions.openoffice.org/en/project/sindhi-spellchecking-dictionary.html\n\nDictionary licensed under GNU LGPL 2.1. Original word list, affix file, description and license are bundled unchanged. Roman matching rules are our draft heuristics; this word list is not a pronunciation dictionary.\n\n$license"
                    setPadding(dp(12), dp(12), dp(12), dp(12)); setTextIsSelectable(true)
                }
                AlertDialog.Builder(this).setTitle("Dictionary attribution").setView(ScrollView(this).apply { addView(body) }).setPositiveButton("Close", null).show()
            }
        }
        paragraph("Free community test build. No ads, analytics, network permission, background jobs, microphone, contacts or overlay permission. Public release awaits language review, device tests and publisher details.")
    }

    private fun toggle(text: String, checked: Boolean, changed: (Boolean) -> Unit) {
        page.addView(Switch(this).apply {
            this.text = text; setTextColor(inkColor); minHeight = dp(56)
            isChecked = checked
            setOnCheckedChangeListener { _, value -> changed(value) }
        })
    }

    private fun confirm(title: String, message: String, action: () -> Unit) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setNegativeButton("Cancel", null)
            .setPositiveButton("Erase") { _, _ -> action() }.show()
    }

    private fun toast(text: String) { Toast.makeText(this, text, Toast.LENGTH_LONG).show() }
    private fun <T> backgroundTask(work: () -> T, done: (T) -> Unit) {
        io.execute {
            try {
                val result = work()
                runOnUiThread { if (!isFinishing && !isDestroyed) done(result) }
            } catch (error: Exception) {
                val message = if (error is IllegalArgumentException) error.message ?: "Invalid data" else "Could not complete this action. Please retry."
                runOnUiThread { if (!isFinishing && !isDestroyed) toast(message) }
            }
        }
    }

    @Deprecated("Platform Activity result API is sufficient for this dependency-light MVP")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        when (requestCode) {
            IMPORT_MAPPING -> backgroundTask({
                val bytes = contentResolver.openInputStream(uri)?.use { input ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(4096)
                    while (true) {
                        val count = input.read(buffer); if (count < 0) break
                        require(output.size() + count <= 131072) { "Mapping must be at most 128 KiB." }
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                } ?: throw IllegalArgumentException("Cannot open this file")
                val decoder = Charsets.UTF_8.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                repository.importMapping(decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString().removePrefix("\uFEFF"))
            }) { toast("Mapping imported. Reopen the keyboard to start a fresh word.") }
            EXPORT_MAPPING -> backgroundTask({
                contentResolver.openOutputStream(uri, "wt")?.use { it.write(repository.mapping().toByteArray(Charsets.UTF_8)) }
                    ?: throw IllegalArgumentException("Cannot write this file")
            }) { toast("Mapping exported") }
        }
    }

    companion object {
        private const val IMPORT_MAPPING = 10
        private const val EXPORT_MAPPING = 11
        private const val PRIVACY = "Sindhi Pinyin Keyboard processes typing on your phone to send the text you choose to the current app. It has no Internet permission, advertising or analytics.\n\nLearning is OFF by default. If enabled, selected words, Roman spellings, frequency counts and previous-word pairs are stored privately on this phone, capped at 1,000 rows. No raw keystroke log or full conversation history is kept. Password and private fields disable suggestions and learning. Host apps control how they use committed text.\n\nYou can inspect/delete entries in Dictionary, erase learned data in Settings, or uninstall the app. Learning is disabled when erasing. Android backup and device transfer are excluded by app rules. Exporting a mapping uses a document provider you choose; it contains no learned words. No dictionary export is implemented.\n\nThis is an unpublished community test build. Publisher identity, contact address and a public policy URL must be added before Google Play release."
    }
}
