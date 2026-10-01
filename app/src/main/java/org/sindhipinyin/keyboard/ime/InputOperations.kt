package org.sindhipinyin.keyboard.ime

import android.icu.text.BreakIterator
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import java.util.Locale

internal object InputOperations {
    fun deleteBackward(connection: InputConnection, start: Int, end: Int, privateField: Boolean, expectSelection: (Int) -> Unit) {
        if (start >= 0 && end >= 0 && start != end) {
            expectSelection(minOf(start, end))
            connection.commitText("", 1)
        } else if (privateField) {
            // Never read a password, even for a transient deletion operation.
            expectSelection(-1)
            connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
        } else {
            // Bounded and immediately discarded: this look-behind is used only to delete one grapheme.
            val before = connection.getTextBeforeCursor(128, 0)?.toString().orEmpty()
            if (before.isNotEmpty()) {
                val boundary = BreakIterator.getCharacterInstance(Locale.ROOT).apply { setText(before) }
                    .preceding(before.length).coerceAtLeast(0)
                val count = before.length - boundary
                expectSelection(if (start >= 0) (start - count).coerceAtLeast(0) else -1)
                connection.deleteSurroundingText(count, 0)
            } else {
                expectSelection(-1)
                connection.deleteSurroundingTextInCodePoints(1, 0)
            }
        }
    }
}
