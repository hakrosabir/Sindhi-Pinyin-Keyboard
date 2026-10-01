package org.sindhipinyin.engine

import org.junit.Assert.*
import org.junit.Test

class ConversationModelTest {
    private fun resource(name: String) = javaClass.classLoader!!.getResource(name)!!.readText()
    private val model = ConversationModel(resource("conversations.tsv"))

    @Test fun completionContainsOnlyUncommittedSuffix() {
        val candidate = model.suggestions("na", "توهان جو", emptySet()).first()
        assertEquals("نالو ڇا آهي", candidate.text)
        assertEquals("nalo sha ahe", candidate.roman)
        assertEquals(CandidateKind.PHRASE, candidate.kind)
    }

    @Test fun suggestionsContinueAfterSpace() {
        assertEquals("ڇا آهي", model.suggestions("", "توهان جو نالو", emptySet()).first().text)
    }

    @Test fun emptyOrUnrelatedContextDoesNotInventContinuation() {
        assertTrue(model.suggestions("", "", emptySet()).isEmpty())
        assertTrue(model.suggestions("", "اڻڄاتل", emptySet()).isEmpty())
    }

    @Test fun exactScriptAliasCanMatchDifferentRomanSpelling() {
        assertTrue(model.suggestions("tawhan", "", setOf("توهان")).any { it.text == "توهان جو نالو ڇا آهي" })
    }

    @Test fun fullEnginePreservesWordChoiceAlongsideSentence() {
        val engine = CandidateEngine(resource("mapping.tsv"), resource("lexicon.tsv"), resource("phrases.tsv"), "", resource("conversations.tsv"))
        val items = engine.candidates("nalo", RomanMode.EVERYDAY, "توهان جو")
        assertTrue(items.any { it.kind == CandidateKind.EXACT && it.text == "نالو" })
        assertTrue(items.any { it.kind == CandidateKind.PHRASE && it.text == "نالو ڇا آهي" })
        assertEquals(CandidateKind.RAW, items.last().kind)
        assertTrue(items.size <= 16)
    }

    @Test fun scholarlyDoesNotUseDraftEverydayTemplates() {
        val engine = CandidateEngine(resource("mapping.tsv"), resource("lexicon.tsv"), resource("phrases.tsv"), "", resource("conversations.tsv"))
        assertFalse(engine.candidates("twan", RomanMode.SCHOLARLY).any { it.text == "توهان جو نالو ڇا آهي" })
    }

    @Test(expected = IllegalArgumentException::class) fun rejectsMisalignedModel() {
        ConversationModel("roman\ttext\tweight\tnote\na b\tا\t10\ttest")
    }

    @Test fun learnedSentenceStaysAnExplicitTapEvenWithShortAlias() {
        val engine = CandidateEngine(resource("mapping.tsv"), resource("lexicon.tsv"), resource("phrases.tsv"))
        val items = engine.candidates("na", RomanMode.EVERYDAY, learned = listOf(LearnedEntry("na", "نالو ڇا آهي", "", 5)))
        assertEquals(CandidateKind.PHRASE, items.first { it.text == "نالو ڇا آهي" }.kind)
    }
}
