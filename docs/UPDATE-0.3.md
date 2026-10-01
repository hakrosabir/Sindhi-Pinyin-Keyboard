# Version 0.3.0-alpha — conversation candidates

Install `dist/Sindhi-Pinyin-Keyboard-0.3.0-alpha.apk` over the existing app. The package and development signing key are unchanged. Do not uninstall: updating preserves settings and your local dictionary.

## Using the update

- The header shows your Roman input and the proposed Sindhi word together.
- Space accepts the current word. Sentence suggestions require a tap.
- The upper candidate row contains sentence continuations; the lower row contains word alternatives, with Roman spellings and category labels.
- Swipe either row to see more choices. Tap ▾ to browse the expanded candidate panel; tap ▴ to return to the letter keys.
- Up to 16 candidates are retained. The literal Roman input remains available.
- Context uses at most four words typed during the current field session. Moving the cursor, changing fields or closing the keyboard clears this temporary context.
- Optional local learning stores completion aliases rather than accidentally teaching a whole sentence as the meaning of a short prefix. Learned multiword suggestions always require a tap.

## Phone checks

1. In the app practice field, type `twan` and Space, then `jo` and Space. Type `na`: look for the sentence choice `نالو ڇا آهي`. Tap it. Expected full text: `توهان جو نالو ڇا آهي`, with no repeated `توهان جو`.
2. Repeat `twan jo nalo sha ahe` using Space after every word. Each Space must accept only one word.
3. Try `twan kathe rahnda ahyo` and `ma Qambar sindh ma rahndo ahyna`.
4. Open ▾, select an alternative, then continue typing. The letter keys should return. Check dark mode and landscape.
5. Repeat in unsent WhatsApp and Messages drafts. Check deletion, moving the cursor, switching to English and switching keyboards with the globe.
6. Check a password field: predictions and learning should be disabled. No permission or battery exemption is required by this update.

## Accuracy and limits

This release adds 26 authored, aligned conversation templates and context-based ranking to the existing 10,000+ entry offline dictionary. Templates and Roman spellings are explicitly marked draft in `engine/src/main/resources/conversations.tsv`. They require Sindhi speaker review. They are not a collected chat corpus or a trained generative model.

This implements more of the Pinyin interaction pattern; it does not establish Sogou-level accuracy or arbitrary sentence understanding. Unknown words still have character conversion and selectable alternatives. Broad top-1/top-3 accuracy, keystroke savings, phone performance and reliability need independent measurement. No phone was connected for this update, so Xiaomi and real-app tests remain for the user to run.

Offline privacy behavior is unchanged: no Internet permission, analytics or raw keystroke logging; optional local aggregate learning remains off by default. Dictionary attribution and LGPL materials are preserved; see `UPDATE-0.2.md`.
