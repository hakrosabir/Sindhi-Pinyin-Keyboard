package org.sindhipinyin.engine

import org.junit.Assert.*
import org.junit.Test

class UserAliasTest {
    private fun resource(name: String) = javaClass.classLoader!!.getResource(name)!!.readText()
    private val engine = CandidateEngine(resource("mapping.tsv"), resource("lexicon.tsv"), resource("phrases.tsv"), resource("dictionaries/sd.dic"), resource("conversations.tsv"))
    @Test fun suppliedAliasesAreTopFourExactChoices() {
        val rows = resource("lexicon.tsv").lineSequence().filter { it.contains("USER supplied") }.map { it.split('\t') }.toList()
        assertTrue(rows.size >= 139)
        rows.forEach { row ->
            assertTrue("Missing ${row[1]} → ${row[2]}", engine.candidates(row[1], RomanMode.EVERYDAY).take(4).any { it.text == row[2] && it.kind == CandidateKind.EXACT })
        }
    }
    @Test fun ambiguousInputKeepsBothUserMeanings() {
        val choices = engine.candidates("ma", RomanMode.EVERYDAY).map { it.text }
        assertTrue(choices.containsAll(listOf("مان", "ماءُ")))
        assertTrue(engine.candidates("hi", RomanMode.EVERYDAY).map { it.text }.containsAll(listOf("هِي", "هي")))
    }
    @Test fun exactWordDoesNotSuppressDictionaryAlternatives() {
        assertTrue(engine.candidates("kitab", RomanMode.EVERYDAY).size >= 3)
    }
    @Test fun repeatedLookupsAreStable() {
        val initial = engine.candidates("ma", RomanMode.EVERYDAY)
        repeat(20) { assertEquals(initial, engine.candidates("ma", RomanMode.EVERYDAY)) }
    }
}
