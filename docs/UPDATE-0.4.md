# 0.4.0-alpha: stable numbered candidates

Install `dist/Sindhi-Pinyin-Keyboard-0.4.0-alpha.apk` over the existing app. Keep the previous app installed to preserve local settings and dictionary entries.

## Changes

The previous implementation rebuilt the suggestion controls twice per keystroke and cleared next-word candidates before a worker returned results. Its phrase row also appeared and disappeared, changing keyboard height. This update publishes one cached candidate result for each input change, keeps the suggestion area's height fixed, and skips rebuilding identical candidate controls. Space uses the displayed default rather than re-ranking at commit time. Dictionary loading and learning remain off the UI thread; candidate ranking uses the in-memory engine.

Word choices now have numbers 1, 2, 3, 4 and onward. Tap the numbered word you want. Four equally sized tiles fit across the main row; swipe for more or open the expanded panel. These are touch labels, not hardware-number shortcuts. Sentence choices remain in the row above. Numbers use LTR direction and Sindhi words use isolated RTL text to prevent reordered labels.

Added 139 previously missing Roman-to-word aliases from the user's supplied list. Existing matching aliases are retained. Ambiguous meanings are separate choices: `ma` includes مان and ماءُ; `hi` includes هي and هِي. All added aliases have regression checks for top-four visibility. Their source is the user, not an independently validated dictionary. General phonetic equivalences are not applied indiscriminately to distinct Sindhi consonants. Exact words no longer suppress phonetic dictionary alternatives.

English now has an authored offline starter vocabulary, prefix completions, one-edit/transposition spelling alternatives, common contractions, limited next-word suggestions and automatic sentence capitalization. Space preserves the typed English word; corrections require selection. The pronoun `i` becomes `I`. No English typing is logged or learned. This is basic assistance, not a full commercial English prediction model.

Fonts use 20sp word choices, 12sp Roman captions, 18sp phrase text and a smaller preview. Numbered tiles use consistent widths. Portrait key rows are 50dp; landscape rows are 40dp. Comma and full stop are available directly. Punctuation removes a space the keyboard just inserted, after checking that the adjacent character is still a space.

## Verify on Xiaomi / WhatsApp

1. Type `ma`, stop for five seconds, and confirm the choices stay visible. Tap the numbered ماءُ choice; only that choice should enter the text. Repeat with مان.
2. Try `hi`, `ahe`, `pany`, `ktab`, `qalm`, `mahn`, `galain` and `bbudhn`. Swipe and expand the choices. Confirm numbers and Sindhi glyphs are readable.
3. Type quickly, pause, then tap choices. Repeat after deleting a letter. Confirm the keyboard height stays constant and suggestions do not vanish while your finger is approaching them.
4. Type `twan jo na`; choose the remaining sentence. Repeat using Space after every complete word. Check there are no repeated words.
5. Switch to English: try `hel` → hello, `teh` → the, and `dont` → don't. Space must preserve an uncorrected typo. Try `thank` + Space and choose `you`. Test sentence capitalization and punctuation after selecting a word.
6. Repeat in WhatsApp, Messages and the app practice field. Test cursor movement, field changes, orientation, dark mode and a larger Android font setting. Password fields must keep suggestions disabled.

No phone was connected for this update. The regression fixes are implemented and desktop checks are recorded in BUILD_REPORT.md, but the reported Xiaomi symptom has not been reproduced or verified resolved on that device. A new Android instrumentation test checks fixed suggestion height, stable repeated-result views and selecting candidate 2; it is compiled, not device-executed.

Android editor behavior reference: [InputMethodService](https://developer.android.com/reference/android/inputmethodservice/InputMethodService) and [InputConnection](https://developer.android.com/reference/android/view/inputmethod/InputConnection). No network permission, analytics or background polling was added.
