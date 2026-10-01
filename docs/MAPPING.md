# Roman Sindhi mapping specification (draft v0.1)

## Status and evidence

**This is a research MVP mapping, not an authoritative Roman Sindhi standard.** Everyday mode uses explicitly proposed aliases. Scholarly mode is an experimental, strict, case-sensitive token mode with diacritics; it is **not certified as complete ALA-LC romanization**. A Sindhi linguist must approve the letter distinctions, short vowels, word-initial vowels, aspiration, nasalization, hamza, inflection and seed words before a public accuracy claim or production release.

Primary references consulted on 2026-09-28:

- [ALA-LC Sindhi table, 2024](https://www.loc.gov/catdir/cpso/romanization/sindhi.pdf) is the proposed scholarly reference. Its indexed text was available, but direct PDF access and a download returned HTTP 403/Cloudflare; extracted text visibly conflates several Sindhi glyphs and combining marks. This build does not claim a glyph-by-glyph verification of that document.
- [Library of Congress table index](https://www.loc.gov/catdir/cpso/roman) lists Sindhi 2024 as current and says earlier versions are reference only.
- [Library of Congress source-document page](https://www.loc.gov/catdir/cpso/romansource.html) recommends using its [Sindhi Word source](https://www.loc.gov/catdir/cpso/romanization/sindhi.doc) when copying table data. Obtain and visually compare that original before replacing draft labels with verified ones.
- [Unicode CLDR 45 Sindhi locale](https://unicode.org/cldr/charts/45/summary/sd.html) provides an independently readable Arabic-script character inventory, including digraphs `جھ`, `گھ`, both `ه` and `ھ`, and auxiliary `َ ُ ِ ئ`. CLDR is character-inventory evidence, not evidence that the proposed Roman input aliases are accepted Sindhi spelling conventions.
- [LoC Extended Arabic character table](https://www.loc.gov/marc/specifications/codetables/ExtendedArabic.html) identifies Sindhi-specific Unicode characters, including `۽` U+06FD. This is encoding evidence, not language-model training data.

The mapping file contains the complete executable specification and a provenance/status note on every row. All reverse-input choices are draft assumptions. No corpus-derived frequencies or professionally reviewed dictionary are included.

## Two input modes

Everyday mode lowercases Roman input with `Locale.ROOT`, applies Unicode NFC, accepts variants such as `sindhi`, `salam`, `shukriyo` and `shukriya`, and keeps the exact literal input as the last candidate. It never removes diacritics or silently rewrites the literal candidate. Case-folding is for lookup only.

Scholarly mode applies NFC while preserving case and marks. `sindhī` is a draft exact seed spelling; `sindhi` and `Sindhī` are different inputs. Canonically equivalent precomposed and decomposed marks match. Fuzzy correction and everyday phrase-prefix expansions are disabled. The mode name describes strict matching, not verified compliance with a published scholarly system. Learned entries must be partitioned by the caller's mode so an everyday alias does not leak into scholarly matches.

The shared mode-independent next-word table is contextual Sindhi text; a previous committed word can still produce next-word suggestions in either mode. The engine itself does not read another application's text or retain history.

## Common rules and proposed aliases

The following summarizes the shipped data; `engine/src/main/resources/mapping.tsv` is authoritative for execution. Every entry below remains subject to expert validation.

| Input category | Everyday input → output | Scholarly draft input → output |
| --- | --- | --- |
| Basic consonants | `b ت` is **not** a rule; see explicit sequence below | Same basic consonants |
| Basic sequence | `b→ب, t→ت, p→پ, j→ج, d→د, r→ر, z→ز, s→س` | Same; no case folding |
| Further basics | `f→ف, q→ق, k→ڪ, g→گ, l→ل, m→م, n→ن, v→و, h→ھ, y→ي` | Same; heh form needs review |
| Aspirates | `bh→ڀ, th→ٿ, jh→جھ, dh→ڌ, ph→ڦ` | Same draft tokens |
| Implosives | `bb→ٻ, jj→ڄ, dd→ڏ, gg→ڳ` | `b̤→ٻ, ẑ→ڄ, d̤→ڏ, g̈→ڳ` |
| Retroflex | `tt→ٽ, tth→ٺ, ddd→ڊ, ddh→ڍ, rr→ڙ, nn→ڻ` | `ṭ→ٽ, ṭh→ٺ, ḍ→ڊ, ḍh→ڍ, ṛ→ڙ, ṇ→ڻ` |
| Nasals | `ny→ڃ, ng→ڱ` | `ñ→ڃ, ṅ→ڱ` |
| Affricates | `ch/c→چ, chh→ڇ` | `c→چ, ch→ڇ` |
| Ambiguous digraphs | `kh→ک/خ, gh→گھ/غ` | Same candidate ambiguities |
| Spelling alternatives | `s→س/ث/ص, z→ز/ذ/ض/ظ, t→ت/ط, h→ھ/ح/ه` | Draft marked `s̱→ث, ṣ→ص, ẕ→ذ, z̤→ض, ẓ→ظ, ḥ→ح`; `ṭ` also offers `ط` |
| Long-vowel input | `aa→ا, ii/ee→ي, uu/oo→و` | `ā→ا, ī→ي, ū→و` |
| Short-vowel fallback | `a→ا, i/e→ي, u/o→و` | `a→َ, i→ِ, u→ُ`; `e→ي, o→و` |
| Other explicit escapes | `aaa→آ, '→ء, ain→ع` | `'→ء, ʻ→ع, ai→َي, au→َو` |

The everyday short-vowel fallback overgenerates letters. The word lexicon is checked before character conversion so `sindhi` can produce conventional `سنڌي` instead of mechanical `سينڌي`. This is why a character mapping alone cannot provide high-accuracy Sindhi input. Whole-word entries such as `ain→۽` intentionally take precedence over the explicit character escape `ain→ع`; both remain candidates.

Repeated-letter aliases conflict with consonant doubling. A vertical bar forces a token boundary without appearing in Sindhi output: `bb→ٻ`, `b|b→بب`; `sh→ش`, `s|h→سھ`. The literal candidate preserves the bar if selected. This delimiter is a project input convention, not a linguistic standard. It is available on the scholarly letters page.

Do not silently normalize `ھ` to `ه`, Sindhi `ڪ` to Urdu/Persian `ک`, or Arabic `ي` to Persian `ی`: NFC does not perform these replacements, and they have distinct code points. Outputs are logical-order Unicode letters/marks, not presentation forms or reversed strings. Android renders shaping, bidi, and ligatures through its text system and font stack. Rendering on the Xiaomi remains a device test, not an engine guarantee.

## Editable TSV formats

UTF-8 text; an optional BOM is accepted. Lines beginning with `#` are comments. No quoting syntax is used; each data row must contain exactly five tab-separated fields.

```text
mode<TAB>roman<TAB>text<TAB>priority<TAB>note
EVERYDAY<TAB>bb<TAB>ٻ<TAB>95<TAB>Community proposed implosive alias
SCHOLARLY<TAB>b̤<TAB>ٻ<TAB>96<TAB>Draft strict token; expert review pending
```

- `mode`: `EVERYDAY`, `SCHOLARLY`, or `BOTH`.
- `roman`: 1–12 UTF-16 units consisting of Latin letters, permitted combining marks, ASCII apostrophe or U+02BB; `|` is reserved for a boundary.
- `text`: 1–12 units of Arabic-script letters or marks. Control characters, bidi formatting characters and presentation forms are rejected. Empty mappings are forbidden.
- `priority`: integer 0–99. Higher is preferred among competing tokenizations. Ranking weights are assumptions, not measured linguistic probabilities.
- `note`: up to 240 units explaining provenance and review status.

At most 1,024 mapping rows and 2,000,000 UTF-16 units are accepted; the Android import path should enforce a tighter byte-read limit before decoding. Duplicate `(effective mode, NFC Roman token, NFC output)` rules are rejected. Conflicting outputs for the same token are allowed and produce candidates. `CandidateEngine.validateMapping(tsv)` returns `null` when valid, otherwise a readable validation error. Construct the new engine successfully before replacing an installed mapping. Reset must restore the bundled TSV.

`lexicon.tsv` uses `mode, roman, text, frequency, note`; `phrases.tsv` uses `context, roman, text, frequency, note`, in that order with literal tabs. The lexicon has 60 draft alias rows (56 everyday and 4 scholarly), including alternate spellings. There are 12 phrase/next-word rows. Frequencies are hand-set 0–1000 ranking weights, not observed counts. Phrase output is the insertion **after** the context, never a replacement for it. The empty context enables typed prefix expansion only; opening a keyboard does not show personal suggestions.

## Engine behavior and limits

```kotlin
CandidateEngine(mappingTsv, lexiconTsv, phrasesTsv)
    .candidates(roman, RomanMode.EVERYDAY, previous = "", learned = emptyList())
```

The result has at most eight distinct text candidates. Non-empty input always ends with `CandidateKind.RAW` containing the exact original input. Inputs exceeding 64 UTF-16 units or containing unsupported control/surrogate characters receive only the literal candidate; the engine never truncates text to commit.

The deterministic pipeline is: exact lexicon entries and locally selected words → prefix completions → everyday phrase prefixes → bounded character-transliteration beam → one-edit/adjacent-transposition correction suggestions → sort, deduplicate and append literal input. It scores exact matches above completions; corrections are suggestions only. The service must prefer the exact-candidate group for automatic word-boundary commits and require selection when that group is ambiguous. It must never automatically commit a correction. Scholarly mode does not perform fuzzy correction.

Character search uses a first-character index, at most 12 token matches per position, a beam width of 12, and at most 64 input units. It returns at most four character-conversion candidates. Lexicon size is capped at 10,000 rows; phrases at 2,000; caller-supplied learned entries at 512. The shipped seed is small. Load/parse files once off the UI thread; perform SQLite I/O off the UI thread; run candidate updates with cancellation/stale-result guards in the IME. No network, model runtime, log, timer, or background worker exists in this engine.

Learning is an input to a pure function. The engine does not decide what to store. The caller must pass only explicit candidate choices made while learning is enabled, partition by mode, clear editor context on editor/selection changes, and suppress learned entries for password/private/no-personalized-learning fields. The API does not justify collecting all typed words or surrounding application text.

## Verification and language research

`CandidateEngineTest` covers case handling, NFC equivalence, dental/retroflex separation, ambiguous digraphs, delimiters, exact words, completions, typo labels, contextual suggestions, learning, literal retention, bounded output and hostile imports. `SeedEvaluation` prints observed top-1/top-3 counts for 12 engineering fixtures, explicitly marked **SEED SMOKE ONLY**. Fixtures overlap the dictionary; a pass is not a language-accuracy estimate.

For a defensible accuracy study, collect a consented, de-identified, licensed dataset of Roman/Sindhi pairs from different writers and dialects. Split by writer and text source before tuning. Freeze mapping/version and seed dictionary; report sample size, ambiguity policy, top-1/top-3, out-of-vocabulary rate, exact-normalization rules, per-dialect results and bootstrap intervals. Maintain separate everyday and scholarly sets. Have two Sindhi reviewers adjudicate alternatives, nasalization, vowels, diacritics and spelling; include names, mixed English, numbers, URLs and unknown words. Do not use private chat logs without explicit informed consent.

Measure keystroke savings in a crossover typing study against the same device's direct Sindhi layout: count every character key, candidate selection, correction, space and mode switch. Report `1 − Roman-condition actions / direct-layout actions` with error rate and task time; values can be negative. Do not claim savings from Roman character counts alone. Device latency, memory, battery, crashes and ANRs require separate instrumentation and Xiaomi testing.

Before naming the mode “ALA-LC strict,” verify the 2024 Word source in an appropriate font, encode every letter and modifier unambiguously, implement the referenced Urdu rules and context-sensitive vowel/hamza rules, settle case behavior, and obtain linguist sign-off with versioned tests. Until then, show “Scholarly (experimental)” in user-facing settings.
