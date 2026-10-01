package org.sindhipinyin.engine

/** Engineering smoke fixtures only: seeded examples are not a held-out language accuracy corpus. */
object SeedEvaluation {
    data class Report(val total: Int, val top1: Int, val top3: Int) {
        override fun toString(): String = "SEED SMOKE ONLY: n=$total; top1=$top1/$total; top3=$top3/$total. " +
            "Examples overlap the seed dictionary; these are not general accuracy estimates."
    }

    fun evaluate(engine: CandidateEngine): Report {
        val fixtures = listOf(
            Triple("sindhi", RomanMode.EVERYDAY, "سنڌي"),
            Triple("SiNdHi", RomanMode.EVERYDAY, "سنڌي"),
            Triple("salam", RomanMode.EVERYDAY, "سلام"),
            Triple("kitab", RomanMode.EVERYDAY, "ڪتاب"),
            Triple("pani", RomanMode.EVERYDAY, "پاڻي"),
            Triple("maan", RomanMode.EVERYDAY, "مان"),
            Triple("sindih", RomanMode.EVERYDAY, "سنڌي"),
            Triple("sind", RomanMode.EVERYDAY, "سنڌي"),
            Triple("kh", RomanMode.SCHOLARLY, "خ"),
            Triple("s|h", RomanMode.SCHOLARLY, "سھ"),
            Triple("sindhī", RomanMode.SCHOLARLY, "سنڌي"),
            Triple("b\u0324", RomanMode.SCHOLARLY, "ٻ"),
        )
        var top1 = 0
        var top3 = 0
        for ((roman, mode, expected) in fixtures) {
            val candidates = engine.candidates(roman, mode).filter { it.kind != CandidateKind.RAW }
            if (candidates.firstOrNull()?.text == expected) top1++
            if (candidates.take(3).any { it.text == expected }) top3++
        }
        return Report(fixtures.size, top1, top3)
    }

    @JvmStatic fun main(args: Array<String>) {
        fun resource(name: String): String = SeedEvaluation::class.java.classLoader!!.getResource(name)!!.readText(Charsets.UTF_8)
        println(evaluate(CandidateEngine(resource("mapping.tsv"), resource("lexicon.tsv"), resource("phrases.tsv"))))
    }
}
