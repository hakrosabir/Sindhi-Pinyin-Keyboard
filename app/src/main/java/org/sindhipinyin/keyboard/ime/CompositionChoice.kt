package org.sindhipinyin.keyboard.ime

import org.sindhipinyin.engine.Candidate
import org.sindhipinyin.engine.CandidateKind

/** Space accepts the best whole-word spelling/conversion; completion and correction need a tap. */
internal object CompositionChoice {
    fun automatic(candidates: List<Candidate>, roman: String): String {
        val exact = candidates.filter { it.kind == CandidateKind.EXACT }.map { it.text }.distinct()
        if (exact.isNotEmpty()) return exact.first()
        candidates.firstOrNull { it.kind == CandidateKind.DICTIONARY }?.let { return it.text }
        return candidates.filter { it.kind == CandidateKind.TRANSLITERATION }
            .firstOrNull()?.text ?: roman
    }
}
