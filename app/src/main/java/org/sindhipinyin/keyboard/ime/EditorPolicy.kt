package org.sindhipinyin.keyboard.ime

import android.text.InputType
import android.view.inputmethod.EditorInfo

/** A conservative default. It never inspects the editor's content or package name. */
internal data class EditorPolicy(val predictions: Boolean, val numeric: Boolean, val privateField: Boolean) {
    companion object {
        fun from(info: EditorInfo?): EditorPolicy {
            if (info == null) return EditorPolicy(false, false, true)
            val kind = info.inputType and InputType.TYPE_MASK_CLASS
            val variation = info.inputType and InputType.TYPE_MASK_VARIATION
            val numeric = kind == InputType.TYPE_CLASS_NUMBER || kind == InputType.TYPE_CLASS_PHONE ||
                kind == InputType.TYPE_CLASS_DATETIME
            val password = (kind == InputType.TYPE_CLASS_TEXT && variation in setOf(
                InputType.TYPE_TEXT_VARIATION_PASSWORD, InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            )) || (kind == InputType.TYPE_CLASS_NUMBER && variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
            val identifier = kind == InputType.TYPE_CLASS_TEXT && variation in setOf(
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
                InputType.TYPE_TEXT_VARIATION_URI, InputType.TYPE_TEXT_VARIATION_FILTER
            )
            val noLearning = info.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING != 0
            val noSuggestions = info.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0
            val privateField = password || identifier || noLearning || noSuggestions || kind != InputType.TYPE_CLASS_TEXT
            return EditorPolicy(!privateField, numeric, privateField)
        }
    }
}
