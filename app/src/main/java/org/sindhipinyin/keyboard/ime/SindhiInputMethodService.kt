package org.sindhipinyin.keyboard.ime

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import org.sindhipinyin.engine.Candidate
import org.sindhipinyin.engine.CandidateKind
import org.sindhipinyin.engine.EnglishEngine
import org.sindhipinyin.engine.RomanMode
import org.sindhipinyin.keyboard.MainActivity
import org.sindhipinyin.keyboard.data.KeyboardRepository
import java.util.ArrayDeque
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/** No networking, foreground/background service, app history, or text logging. */
class SindhiInputMethodService : InputMethodService(), KeyboardSurface.Actions {
    private val main = Handler(Looper.getMainLooper())
    private val worker = ThreadPoolExecutor(1, 1, 20L, TimeUnit.SECONDS, LinkedBlockingQueue()).apply {
        allowCoreThreadTimeOut(true)
    }
    private val repository by lazy { KeyboardRepository.get(applicationContext) }
    private var surface: KeyboardSurface? = null
    private var lookup: Future<*>? = null
    private var session = 0L
    private var generation = 0L
    private var active = false
    private var policy = EditorPolicy(false, false, true)
    private var mode = KeyboardMode.ROMAN
    private var preferredMode = KeyboardMode.ROMAN
    private var roman = ""
    private var previous = ""
    private var displayed = emptyList<Candidate>()
    private var composingStart = -1
    private var composingText = ""
    private var insertedSpace = false
    private var selectionStart = -1
    private var selectionEnd = -1
    private val ownSelections = ArrayDeque<Pair<Int, Int>>()

    override fun onCreate() {
        super.onCreate()
        worker.execute { runCatching { repository.candidates("", "") } }
    }

    override fun onCreateInputView(): View = KeyboardSurface(this, this).also {
        surface = it
        configureSurface()
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        // Never carry words, editor IDs or composing text between fields, even within the same app.
        resetSession(finishComposition = false)
        active = true
        policy = EditorPolicy.from(attribute)
        mode = if (policy.privateField) KeyboardMode.ENGLISH else preferredMode
        selectionStart = attribute?.initialSelStart ?: -1
        selectionEnd = attribute?.initialSelEnd ?: -1
        configureSurface()
        worker.execute { runCatching { repository.candidates("", "") } }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        active = true
        configureSurface()
        refreshCandidates()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        resetSession(finishComposition = true)
        active = false
        super.onFinishInputView(finishingInput)
    }

    override fun onFinishInput() {
        resetSession(finishComposition = true)
        active = false
        super.onFinishInput()
    }

    override fun onWindowHidden() {
        resetSession(finishComposition = true)
        active = false
        super.onWindowHidden()
    }

    override fun onDestroy() {
        active = false
        invalidateLookup()
        main.removeCallbacksAndMessages(null)
        worker.shutdownNow()
        surface = null
        super.onDestroy()
    }

    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        val reported = newSelStart to newSelEnd
        // Delayed acknowledgements of our own batched commits must not erase next-word context.
        if (ownSelections.contains(reported)) {
            while (ownSelections.isNotEmpty()) if (ownSelections.removeFirst() == reported) break
            return
        }
        if (selectionStart == newSelStart && selectionEnd == newSelEnd) return
        val composingCursor = roman.isNotEmpty() && candidatesStart >= 0 && candidatesEnd >= 0 &&
            newSelStart == candidatesEnd && newSelEnd == candidatesEnd
        selectionStart = newSelStart
        selectionEnd = newSelEnd
        if (composingCursor) {
            composingStart = candidatesStart
            return
        }
        // User moved the cursor, selected text, or the app rewrote the field. Drop ephemeral context.
        resetSession(finishComposition = true)
    }

    override fun text(text: String) {
        if (!active || text.isEmpty()) return
        val isRomanLetter = text.codePoints().allMatch { Character.isLetter(it) || Character.getType(it) == Character.NON_SPACING_MARK.toInt() } ||
            text == "ʻ" || text == "ʼ" || text == "'" || text == "|"
        if (mode != KeyboardMode.SCRIPT && policy.predictions && isRomanLetter) {
            insertedSpace = false
            if (roman.codePointCount(0, roman.length) >= MAX_ROMAN) commitComposition()
            if (roman.isEmpty()) composingStart = minOf(selectionStart, selectionEnd)
            roman += text
            // Pure, bounded transliteration gives immediate composition without waiting for SQLite.
            refreshCandidates()
        } else {
            commitComposition()
            if (insertedSpace && policy.predictions && text in listOf(".", ",", "!", "?", ":", ";", "۔", "،", "؟")) {
                edit { connection ->
                    if (selectionStart == selectionEnd && connection.getTextBeforeCursor(1, 0)?.toString() == " ") {
                        if (connection.deleteSurroundingText(1, 0)) expectSelection((selectionStart - 1).coerceAtLeast(0))
                    }
                }
            }
            commitText(text)
            if (text.any { it in ".!?؟۔\n" }) previous = ""
            refreshCandidates()
        }
    }

    override fun backspace() {
        if (!active) return
        insertedSpace = false
        if (roman.isNotEmpty()) {
            roman = roman.substring(0, roman.offsetByCodePoints(roman.length, -1))
            if (roman.isEmpty()) {
                edit { connection ->
                    val position = composingStart
                    expectSelection(position)
                    connection.setComposingText("", 1)
                    connection.finishComposingText()
                }
                composingText = ""
                composingStart = -1
                invalidateLookup()
                displayed = emptyList()
                surface?.candidates(emptyList(), "")
            } else {
                refreshCandidates()
            }
            return
        }
        previous = ""
        invalidateLookup()
        displayed = emptyList()
        surface?.candidates(emptyList(), "")
        edit { connection -> InputOperations.deleteBackward(connection, selectionStart, selectionEnd, policy.privateField, ::expectSelection) }
    }

    override fun space() {
        if (!active) return
        commitComposition()
        commitText(" ")
        refreshCandidates()
    }

    override fun enter() {
        if (!active) return
        commitComposition()
        previous = ""
        invalidateLookup()
        surface?.candidates(emptyList(), "")
        val info = currentInputEditorInfo
        val action = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        val canPerform = info != null && info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION == 0
        if (canPerform && (info!!.actionId != 0 || action !in listOf(EditorInfo.IME_ACTION_NONE, EditorInfo.IME_ACTION_UNSPECIFIED))) {
            var performed = false
            edit { performed = it.performEditorAction(if (info.actionId != 0) info.actionId else action) }
            if (!performed) commitText("\n")
        } else commitText("\n")
    }

    override fun candidate(candidate: Candidate) {
        if (!active || !policy.predictions || mode == KeyboardMode.SCRIPT || candidate !in displayed) return
        val input = roman
        val prior = previous
        invalidateLookup()
        val committed = commitText(candidate.text)
        clearCompositionState()
        if (!committed) { previous = ""; refreshCandidates(); return }
        commitText(" ")
        if (candidate.kind != CandidateKind.RAW) {
            rememberContext(candidate.text)
            val learningAlias = if (candidate.kind in setOf(CandidateKind.PHRASE, CandidateKind.COMPLETION, CandidateKind.NEXT_WORD))
                candidate.roman else input.ifEmpty { candidate.roman }
            maybeLearn(learningAlias, candidate.text, prior)
        } else previous = ""
        refreshCandidates()
    }

    override fun nextMode() {
        commitComposition()
        previous = ""
        mode = KeyboardMode.entries[(mode.ordinal + 1) % KeyboardMode.entries.size]
        if (!policy.privateField) preferredMode = mode
        configureSurface()
        refreshCandidates()
    }

    override fun globe() {
        commitComposition()
        resetSession(finishComposition = true)
        (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)?.showInputMethodPicker()
    }

    override fun settings() {
        commitComposition()
        resetSession(finishComposition = true)
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        requestHideSelf(0)
    }

    private fun configureSurface() {
        val preferences = repository.preferences
        surface?.configure(mode, policy.privateField, policy.numeric,
            preferences.romanMode == RomanMode.SCHOLARLY, actionLabel(), preferences.theme,
            preferences.vibration && !policy.privateField, preferences.sound && !policy.privateField)
    }

    private fun actionLabel(): String {
        val info = currentInputEditorInfo ?: return "Enter"
        if (info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) return "Enter"
        info.actionLabel?.takeIf { it.isNotBlank() }?.let { return it.toString().take(12) }
        return when (info.imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_GO -> "Go"
            EditorInfo.IME_ACTION_SEARCH -> "Search"
            EditorInfo.IME_ACTION_SEND -> "Send"
            EditorInfo.IME_ACTION_NEXT -> "Next"
            EditorInfo.IME_ACTION_DONE -> "Done"
            EditorInfo.IME_ACTION_PREVIOUS -> "Previous"
            else -> "Enter"
        }
    }

    private fun refreshCandidates() {
        invalidateLookup()
        if (!active || !policy.predictions || mode == KeyboardMode.SCRIPT) {
            displayed = emptyList()
            surface?.candidates(emptyList(), "")
            return
        }
        // Publish one immutable result per input change. Never clear then asynchronously replace
        // tappable candidates. SQLite is warmed separately, off the input thread.
        displayed = safeBaseCandidates(roman)
        if (roman.isNotEmpty()) setComposition(automaticCandidate(displayed, roman))
        surface?.candidates(displayed, roman)
        if (roman.isEmpty() && mode == KeyboardMode.ENGLISH) updateEnglishCapitalization()
    }

    private fun updateEnglishCapitalization() {
        if (mode != KeyboardMode.ENGLISH || !policy.predictions) return
        val caps = runCatching { currentInputConnection?.getCursorCapsMode(android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) ?: 0 }.getOrDefault(0)
        surface?.setAutomaticShift(caps != 0 || selectionStart == 0)
    }
    private fun safeBaseCandidates(input: String): List<Candidate> = try {
        if (mode == KeyboardMode.ENGLISH) EnglishEngine.candidates(input, previous) else repository.baseCandidates(input, previous)
    } catch (_: RuntimeException) { listOf(Candidate(input, CandidateKind.RAW, input)) }

    private fun automaticCandidate(candidates: List<Candidate>, input: String): String {
        // Pinyin-style default: show/commit the best spelling while alternatives stay selectable.
        return CompositionChoice.automatic(candidates, input)
    }

    private fun setComposition(text: String) {
        if (text == composingText) return
        edit { connection ->
            val start = if (composingStart >= 0) composingStart else minOf(selectionStart, selectionEnd)
            expectSelection(if (start >= 0) start + text.length else -1)
            if (connection.setComposingText(text, 1)) composingText = text
        }
    }

    private fun commitComposition() {
        if (roman.isEmpty()) return
        val input = roman
        val prior = previous
        // Do not rely on asynchronous result timing: exact/base lookup is bounded and database-free.
        val text = automaticCandidate(displayed.ifEmpty { safeBaseCandidates(input) }, input)
        invalidateLookup()
        val committed = commitText(text)
        clearCompositionState()
        if (committed && (text != input || mode == KeyboardMode.ENGLISH)) {
            rememberContext(text)
            maybeLearn(input, text, prior)
        } else previous = ""
    }

    private fun maybeLearn(input: String, text: String, prior: String) {
        if (mode != KeyboardMode.ROMAN || input.isEmpty() || !policy.predictions || !repository.preferences.learning) return
        val revision = repository.preferences.learningRevision
        val romanMode = repository.preferences.romanMode
        // Only explicitly opted-in committed words reach SQLite; no database write runs on the UI thread.
        worker.execute {
            try { repository.learn(input, text, prior.substringAfterLast(' '), revision, romanMode); repository.candidates("", "") } catch (_: RuntimeException) { /* typing remains available */ }
        }
    }

    private fun rememberContext(text: String) {
        previous = (previous + " " + text).trim().split(Regex("\\s+")).takeLast(4).joinToString(" ").takeLast(128)
    }

    private fun commitText(text: String): Boolean {
        var committed = false
        edit { connection ->
            val start = if (composingStart >= 0 && roman.isNotEmpty()) composingStart else minOf(selectionStart, selectionEnd)
            expectSelection(if (start >= 0) start + text.length else -1)
            committed = connection.commitText(text, 1)
            if (committed) insertedSpace = text.endsWith(' ')
            connection.finishComposingText()
        }
        return committed
    }

    override fun onCurrentInputMethodSubtypeChanged(newSubtype: android.view.inputmethod.InputMethodSubtype?) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype)
        commitComposition()
        previous = ""
        preferredMode = if (newSubtype?.languageTag?.startsWith("en") == true) KeyboardMode.ENGLISH else KeyboardMode.ROMAN
        mode = if (policy.privateField) KeyboardMode.ENGLISH else preferredMode
        configureSurface()
        refreshCandidates()
    }

    private inline fun edit(block: (InputConnection) -> Unit) {
        val connection = currentInputConnection ?: return
        try {
            connection.beginBatchEdit()
            try { block(connection) } finally { connection.endBatchEdit() }
        } catch (_: RuntimeException) {
            // Some remote editors disconnect while a key is tapped. Do not log their text or crash.
            invalidateLookup()
        }
    }

    private fun expectSelection(position: Int) {
        selectionStart = position
        selectionEnd = position
        if (position < 0) return
        ownSelections.addLast(position to position)
        while (ownSelections.size > 16) ownSelections.removeFirst()
    }

    private fun invalidateLookup() {
        generation++
        lookup?.cancel(true)
        lookup = null
        worker.purge()
    }

    private fun clearCompositionState() {
        roman = ""
        composingText = ""
        composingStart = -1
        displayed = emptyList()
    }

    private fun resetSession(finishComposition: Boolean) {
        session++
        invalidateLookup()
        if (finishComposition) edit { it.finishComposingText() }
        clearCompositionState()
        previous = ""
        ownSelections.clear()
        insertedSpace = false
        surface?.candidates(emptyList(), "")
    }

    companion object { private const val MAX_ROMAN = 64 }
}
