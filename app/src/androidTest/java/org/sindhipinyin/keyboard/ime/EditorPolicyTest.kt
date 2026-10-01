package org.sindhipinyin.keyboard.ime

import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditorPolicyTest {
    @Test fun ordinaryChatAllowsPredictions() {
        assertTrue(EditorPolicy.from(editor(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)).predictions)
    }

    @Test fun allTextPasswordVariationsDisablePredictions() {
        listOf(InputType.TYPE_TEXT_VARIATION_PASSWORD, InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD).forEach {
            val policy = EditorPolicy.from(editor(InputType.TYPE_CLASS_TEXT or it))
            assertTrue(policy.privateField)
            assertFalse(policy.predictions)
        }
    }

    @Test fun identifiersAndNumericEditorsDefaultToRaw() {
        listOf(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_URI).forEach {
            assertFalse(EditorPolicy.from(editor(InputType.TYPE_CLASS_TEXT or it)).predictions)
        }
        listOf(InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_DATETIME).forEach {
            val policy = EditorPolicy.from(editor(it))
            assertTrue(policy.numeric)
            assertFalse(policy.predictions)
        }
        assertFalse(EditorPolicy.from(editor(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)).predictions)
    }

    @Test fun editorsCanOptOutOfPersonalizedLearningOrSuggestions() {
        val incognito = editor(InputType.TYPE_CLASS_TEXT).apply { imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING }
        assertFalse(EditorPolicy.from(incognito).predictions)
        assertFalse(EditorPolicy.from(editor(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)).predictions)
    }

    @Test fun missingOrUnspecifiedInputIsConservative() {
        assertFalse(EditorPolicy.from(null).predictions)
        assertFalse(EditorPolicy.from(editor(InputType.TYPE_NULL)).predictions)
    }

    private fun editor(type: Int) = EditorInfo().apply { inputType = type }
}
