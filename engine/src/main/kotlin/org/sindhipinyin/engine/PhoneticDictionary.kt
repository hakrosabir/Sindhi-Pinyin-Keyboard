package org.sindhipinyin.engine

import java.text.Normalizer
import java.util.Locale

/** Candidate retrieval from a script word list, not a claim of correct pronunciation.
 * Vowels and related consonants are folded for retrieval only; output spelling stays intact.
 * A consonant index keeps the 10k-word dictionary out of the per-key scanning path.
 */
internal class PhoneticDictionary(dictionary: String) {
    private data class Entry(val text: String, val reading: String)
    private val index: Map<String, List<Entry>>
    val size: Int

    init {
        require(dictionary.length <= 2_000_000) { "Word list too large" }
        val entries = dictionary.lineSequence().drop(1).map { it.trim().substringBefore('/') }
            .filter { it.length in 2..32 && it.all { ch -> ch in '\u0600'..'\u06ff' && (ch.isLetter() || ch.category == CharCategory.NON_SPACING_MARK) } }
            .map { Normalizer.normalize(it, Normalizer.Form.NFC) }.distinct().take(25000)
            .map { Entry(it, scriptReading(it)) }.filter { it.reading.isNotEmpty() }.toList()
        size = entries.size
        index = entries.groupBy { skeleton(it.reading) }
    }

    fun matches(roman: String): List<String> {
        if (roman.length !in 2..40 || roman.any { !it.isLetter() }) return emptyList()
        val reading = fold(roman)
        val signature = skeleton(reading)
        if (signature.isEmpty()) return emptyList()
        return index[signature].orEmpty().asSequence()
            .filter { kotlin.math.abs(it.reading.length - reading.length) <= 6 }
            .map { it to distance(reading, it.reading) }
            .sortedWith(compareBy<Pair<Entry, Int>> { it.second }.thenBy { it.first.text.length }.thenBy { it.first.text })
            .take(10).map { it.first.text }.toList()
    }

    private fun distance(a: String, b: String): Int {
        var row = IntArray(b.length + 1) { it * 2 }
        for (i in a.indices) {
            val next = IntArray(b.length + 1)
            next[0] = (i + 1) * 2
            for (j in b.indices) {
                next[j + 1] = minOf(row[j + 1] + if (a[i] in "aeiou") 1 else 4,
                    next[j] + if (b[j] in "aeiou") 1 else 4,
                    row[j] + if (a[i] == b[j]) 0 else if (a[i] in "aeiou" && b[j] in "aeiou") 1 else 5)
            }
            row = next
        }
        return row.last()
    }

    private fun skeleton(value: String): String = value.filter { it !in "aeiou" }
    private fun fold(input: String): String = Normalizer.normalize(input.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .filter { it.category != CharCategory.NON_SPACING_MARK }
        .replace("chh", "Ch").replace("ch", "c").replace("Ch", "ch")
        .replace('w', 'o').replace('v', 'o').replace('y', 'i').replace('x', 'k')
        .replace(Regex("([a-z])\\1+"), "$1")

    private fun scriptReading(text: String): String {
        val raw = buildString {
            text.forEach { ch -> append(when (ch) {
                'ا', 'آ', 'أ', 'إ', 'ع' -> "a"
                'ب', 'ٻ' -> "b"; 'ڀ' -> "bh"; 'پ' -> "p"; 'ڦ' -> "ph"
                'ت', 'ٽ', 'ط' -> "t"; 'ٿ', 'ٺ' -> "th"
                'ج', 'ڄ' -> "j"; 'چ' -> "c"; 'ڇ' -> "ch"; 'ڃ' -> "ny"
                'د', 'ڏ', 'ڊ' -> "d"; 'ڌ', 'ڍ' -> "dh"
                'ر', 'ڙ' -> "r"; 'س', 'ث', 'ص' -> "s"; 'ش' -> "sh"
                'ز', 'ذ', 'ض', 'ظ' -> "z"; 'ف' -> "f"; 'ق' -> "q"
                'ڪ', 'ک', 'ك' -> if (ch == 'ک') "kh" else "k"
                'گ', 'ڳ' -> "g"; 'غ' -> "gh"; 'ڱ' -> "ng"
                'ل' -> "l"; 'م' -> "m"; 'ن', 'ڻ', 'ں' -> "n"
                'ه', 'ھ', 'ح', 'ہ' -> "h"; 'و', 'ؤ' -> "o"
                'ي', 'ی', 'ى', 'ئ', 'ے' -> "i"
                else -> ""
            }) }
        }
        // Already tokenized script ch/c must not undergo Roman ch-folding again.
        return raw.replace('y', 'i').replace(Regex("([a-z])\\1+"), "$1")
    }
}
