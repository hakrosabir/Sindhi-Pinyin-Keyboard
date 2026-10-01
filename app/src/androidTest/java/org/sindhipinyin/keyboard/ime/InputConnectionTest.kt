package org.sindhipinyin.keyboard.ime

import android.text.Editable
import android.text.Selection
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.sindhipinyin.engine.Candidate
import org.sindhipinyin.engine.CandidateKind

/** Real Android Editable/ICU behavior; this is a contract harness, not a substitute for chat-app tests. */
@RunWith(AndroidJUnit4::class)
class InputConnectionTest {
    @Test fun composingCandidatesReplaceOnlyTheComposition() {
        val editor = BufferConnection("before ")
        editor.setComposingText("sin", 1)
        editor.setComposingText("سنڌي", 1)
        editor.commitText("سنڌي", 1)
        editor.finishComposingText()
        editor.commitText(" ", 1)
        assertEquals("before سنڌي ", editor.buffer.toString())
    }

    @Test fun typingReplacesSelectedTextWithoutDeletingNeighbors() {
        val editor = BufferConnection("left wrong right")
        Selection.setSelection(editor.buffer, 5, 10)
        editor.setComposingText("سنڌي", 1)
        editor.commitText("سنڌي", 1)
        assertEquals("left سنڌي right", editor.buffer.toString())
    }

    @Test fun aRapidSpaceUsesCurrentRomanSnapshot() {
        val editor = BufferConnection("")
        editor.setComposingText("si", 1)
        editor.setComposingText("sindhi", 1)
        val committed = CompositionChoice.automatic(listOf(Candidate("سنڌي", CandidateKind.EXACT)), "sindhi")
        editor.commitText(committed, 1)
        editor.finishComposingText()
        editor.commitText(" ", 1)
        assertEquals("سنڌي ", editor.buffer.toString())
    }

    @Test fun backspaceDeletesOneEmojiOrCombiningCluster() {
        listOf("x😀", "xā", "xبَ").forEach { text ->
            val editor = BufferConnection(text)
            InputOperations.deleteBackward(editor, text.length, text.length, false) {}
            assertEquals("x", editor.buffer.toString())
        }
    }

    @Test fun backspaceDeletesSelectionOnly() {
        val editor = BufferConnection("one two three")
        Selection.setSelection(editor.buffer, 4, 7)
        InputOperations.deleteBackward(editor, 4, 7, false) {}
        assertEquals("one  three", editor.buffer.toString())
        assertEquals(0, editor.lookBehindReads)
    }

    @Test fun protectedBackspaceNeverReadsThePassword() {
        val editor = BufferConnection("private")
        InputOperations.deleteBackward(editor, 7, 7, true) {}
        assertEquals(0, editor.lookBehindReads)
        assertEquals(listOf(KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP), editor.deleteEvents)
    }

    @Test fun insertionAfterCursorMovementUsesNewPosition() {
        val editor = BufferConnection("left right")
        Selection.setSelection(editor.buffer, 5)
        editor.setComposingText("سنڌي ", 1)
        editor.finishComposingText()
        assertEquals("left سنڌي right", editor.buffer.toString())
    }

    private class BufferConnection(text: String) : BaseInputConnection(
        View(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext), true
    ) {
        val buffer: Editable = Editable.Factory.getInstance().newEditable(text).apply { Selection.setSelection(this, length) }
        var lookBehindReads = 0
        val deleteEvents = mutableListOf<Int>()
        override fun getEditable(): Editable = buffer
        override fun getTextBeforeCursor(length: Int, flags: Int): CharSequence? {
            lookBehindReads++
            return super.getTextBeforeCursor(length, flags)
        }
        override fun sendKeyEvent(event: KeyEvent): Boolean {
            if (event.keyCode == KeyEvent.KEYCODE_DEL) deleteEvents += event.action
            return true
        }
    }
}
