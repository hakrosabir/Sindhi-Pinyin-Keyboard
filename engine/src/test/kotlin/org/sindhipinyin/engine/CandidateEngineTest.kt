package org.sindhipinyin.engine

import org.junit.Assert.*
import org.junit.Test
import java.text.Normalizer
import java.util.Locale

class CandidateEngineTest {
    private val mapping = resource("mapping.tsv")
    private val engine = CandidateEngine(mapping, resource("lexicon.tsv"), resource("phrases.tsv"))

    @Test fun exactSeedWordRanksFirst() {
        val candidates = engine.candidates("sindhi", RomanMode.EVERYDAY)
        assertEquals(Candidate("سنڌي", CandidateKind.EXACT, "sindhi"), candidates.first())
        assertEquals(CandidateKind.RAW, candidates.last().kind)
        assertEquals("sindhi", candidates.last().text)
    }

    @Test fun everydayNormalizesCaseButLiteralKeepsCase() {
        val candidates = engine.candidates("SiNdHi", RomanMode.EVERYDAY)
        assertEquals("سنڌي", candidates.first().text)
        assertEquals("SiNdHi", candidates.last().text)
    }

    @Test fun everydayNormalizationDoesNotDependOnTurkishLocale() {
        val old = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals("سنڌي", engine.candidates("SINDHI", RomanMode.EVERYDAY).first().text)
        } finally { Locale.setDefault(old) }
    }

    @Test fun strictDistinguishesLongVowelAndCase() {
        assertEquals("سنڌي", engine.candidates("sindhī", RomanMode.SCHOLARLY).first().text)
        assertFalse(engine.candidates("sindhi", RomanMode.SCHOLARLY).any { it.kind == CandidateKind.EXACT })
        assertFalse(engine.candidates("Sindhī", RomanMode.SCHOLARLY).any { it.kind == CandidateKind.EXACT })
    }

    @Test fun strictAcceptsCanonicallyEquivalentCombiningMarks() {
        val key = Normalizer.normalize("sindhī", Normalizer.Form.NFD)
        assertEquals("سنڌي", engine.candidates(key, RomanMode.SCHOLARLY).first().text)
        assertEquals("ٻ", engine.candidates("b\u0324", RomanMode.SCHOLARLY).first().text)
    }

    @Test fun retroflexDoesNotCollapseIntoDental() {
        assertEquals("ت", engine.candidates("t", RomanMode.SCHOLARLY).first().text)
        val retroflex = engine.candidates("ṭ", RomanMode.SCHOLARLY).map { it.text }
        assertTrue("ٽ" in retroflex)
        assertFalse("ت" in retroflex)
    }

    @Test fun homographsStaySelectableWithinTopThree() {
        val results = engine.candidates("kh", RomanMode.SCHOLARLY)
        assertTrue(results.take(3).any { it.text == "ک" })
        assertTrue(results.take(3).any { it.text == "خ" })
    }

    @Test fun separatorPreventsDigraphConsumption() {
        assertEquals("ش", engine.candidates("sh", RomanMode.SCHOLARLY).first().text)
        assertEquals("سھ", engine.candidates("s|h", RomanMode.SCHOLARLY).first().text)
        assertEquals("بب", engine.candidates("b|b", RomanMode.EVERYDAY).first().text)
    }

    @Test fun completionsAndTyposAreLabeled() {
        assertTrue(engine.candidates("sind", RomanMode.EVERYDAY).any {
            it.text == "سنڌي" && it.kind == CandidateKind.COMPLETION
        })
        assertTrue(engine.candidates("sindih", RomanMode.EVERYDAY).any {
            it.text == "سنڌي" && it.kind == CandidateKind.CORRECTION
        })
        assertFalse(engine.candidates("sindih", RomanMode.SCHOLARLY).any { it.kind == CandidateKind.CORRECTION })
    }

    @Test fun previousWordProducesNextWordAndShortPhrase() {
        assertTrue(engine.candidates("", RomanMode.EVERYDAY, "سنڌي").any {
            it.text == "ٻولي" && it.kind == CandidateKind.NEXT_WORD
        })
        assertTrue(engine.candidates("", RomanMode.EVERYDAY, "توهان").any {
            it.text == "چڱا آهيو" && it.kind == CandidateKind.PHRASE
        })
        assertTrue(engine.candidates("", RomanMode.EVERYDAY).isEmpty())
    }

    @Test fun learningBoostsOnlyExplicitInputOrMatchingContext() {
        val learned = listOf(LearnedEntry("sindhi", "سنڌ", "توهان", 25))
        assertEquals("سنڌ", engine.candidates("sindhi", RomanMode.EVERYDAY, learned = learned).first().text)
        assertTrue(engine.candidates("", RomanMode.EVERYDAY, learned = learned).isEmpty())
        assertTrue(engine.candidates("", RomanMode.EVERYDAY, "توهان", learned).any { it.text == "سنڌ" })
    }

    @Test fun oversizeInputIsPreservedWithoutTransliteration() {
        val long = "x".repeat(300)
        assertEquals(listOf(Candidate(long, CandidateKind.RAW, long)), engine.candidates(long, RomanMode.EVERYDAY))
    }

    @Test fun candidatesAreBoundedUniqueAndKeepLiteral() {
        val candidates = engine.candidates("s".repeat(40), RomanMode.EVERYDAY)
        assertTrue(candidates.size <= CandidateEngine.MAX_CANDIDATES)
        assertEquals(candidates.size, candidates.map { it.text }.distinct().size)
        assertEquals(CandidateKind.RAW, candidates.last().kind)
    }

    @Test fun rawUnknownInputCannotBeLost() {
        assertEquals("🙂", engine.candidates("🙂", RomanMode.EVERYDAY).single().text)
        assertEquals("🙂", engine.candidates("🙂", RomanMode.EVERYDAY).last().text)
    }

    @Test fun invalidImportRejectedWithoutInstallingIt() {
        assertNull(CandidateEngine.validateMapping(mapping))
        assertNotNull(CandidateEngine.validateMapping(mapping.replace("BOTH\tb\tب", "OTHER\tb\tب")))
        assertNotNull(CandidateEngine.validateMapping(mapping.replace("BOTH\tb\tب", "BOTH\tb\t\u202Eب")))
        assertNotNull(CandidateEngine.validateMapping(mapping.replace("BOTH\tb\tب", "BOTH\tb\t<script>")))
        assertNotNull(CandidateEngine.validateMapping(mapping + "\nBOTH\tb\tب\t90\tduplicate"))
        assertNotNull(CandidateEngine.validateMapping(mapping.replace("\t90\t", "\t100\t")))
    }

    @Test fun customMappingControlsFallback() {
        val custom = "mode\troman\ttext\tpriority\tnote\nBOTH\tx\tٻ\t90\tCommunity proposed"
        val imported = CandidateEngine(custom, "mode\troman\ttext\tfrequency\tnote", "context\troman\ttext\tfrequency\tnote")
        assertEquals("ٻ", imported.candidates("x", RomanMode.EVERYDAY).first().text)
    }

    @Test fun seedSmokeEvaluationReportsObservedFixtureRates() {
        val report = SeedEvaluation.evaluate(engine)
        println(report)
        assertEquals(12, report.total)
        assertTrue(report.top1 > 0)
        assertTrue(report.top3 >= report.top1)
    }

    private fun resource(name: String): String = javaClass.classLoader!!.getResource(name)!!.readText(Charsets.UTF_8)

    @Test fun broadDictionarySupportsWordsOutsideTheSeed() {
        val broad = CandidateEngine(mapping, resource("lexicon.tsv"), resource("phrases.tsv"), resource("dictionaries/sd.dic"))
        assertTrue(broad.dictionarySize > 10000)
        for ((roman, script) in listOf("taleem" to "تعليم", "mehnat" to "محنت", "mohabbat" to "محبت", "insan" to "انسان")) {
            val candidates = broad.candidates(roman, RomanMode.EVERYDAY)
            assertTrue("$roman: $candidates", candidates.any { it.text == script && it.kind == CandidateKind.DICTIONARY })
        }
        assertFalse(broad.candidates("taleem", RomanMode.SCHOLARLY).any { it.kind == CandidateKind.DICTIONARY })
    }

    @Test fun everyUnmarkedRomanLetterHasEverydayFallback() {
        for (letter in 'a'..'z') {
            assertTrue("Unmapped letter $letter", engine.candidates(letter.toString(), RomanMode.EVERYDAY).any { it.kind == CandidateKind.TRANSLITERATION && it.text.none { c -> c in 'a'..'z' } })
        }
    }

    @Test fun maUsesExplicitPreviousWordContext() {
        assertEquals("مان", engine.candidates("ma", RomanMode.EVERYDAY).first().text)
        assertEquals("۾", engine.candidates("ma", RomanMode.EVERYDAY, "سنڌ").first().text)
        assertEquals("مان", engine.candidates("ma", RomanMode.EVERYDAY, "آهي").first().text)
    }
}
