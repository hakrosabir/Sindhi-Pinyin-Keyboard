package org.sindhipinyin.engine

import org.junit.Assert.*
import org.junit.Test

class EnglishEngineTest {
    @Test fun retainsTypedWordAndOffersCompletion() {
        val items = EnglishEngine.candidates("hel")
        assertEquals("hel", items.first().text)
        assertTrue(items.any { it.text == "hello" && it.kind == CandidateKind.COMPLETION })
    }
    @Test fun transposedTypoNeedsExplicitChoice() {
        val items = EnglishEngine.candidates("teh")
        assertEquals("teh", items.first().text)
        assertTrue(items.any { it.text == "the" && it.kind == CandidateKind.CORRECTION })
    }
    @Test fun offersContractionsAndPreservesCapitalization() {
        assertTrue(EnglishEngine.candidates("Dont").any { it.text == "Don't" })
        assertTrue(EnglishEngine.candidates("HEL").any { it.text == "HELLO" })
        assertEquals("I", EnglishEngine.candidates("i").first().text)
    }
    @Test fun predictsOnlyFromSuppliedContext() {
        assertEquals("you", EnglishEngine.candidates("", "thank").first().text)
        assertTrue(EnglishEngine.candidates("").isEmpty())
    }
}
