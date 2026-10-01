package org.sindhipinyin.keyboard.ime

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import org.sindhipinyin.engine.Candidate
import org.sindhipinyin.engine.CandidateKind

enum class KeyboardMode(val label: String) { ROMAN("Roman Sindhi"), SCRIPT("سنڌي"), ENGLISH("English") }

/** Native widgets preserve TalkBack focus, Android shaping, and system font fallback. */
class KeyboardSurface(context: Context, private val actions: Actions) : LinearLayout(context) {
    interface Actions {
        fun text(text: String)
        fun backspace()
        fun space()
        fun enter()
        fun nextMode()
        fun globe()
        fun settings()
        fun candidate(candidate: Candidate)
    }

    private var mode = KeyboardMode.ROMAN
    private var privateField = false
    private var scholarly = false
    private var symbols = false
    private var extended = false
    private var shifted = false
    private var scriptPage = 0
    private var enterLabel = "Enter"
    private var vibration = false
    private var sound = false
    private var inkColor = Color.BLACK
    private var keyColor = Color.WHITE
    private var surfaceColor = Color.LTGRAY
    private var secondary = Color.DKGRAY
    private var accent = Color.BLUE
    private var controlColor = Color.LTGRAY
    private var selectedColor = Color.LTGRAY
    private var actionColor = Color.BLUE
    private var actionInk = Color.WHITE
    private var borderColor = Color.GRAY
    private val header = LinearLayout(context)
    private val candidateRow = LinearLayout(context)
    private val keys = LinearLayout(context)
    private val status = TextView(context)
    private val preview = TextView(context)
    private val phraseRow = LinearLayout(context)
    private val phraseScroll = HorizontalScrollView(context)
    private lateinit var expandButton: Button
    private var expanded = false
    private var visibleCandidates = emptyList<Candidate>()
    private var automatic: String? = null
    private var lastComposition: String? = null
    private var numberedWords = emptyList<Candidate>()
    private val compact get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    private val rowHeight get() = if (compact) 40 else 54

    init {
        orientation = VERTICAL
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        setPadding(dp(3), dp(4), dp(3), dp(4))
        header.orientation = HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL
        addView(header, LayoutParams(LayoutParams.MATCH_PARENT, dp(if (compact) 40 else 52)))
        phraseRow.orientation = HORIZONTAL
        phraseScroll.apply {
            isHorizontalScrollBarEnabled = false
            addView(phraseRow)
        }
        addView(phraseScroll, LayoutParams(LayoutParams.MATCH_PARENT, dp(if (compact) 36 else 48)))
        val scroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            addView(candidateRow, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
        }
        candidateRow.orientation = HORIZONTAL
        candidateRow.gravity = Gravity.CENTER_VERTICAL
        val choices = LinearLayout(context)
        choices.addView(scroll, LayoutParams(0, dp(60), 1f))
        expandButton = button("▾", "Show more candidates") {
            expanded = !expanded
            renderKeys()
        }
        choices.addView(expandButton, LayoutParams(dp(48), dp(56)))
        addView(choices)
        keys.orientation = VERTICAL
        addView(keys, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        setOnApplyWindowInsetsListener { view, insets ->
            val bottom = if (Build.VERSION.SDK_INT >= 30) insets.getInsets(WindowInsets.Type.navigationBars()).bottom
                else @Suppress("DEPRECATION") insets.systemWindowInsetBottom
            view.setPadding(dp(3), dp(4), dp(3), dp(4) + bottom)
            insets
        }
    }

    fun configure(
        mode: KeyboardMode, privateField: Boolean, numeric: Boolean, scholarly: Boolean,
        enterLabel: String, theme: String, vibration: Boolean, sound: Boolean
    ) {
        this.mode = mode
        this.privateField = privateField
        this.scholarly = scholarly
        this.enterLabel = enterLabel
        this.vibration = vibration
        this.sound = sound
        this.symbols = numeric
        extended = false
        expanded = false
        shifted = false
        scriptPage = 0
        val dark = theme == "dark" || (theme == "system" &&
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
        inkColor = Color.parseColor(if (dark) "#F5F6FA" else "#20242D")
        secondary = Color.parseColor(if (dark) "#BCC3CF" else "#586273")
        keyColor = Color.parseColor(if (dark) "#343943" else "#FFFFFF")
        surfaceColor = Color.parseColor(if (dark) "#191D25" else "#E9EDF3")
        accent = Color.parseColor(if (dark) "#A9C7FF" else "#2458C5")
        controlColor = Color.parseColor(if (dark) "#272D38" else "#D9DFE9")
        selectedColor = Color.parseColor(if (dark) "#273C5D" else "#DCE8FF")
        actionColor = Color.parseColor(if (dark) "#A9C7FF" else "#2458C5")
        actionInk = Color.parseColor(if (dark) "#152C50" else "#FFFFFF")
        borderColor = Color.parseColor(if (dark) "#4B5361" else "#CFD5DF")
        lastComposition = null
        expandButton.setTextColor(inkColor)
        styleButton(expandButton, "toolbar")
        background = GradientDrawable().apply {
            setColor(surfaceColor)
            val radius = dp(18).toFloat()
            cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
        }
        renderHeader()
        renderKeys()
        candidates(emptyList(), "")
        requestApplyInsets()
    }

    fun candidates(items: List<Candidate>, composing: String) {
        if (lastComposition == composing && visibleCandidates == items) return
        lastComposition = composing
        candidateRow.removeAllViews()
        phraseRow.removeAllViews()
        visibleCandidates = items
        automatic = if (composing.isEmpty()) null else CompositionChoice.automatic(items, composing)
        numberedWords = items.filter { it.kind != CandidateKind.PHRASE }.sortedBy { if (it.text == automatic) 0 else 1 }
        val phrases = items.filter { it.kind == CandidateKind.PHRASE }
        phraseScroll.visibility = View.VISIBLE
        if (phrases.isEmpty()) phraseRow.addView(TextView(context).apply {
            text = if (privateField) "Private typing" else if (mode == KeyboardMode.ENGLISH) "English · tap a suggestion" else "Sentence suggestions"
            textSize = 12f
            setTextColor(secondary)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), 0, dp(10), 0)
        }, LayoutParams(LayoutParams.WRAP_CONTENT, dp(if (compact) 36 else 48)))
        phrases.forEach { phraseRow.addView(candidateButton(it), LayoutParams(LayoutParams.WRAP_CONTENT, dp(if (compact) 36 else 48)).apply { setMargins(dp(2), 0, dp(2), 0) }) }
        expandButton.visibility = View.VISIBLE
        expandButton.isEnabled = items.isNotEmpty()
        expandButton.alpha = if (items.isEmpty()) .4f else 1f
        if (items.isEmpty()) {
            val text = TextView(context).apply {
                this.text = when {
                    privateField -> "Suggestions off in this field"
                    mode == KeyboardMode.ENGLISH -> "Type a word to see suggestions"
                    mode == KeyboardMode.SCRIPT -> "سنڌي · direct typing"
                    else -> "Type Roman Sindhi • choose 1, 2, 3…"
                }
                textSize = 13f
                setTextColor(secondary)
                setPadding(dp(10), 0, dp(10), 0)
                gravity = Gravity.CENTER_VERTICAL
            }
            candidateRow.addView(text, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
        } else {
            numberedWords.take(16).forEach { item ->
                val button = candidateButton(item)
                candidateRow.addView(button, LayoutParams((((width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels) - dp(70)) / 4).coerceAtLeast(dp(60)), dp(56)).apply { setMargins(dp(2), 0, dp(2), 0) })
            }
        }
        status.text = when {
            composing.isNotEmpty() -> composing.take(40)
            privateField -> "Private typing"
            mode == KeyboardMode.ROMAN && scholarly -> "Scholarly Sindhi"
            else -> mode.label
        }
        preview.text = automatic ?: ""
        preview.visibility = if (composing.isEmpty()) View.GONE else View.VISIBLE
        if (expanded) renderKeys()
    }

    private fun candidateButton(item: Candidate): Button {
        val number = numberedWords.indexOf(item).takeIf { it >= 0 }?.plus(1)
        val caption = (if (item.text == automatic) "✓ " else "") + item.roman
        return button("", "${number?.let { "Candidate $it, " } ?: "Sentence, "}${item.text}, $caption") {
            expanded = false
            renderKeys()
            actions.candidate(item)
        }.apply {
            textSize = if (item.kind == CandidateKind.PHRASE) 18f else 20f
            val firstLine = if (number != null) "$number · \u2067${item.text}\u2069" else item.text
            val label = if (item.kind == CandidateKind.PHRASE) firstLine else "$firstLine\n\u2066$caption\u2069"
            text = SpannableString(label).apply {
                if (number != null) {
                    setSpan(RelativeSizeSpan(.65f), 0, number.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(ForegroundColorSpan(accent), 0, number.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (label.length > firstLine.length) setSpan(RelativeSizeSpan(.6f), firstLine.length + 1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textDirection = if (number != null) View.TEXT_DIRECTION_LTR else View.TEXT_DIRECTION_FIRST_STRONG_RTL
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(dp(4), 0, dp(4), 0)
            styleButton(this, if (item.text == automatic) "selected" else "candidate")
        }
    }

    fun setAutomaticShift(enabled: Boolean) {
        if (mode == KeyboardMode.ENGLISH && !privateField && shifted != enabled) {
            shifted = enabled
            renderKeys()
        }
    }
    private fun renderHeader() {
        header.removeAllViews()
        status.apply {
            textSize = 13f
            textDirection = View.TEXT_DIRECTION_LTR
            setTextColor(accent)
            maxLines = 1
            setPadding(dp(10), 0, dp(4), 0)
        }
        (status.parent as? android.view.ViewGroup)?.removeView(status)
        (preview.parent as? android.view.ViewGroup)?.removeView(preview)
        val composition = LinearLayout(context).apply { orientation = VERTICAL; gravity = Gravity.CENTER_VERTICAL }
        composition.addView(status, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        preview.apply {
            textSize = 18f
            setTextColor(inkColor)
            maxLines = 1
            textDirection = View.TEXT_DIRECTION_RTL
            gravity = Gravity.END
            setPadding(dp(10), 0, dp(10), 0)
        }
        composition.addView(preview, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        header.addView(composition, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
        status.gravity = Gravity.CENTER_VERTICAL
        if (mode == KeyboardMode.ROMAN) {
            header.addView(button("ā…", "Scholarly letters and diacritics") {
                symbols = false
                extended = !extended
                renderKeys()
            }, LayoutParams(dp(56), dp(48)))
        } else if (mode == KeyboardMode.SCRIPT) {
            header.addView(button("◌َ", "Sindhi marks and additional letters") {
                symbols = false
                extended = !extended
                renderKeys()
            }, LayoutParams(dp(56), dp(48)))
        }
        header.addView(button(when (mode) { KeyboardMode.ROMAN -> "SD"; KeyboardMode.SCRIPT -> "سنڌي"; KeyboardMode.ENGLISH -> "EN" }, "Language: ${mode.label}. Switch language") { actions.nextMode() }.apply {
            textSize = 13f
            styleButton(this, "toolbar")
        }, LayoutParams(dp(48), dp(48)))
        header.addView(button("⚙", "Keyboard settings and typing guide") { actions.settings() }, LayoutParams(dp(52), dp(48)))
    }

    private fun renderKeys() {
        keys.removeAllViews()
        setButtonLabel(expandButton, if (expanded) "▴" else "▾")
        expandButton.contentDescription = if (expanded) "Return to letter keys" else "Show more candidates"
        if (expanded && visibleCandidates.isNotEmpty()) {
            val grid = LinearLayout(context).apply { orientation = VERTICAL }
            visibleCandidates.filter { it.kind == CandidateKind.PHRASE }.forEach {
                grid.addView(candidateButton(it), LayoutParams(LayoutParams.MATCH_PARENT, dp(72)))
            }
            visibleCandidates.filter { it.kind != CandidateKind.PHRASE }.chunked(2).forEach { pair ->
                val row = LinearLayout(context)
                pair.forEach { row.addView(candidateButton(it), LayoutParams(0, dp(64), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }) }
                grid.addView(row)
            }
            keys.addView(ScrollView(context).apply { addView(grid) }, LayoutParams(LayoutParams.MATCH_PARENT, dp(rowHeight * 4)))
            return
        }
        val rows: List<List<String>> = when {
            symbols -> listOf("1234567890".map(Char::toString), listOf("@", "#", "$", "%", "&", "-", "+", "(", ")"), listOf("!", "?", ".", ",", ":", "\"", "'", "/", "⌫"))
            extended && mode == KeyboardMode.SCRIPT -> listOf(
                listOf("آ", "ه", "ئ"), listOf("َ", "ِ", "ُ", "ّ", "ْ", "ٰ"), listOf("⇥", "۔", "،", "؟", "⌫")
            )
            extended -> listOf(
                listOf("ā", "ī", "ū", "ṭ", "ḍ", "ṛ", "ṇ", "ṅ", "ñ"),
                listOf("ḥ", "ṣ", "ẓ", "ẑ", "ẕ", "b̤", "d̤", "g̈", "ʻ"),
                listOf("c", "v", "|", "h", "ʼ", ".", "،", "؟", "⌫")
            )
            mode == KeyboardMode.SCRIPT -> {
                val page = SCRIPT_KEYS.chunked(26)[scriptPage]
                listOf(page.take(9), page.drop(9).take(9), listOf("⇥") + page.drop(18) + listOf("⌫"))
            }
            else -> listOf("qwertyuiop".map(Char::toString), "asdfghjkl".map(Char::toString), listOf("⇧") + "zxcvbnm".map(Char::toString) + listOf("⌫"))
        }
        rows.forEachIndexed { rowIndex, letters ->
            val row = row()
            val qwerty = !symbols && !extended && mode != KeyboardMode.SCRIPT
            if (qwerty && rowIndex == 1) row.addView(View(context), LayoutParams(0, 1, .5f))
            letters.forEach { key ->
                val output = if (shifted && !symbols && mode != KeyboardMode.SCRIPT) key.uppercase() else key
                val display = if (output.first().category == CharCategory.NON_SPACING_MARK) "◌$output" else output
                val description = when (key) { "⌫" -> "Delete"; "⇧" -> "Shift"; "⇥" -> "Next Sindhi letter page"; else -> display }
                val button = button(display, description) {
                    when (key) {
                        "⌫" -> actions.backspace()
                        "⇧" -> { shifted = !shifted; renderKeys() }
                        "⇥" -> { if (extended) extended = false else scriptPage = (scriptPage + 1) % 2; renderKeys() }
                        else -> {
                            actions.text(output)
                            if (shifted) { shifted = false; renderKeys() }
                        }
                    }
                }
                if (key in listOf("⇧", "⌫", "⇥")) styleButton(button, if (key == "⇧" && shifted) "selected" else "control")
                row.addView(button, weighted(if (qwerty && rowIndex == 2 && key in listOf("⇧", "⌫")) 1.5f else 1f))
            }
            if (qwerty && rowIndex == 1) row.addView(View(context), LayoutParams(0, 1, .5f))
        }
        val controls = row()
        controls.addView(button(if (symbols) "ABC" else "?123", "Toggle numbers and symbols") {
            symbols = !symbols
            extended = false
            renderKeys()
        }.apply { styleButton(this, "control") }, weighted(1.25f))
        controls.addView(button("🌐", "Choose any installed keyboard") { actions.globe() }.apply { styleButton(this, "control") }, weighted(1f))
        controls.addView(button(",", "Comma") { actions.text(if (mode == KeyboardMode.ENGLISH) "," else "،") }, weighted(.65f))
        controls.addView(button(if (mode == KeyboardMode.ENGLISH) "English" else "سنڌي", "Space") { actions.space() }.apply { textSize = 15f }, weighted(4f))
        controls.addView(button(".", "Full stop") { actions.text(if (mode == KeyboardMode.ENGLISH) "." else "۔") }, weighted(.65f))
        controls.addView(button(enterLabel, enterLabel) { actions.enter() }.apply { styleButton(this, "primary"); textSize = 14f }, weighted(1.4f))
    }

    private fun row(): LinearLayout = LinearLayout(context).also {
        it.orientation = HORIZONTAL
        it.gravity = Gravity.CENTER
        keys.addView(it, LayoutParams(LayoutParams.MATCH_PARENT, dp(rowHeight)))
    }

    private fun weighted(weight: Float = 1f) = LayoutParams(0, dp(rowHeight - 4), weight).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }

    private fun button(label: String, description: String, onClick: () -> Unit): Button = Button(context).apply {
        setButtonLabel(this, label)
        contentDescription = description
        isAllCaps = false
        textSize = if (label.length > 3) 13f else if (compact) 18f else 20f
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        setTextColor(inkColor)
        minWidth = 0
        minimumWidth = 0
        minHeight = 0
        minimumHeight = 0
        maxLines = 2
        gravity = Gravity.CENTER
        setPadding(0, 0, 0, 0)
        styleButton(this, if (label in listOf("⚙", "ā…", "◌َ")) "toolbar" else "letter")
        isSoundEffectsEnabled = sound
        setOnClickListener {
            if (vibration) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        }
    }

    private fun setButtonLabel(button: Button, label: String) {
        button.text = if (label in setOf("🌐", "⚙", "⌫", "⇧", "▾", "▴")) {
            val icon = KeyboardIcon(label, inkColor).apply { setBounds(0, 0, dp(22), dp(22)) }
            SpannableString("\uFFFC").apply { setSpan(ImageSpan(icon, ImageSpan.ALIGN_BOTTOM), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        } else label
    }

    private fun styleButton(button: Button, role: String) {
        val fill = when (role) {
            "primary" -> actionColor
            "selected" -> selectedColor
            "control" -> controlColor
            "toolbar", "candidate" -> surfaceColor
            else -> keyColor
        }
        val radius = dp(if (role == "primary" || role == "selected") 12 else 9).toFloat()
        button.backgroundTintList = null
        button.background = RippleDrawable(ColorStateList.valueOf(accent and 0x00FFFFFF or 0x26000000), GradientDrawable().apply {
            setColor(fill); cornerRadius = radius
            if (role == "selected") setStroke(dp(1), accent)
            else if (role == "letter") setStroke(dp(1), borderColor)
        }, GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = radius })
        button.setTextColor(if (role == "primary") actionInk else inkColor)
        button.stateListAnimator = null
        button.elevation = if (role == "letter") dp(1).toFloat() else 0f
        button.isSelected = role == "selected"
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + .5f).toInt()

    companion object {
        // Traditional core plus CLDR Sindhi auxiliary keys in the marks row. جھ and گھ are sequences.
        private val SCRIPT_KEYS = "ا ب ٻ ڀ ت ٿ ٽ ٺ ث پ ج ڄ جھ ڃ چ ڇ ح خ د ڌ ڏ ڊ ڍ ذ ر ڙ ز س ش ص ض ط ظ ع غ ف ڦ ق ڪ ک گ ڳ گھ ڱ ل م ن ڻ و ھ ء ي".split(" ")
    }
}
