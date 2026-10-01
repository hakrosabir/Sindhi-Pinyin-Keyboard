package org.sindhipinyin.engine

import java.text.Normalizer
import java.util.Locale

/** Small, explicitly authored phrase model. Matching uses only caller-supplied session context.
 * Results are insertion suffixes: already committed words are never returned again.
 */
internal class ConversationModel(tsv: String) {
    private data class Sentence(val roman: List<String>, val script: List<String>, val weight: Int)
    private data class Match(val sentence: Sentence, val start: Int, val contextLength: Int)
    private val sentences: List<Sentence>

    init {
        require(tsv.length <= 500_000) { "Conversation model too large" }
        val rows = tsv.lineSequence().filter { it.isNotBlank() && !it.startsWith('#') }.toList()
        if (rows.isEmpty()) sentences = emptyList() else {
            require(rows.first() == "roman\ttext\tweight\tnote") { "Invalid conversation model header" }
            require(rows.size <= 1001) { "Too many conversation templates" }
            sentences = rows.drop(1).map { row ->
                val fields = row.split('\t')
                require(fields.size == 4) { "Invalid conversation model row" }
                val roman = tokens(normalize(fields[0]))
                val script = tokens(fields[1])
                require(roman.size in 2..12 && roman.size == script.size) { "Conversation tokens must align: ${fields[0]}" }
                require(roman.all { it.length <= 32 && it.all { ch -> ch.isLetter() } }) { "Invalid conversation Roman token" }
                require(script.all { it.length <= 32 && it.all { ch -> ch in '\u0600'..'\u06ff' && (ch.isLetter() || ch == '\u06fe' || ch.category == CharCategory.NON_SPACING_MARK) } }) { "Invalid conversation script token" }
                val weight = fields[2].toIntOrNull()
                require(weight != null && weight in 0..1000) { "Invalid conversation weight" }
                Sentence(roman, script, weight)
            }
        }
    }

    fun suggestions(roman: String, context: String, exactScripts: Set<String>): List<Candidate> {
        val input = normalize(roman)
        if (input.length > 64 || input.any { it.isWhitespace() }) return emptyList()
        if (input.isEmpty() && context.isBlank()) return emptyList()
        val matches = matches(context).filter { match ->
            val sentence = match.sentence
            input.isEmpty() || sentence.roman[match.start].startsWith(input) || sentence.script[match.start] in exactScripts
        }
        return matches.sortedWith(compareByDescending<Match> { it.contextLength }.thenByDescending { it.sentence.weight })
            .map { match ->
                val text = match.sentence.script.drop(match.start).joinToString(" ")
                Candidate(text, if (' ' in text) CandidateKind.PHRASE else CandidateKind.NEXT_WORD,
                    match.sentence.roman.drop(match.start).joinToString(" "))
            }.distinctBy { it.text }.take(6)
    }

    fun contextBoost(context: String, script: String): Int = matches(context)
        .filter { it.contextLength > 0 && it.sentence.script[it.start] == script }
        .maxOfOrNull { it.contextLength * 180 } ?: 0

    private fun matches(context: String): List<Match> {
        val history = tokens(Normalizer.normalize(context, Normalizer.Form.NFC)).takeLast(4)
        if (history.isEmpty()) return sentences.map { Match(it, 0, 0) }
        return buildList {
            for (sentence in sentences) for (start in 1 until sentence.script.size) {
                val longest = (1..minOf(4, history.size, start)).lastOrNull { size ->
                    history.takeLast(size) == sentence.script.subList(start - size, start)
                } ?: continue
                add(Match(sentence, start, longest))
            }
        }
    }

    private fun normalize(value: String) = Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFC)
    private fun tokens(value: String) = value.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
}
