package org.sindhipinyin.keyboard.ime

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.sindhipinyin.engine.Candidate
import org.sindhipinyin.engine.CandidateKind

class CandidateSurfaceTest {
    private class Actions : KeyboardSurface.Actions {
        var selected: Candidate? = null
        override fun text(text: String) {}
        override fun backspace() {}
        override fun space() {}
        override fun enter() {}
        override fun nextMode() {}
        override fun globe() {}
        override fun settings() {}
        override fun candidate(candidate: Candidate) { selected = candidate }
    }
    private fun buttons(view: View): List<Button> = if (view is Button) listOf(view) else if (view is ViewGroup)
        (0 until view.childCount).flatMap { buttons(view.getChildAt(it)) } else emptyList()
    private fun height(view: View): Int {
        view.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        return view.measuredHeight
    }

    @Test fun suggestionsKeepHeightAndRepeatedResultsKeepTouchableViews() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val actions = Actions()
            val view = KeyboardSurface(InstrumentationRegistry.getInstrumentation().targetContext, actions)
            view.configure(KeyboardMode.ROMAN, false, false, false, "Enter", "light", false, false)
            val initialHeight = height(view)
            val items = listOf(Candidate("مان", CandidateKind.EXACT, "ma"), Candidate("ماءُ", CandidateKind.EXACT, "ma"), Candidate("مان ٺيڪ آهيان", CandidateKind.PHRASE, "maan theek ahyan"))
            view.candidates(items, "ma")
            assertEquals(initialHeight, height(view))
            val second = buttons(view).first { it.contentDescription.toString().startsWith("Candidate 2,") }
            view.candidates(items, "ma")
            assertSame(second, buttons(view).first { it.contentDescription.toString().startsWith("Candidate 2,") })
            second.performClick()
            assertEquals(items[1], actions.selected)
            view.candidates(emptyList(), "")
            assertEquals(initialHeight, height(view))
        }
    }
}
