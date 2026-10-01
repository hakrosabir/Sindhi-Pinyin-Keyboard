# 0.2.0-alpha: unrestricted writing and conversation fixes

The first version required a single unambiguous candidate before Space would convert. Everyday Sindhi has many spelling alternatives, so this often left ordinary words in Roman text. It also allowed prefix completions to crowd out fallback conversion. Both conditions have been changed.

## Changed behavior

- Sindhi preview updates during Roman typing; Space commits the highest ranked exact word, dictionary match, or mechanical conversion, in that order. Longer completions and typo corrections still require selection.
- The candidate accepted by Space is moved to the front and marked **✓ Space**. You can select another candidate or the literal Roman text.
- A mechanical fallback is reserved even when many completion candidates exist. All 26 unmarked Latin letters now have a conversion rule in everyday mode. Unknown words are not restricted to the seed lexicon; spelling still may need correction.
- A 10,000+ word offline script dictionary supplies additional phonetic candidates through an indexed consonant/vowel heuristic. It does not contain authoritative Roman pronunciations or real usage frequencies. Related consonants can produce incorrect suggestions; choose alternatives when needed.
- Additional everyday aliases include the user's `twan`, `sha`, `kathe`, `rahnda`, `ahyo`, `rahndo`, `ahyna`, `ma` and `Qambar` examples. The intended Sindhi spellings below were inferred from the supplied English meaning and still need native-speaker confirmation.
- Same-session context distinguishes `ma` as مان initially versus ۾ after سنڌ, قمبر, ڪراچي or پاڪستان. These are explicit draft context rules, not a general grammar model.
- In-memory local dictionary caching lets selected/saved words affect the automatic candidate without doing SQLite I/O on every key. Erasing or disabling learning invalidates personal suggestions.

## User examples covered by regression tests

| Roman input | Expected Sindhi words |
| --- | --- |
| `twan jo nalo sha ahe` | توهان جو نالو ڇا آهي |
| `twan kathe rahnda ahyo` | توهان ڪٿي رهندا آهيو |
| `ma Qambar sindh ma rahndo ahyna` | مان قمبر سنڌ ۾ رهندو آهيان |

These tests verify word-by-word composition and Space behavior; they do not prove all dialects/spellings or unseen conversations. This version is not equivalent to Sogou's trained language model and does not yet decode arbitrarily long unspaced Roman sentences as one composition.

## Dictionary provenance

[Sindhi Spellchecking Dictionary, 2011.01.10](https://extensions.openoffice.org/en/project/sindhi-spellchecking-dictionary.html), maintained as `nizamani` on the source page, is distributed there under GNU LGPL 2.1. The original package's `sd.dic`, `sd.aff`, description, source attribution and license are included unchanged in `engine/src/main/resources/dictionaries/` and APK assets. Its description mentions Urdu dictionary ancestry; this is not presented as a fully curated Sindhi-only pronunciation corpus. The original files remain editable/rebuildable, and their license stays separate from the app code. The in-app credits provide attribution and the full license.

## Installation

Install `dist/Sindhi-Pinyin-Keyboard-0.2.0-alpha.apk` over 0.1.0. Application ID and signing key are unchanged; versionCode increases to 2. Do not uninstall unless you intend to erase local settings/dictionary. Select **Everyday spellings** and **Roman Sindhi** mode for the examples above. Updates preserve imported mappings; restore the bundled mapping in Settings if testing the new default letter table.

Existing architecture and mapping documents describe the initial prototype unless superseded here. Scholarly input remains a strict draft and does not use fuzzy word-list retrieval.

## Build verification

Gradle engine tests, app unit tests, debug APK assembly and Android lint completed successfully. **30 unit tests passed**, including the three complete user-example sequences and dictionary retrieval outside the seed lexicon. These are regression checks, not independent language-accuracy measurements. The APK signature and ZIP alignment were verified; package and versionCode were inspected. The updated APK has not yet been exercised on the Xiaomi.

- APK size: 1,087,971 bytes
- SHA-256: `ec93c3e2fe6189235baa5da6b25adc255cab0d240aaa50c6ce559b01d097dbff`
- Source data, attribution and LGPL license are included in the final package.
