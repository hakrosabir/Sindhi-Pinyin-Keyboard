package org.sindhipinyin.engine

import java.util.Locale

/** Authored offline starter vocabulary. No editor history or network requests. */
object EnglishEngine {
    private val words = ("a about above after again all also always am an and another any are around as ask at away " +
        "back bad be beautiful because been before being best better big book both bring brother but buy by call came can cannot car care change check child city clear close come could country day dear did different do does doing done dont down drink each easy eat education else email end enough even evening ever every everyone example family far fast father feel few find fine first five fix food for found four free friend friends from full get give go going good great group had happy hard has have he head hear hello help her here hey hi high him his home hope hot hour house how i if important in information inside into is it its just keep keyboard kind know language last late later learn leave left let lets life like little live local long look love made make man many may maybe me mean message might minute miss mobile mom money month more morning most mother move much must my name near need never new next nice night no not nothing now number of off offline often oh okay old on once one only open or other our out over own page part people person phone place please point problem question quick read ready really right room run said same say school screen see send sentence set she should show simple since sister sleep small smart so some someone something soon sorry sound space speak start stay still stop story student study such sure take talk teach tell test text than thank thanks that the their them then there these they thing think this those three through time to today together tomorrow too try two type under understand until up us use used useful user very wait walk want warm was water way we week well went were what when where which while white who why will with without word work world would write wrong year yes yesterday yet you your yours").split(' ').distinct()
    private val contractions = mapOf("dont" to "don't", "cant" to "can't", "im" to "I'm", "ive" to "I've", "youre" to "you're", "isnt" to "isn't", "wont" to "won't", "didnt" to "didn't", "lets" to "let's")
    private val next = mapOf("how" to listOf("are", "is", "do"), "thank" to listOf("you"), "good" to listOf("morning", "night", "evening"), "i" to listOf("am", "have", "will"), "are" to listOf("you", "we", "they"), "you" to listOf("are", "can", "have"), "see" to listOf("you"), "my" to listOf("name", "friend", "home"))

    fun candidates(input: String, previous: String = ""): List<Candidate> {
        if (input.isEmpty()) return next[previous.substringAfterLast(' ').lowercase(Locale.ROOT)].orEmpty().map { Candidate(it, CandidateKind.NEXT_WORD, it) }
        if (input.length > 64) return listOf(Candidate(input, CandidateKind.RAW, input))
        val lower = input.lowercase(Locale.ROOT)
        fun styled(word: String) = when {
            word == "i" -> "I"
            input.length > 1 && input.all { it.isUpperCase() } -> word.uppercase(Locale.ROOT)
            input.first().isUpperCase() -> word.replaceFirstChar { it.uppercaseChar() }
            else -> word
        }
        val result = mutableListOf(Candidate(if (lower == "i") "I" else input, CandidateKind.EXACT, input))
        contractions[lower]?.let { result += Candidate(styled(it), CandidateKind.CORRECTION, input) }
        words.asSequence().filter { it.startsWith(lower) && it != lower }.take(6).forEach { result += Candidate(styled(it), CandidateKind.COMPLETION, it) }
        if (lower.length >= 3 && lower !in words) words.asSequence().filter { near(lower, it) }.take(3).forEach { result += Candidate(styled(it), CandidateKind.CORRECTION, it) }
        return result.distinctBy { it.text }.take(9)
    }

    private fun near(a: String, b: String): Boolean {
        if (kotlin.math.abs(a.length - b.length) > 1) return false
        if (a.length == b.length) {
            val differences = a.indices.filter { a[it] != b[it] }
            return differences.size == 1 || (differences.size == 2 && differences[1] == differences[0] + 1 && a[differences[0]] == b[differences[1]] && a[differences[1]] == b[differences[0]])
        }
        val short = if (a.length < b.length) a else b
        val long = if (a.length < b.length) b else a
        var i = 0
        while (i < short.length && short[i] == long[i]) i++
        return short.substring(i) == long.substring(i + 1)
    }
}
