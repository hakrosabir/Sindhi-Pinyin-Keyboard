package org.sindhipinyin.keyboard.ime

import org.junit.Assert.assertEquals
import org.junit.Test
import org.sindhipinyin.engine.Candidate
import org.sindhipinyin.engine.CandidateKind

class CompositionChoiceTest {
    @Test fun aCorrectionIsNeverSilentlyAccepted() {
        assertEquals("sindhii", CompositionChoice.automatic(listOf(Candidate("سنڌي", CandidateKind.CORRECTION)), "sindhii"))
    }

    @Test fun completionAndPredictionsRequireSelection() {
        for (kind in listOf(CandidateKind.COMPLETION, CandidateKind.NEXT_WORD, CandidateKind.PHRASE)) {
            assertEquals("si", CompositionChoice.automatic(listOf(Candidate("سنڌي", kind)), "si"))
        }
    }

    @Test fun unambiguousBaseSpellingCommitsWithoutWaitingForSuggestions() {
        assertEquals("سنڌي", CompositionChoice.automatic(listOf(
            Candidate("سنڌي", CandidateKind.EXACT), Candidate("sindhi", CandidateKind.RAW)
        ), "sindhi"))
    }

    @Test fun ambiguousConversionAcceptsBestSindhiRatherThanLeavingRoman() {
        assertEquals("ت", CompositionChoice.automatic(listOf(
            Candidate("ت", CandidateKind.TRANSLITERATION), Candidate("ٽ", CandidateKind.TRANSLITERATION)
        ), "t"))
    }

    @Test fun duplicateKindsForSameSpellingAreUnambiguous() {
        assertEquals("سنڌي", CompositionChoice.automatic(listOf(
            Candidate("سنڌي", CandidateKind.EXACT), Candidate("سنڌي", CandidateKind.TRANSLITERATION)
        ), "sindhi"))
    }

    @Test fun exactWordTakesPrecedenceOverCharacterFallbacks() {
        assertEquals("سنڌي", CompositionChoice.automatic(listOf(
            Candidate("سنڌي", CandidateKind.EXACT), Candidate("سندهي", CandidateKind.TRANSLITERATION)
        ), "sindhi"))
    }

    @Test fun competingExactWordsAcceptRankedDefault() {
        assertEquals("س", CompositionChoice.automatic(listOf(
            Candidate("س", CandidateKind.EXACT), Candidate("ص", CandidateKind.EXACT)
        ), "s"))
    }

    @Test fun engineFailurePreservesRomanText() {
        assertEquals("myname", CompositionChoice.automatic(emptyList(), "myname"))
    }

    @Test fun broadDictionaryCandidateBeatsMechanicalFallback() {
        assertEquals("تعليم", CompositionChoice.automatic(listOf(
            Candidate("تعليم", CandidateKind.DICTIONARY), Candidate("تاليم", CandidateKind.TRANSLITERATION)
        ), "taleem"))
    }

    @Test fun completeUserConversationsCommitWordByWord() {
        fun resource(name: String) = javaClass.classLoader!!.getResource(name)!!.readText(Charsets.UTF_8)
        val engine = org.sindhipinyin.engine.CandidateEngine(resource("mapping.tsv"), resource("lexicon.tsv"), resource("phrases.tsv"), resource("dictionaries/sd.dic"), resource("conversations.tsv"))
        fun conversation(input: String): String {
            var previous = ""
            return input.split(' ').joinToString(" ") { roman ->
                CompositionChoice.automatic(engine.candidates(roman, org.sindhipinyin.engine.RomanMode.EVERYDAY, previous), roman).also { previous = (previous + " " + it).trim().split(' ').takeLast(4).joinToString(" ") }
            }
        }
        assertEquals("توهان جو نالو ڇا آهي", conversation("twan jo nalo sha ahe"))
        assertEquals("توهان ڪٿي رهندا آهيو", conversation("twan kathe rahnda ahyo"))
        assertEquals("مان قمبر سنڌ ۾ رهندو آهيان", conversation("ma Qambar sindh ma rahndo ahyna"))
    }
}
