# UI and interaction specification

This describes the MVP interaction and the acceptance criteria for polish. It is not a set of screenshots from a tested Xiaomi device. All Sindhi instructional translations and seed examples require native-speaker review before public release.

## First launch

The launcher uses **Home**, **Guide**, **Dictionary**, and **Settings** tabs. Home explains Roman-to-Sindhi candidate typing and offline operation, with setup and practice actions. Setup opens Android's own IME settings/picker; enabling cannot be silently done by the app. Explain that Android shows a standard keyboard trust warning because a selected keyboard handles what the user types. Link the full privacy text from the same screen.

Provide **Everyday** and **Scholarly (draft)** mode help. Everyday tolerates common alternative spellings; scholarly mode exposes precise project tokens and marks the standard as pending review. Do not call the convention an official Sindhi “Pinyin” standard. The working app name is an analogy to candidate-based input.

## Keyboard layout

```text
┌─────────────────────────────────────────────┐
│ mode/status          composing Roman input   │
├─────────────────────────────────────────────┤
│ Sindhi candidate • kind | candidate • kind … │
├─────────────────────────────────────────────┤
│ q   w   e   r   t   y   u   i   o   p       │
│   a   s   d   f   g   h   j   k   l         │
│ shift  z   x   c   v   b   n   m   delete   │
│ 123  mode  globe   space   punctuation enter │
└─────────────────────────────────────────────┘
```

The diagram is schematic; actual key sizing and scroll behavior must be verified on-device. Roman key order stays LTR even when Android's system language is RTL. Candidate text uses RTL/first-strong direction and ordinary Unicode logical order. Rank order remains stable; labels and positions must be discoverable with TalkBack. Mixed Roman hints should be separate views where possible so punctuation does not reorder a Sindhi word.

The mode key cycles this app's Roman Sindhi, direct Sindhi script, and English modes. A separate globe action opens the Android input-method picker for other installed keyboards. The direct-script layout has two letter pages and a diacritic/auxiliary row. The Roman header's **ā…** control opens scholarly characters/diacritics. English provides ordinary literal input, not an English prediction model. Numeric fields get the symbols/numbers page; passwords start with literal English input without suggestions or learning. Settings, mode, globe and delete remain discoverable.

## Candidate behavior

- Show the source Roman input and candidate types clearly. “Exact” means a match to a stored input form, not a guarantee of linguistic correctness.
- Tapping a candidate commits that word or phrase and appends one space. Prefix completion and correction are offered for explicit choice. Phrases must be recognizably multiword and must not silently replace already committed text.
- The current composition shows Sindhi only when there is one distinct exact/mapping output. If there are multiple eligible outputs, it remains Roman until the user picks the intended Sindhi form. Space/Enter/punctuation use that same conservative rule, so an unknown or ambiguous form can remain Roman. The literal Roman candidate is also available explicitly. Corrections, completions, learned candidates and predictions always need a tap.
- Next-word suggestions use only the current session context and the optional local transition table. Moving the cursor or switching fields resets that context.
- Clear candidates in private/structured fields. A concise status may say “Suggestions off for this field.” Never announce secret characters through a candidate label.

## Practice and mapping guide

Show a searchable/readable mapping table with Roman token, Sindhi output, mode, and review status. Explain that different everyday spellings may lead to the same word and one spelling may have several possible outputs. The tutorial should walk through typing, choosing a candidate, deleting, switching to English, using the globe, and changing modes. Include diacritic and strict-token entry instructions.

Practice is local and must not create a saved transcript or opt users into learning. Use reviewed synthetic examples, never a user's messages. Practice inside the app helps learn the rules; it does not substitute for testing the installed IME in real apps.

## Settings

| Control | Expected behavior |
| --- | --- |
| Theme | Follow system, light, or dark; persist choice. |
| Vibration and sound | Explicit controls; respect Android system settings and silent/DND behavior. Defaults should avoid surprising feedback. |
| Romanization mode | Explain the everyday/strict distinction and draft status before selection. |
| Local learning | Off by default; explain saved committed words/input aliases/transitions before enabling. Manual dictionary entries remain available when learning is off. |
| Delete learned data | Names exactly what is deleted; clear only learned records, or clear all dictionary records, and disable automatic learning. |
| Custom dictionary | Add/review/delete words or short phrases; distinguish manually added entries from automatically learned entries. |
| Mapping import/reset | Import through the system document picker, validate the complete file, show errors, and provide a bundled-mapping reset. |
| Privacy and help | Available without enabling the IME or going online. |

Mapping import/export uses TSV through Android's document picker. It does not export the learned dictionary or a typing transcript. Invalid imports must leave the working mapping intact. A user-selected cloud file provider is a separate privacy boundary.

## Accessibility and display acceptance

Use real buttons with readable labels/content descriptions, visible focus and pressed states, sufficient contrast, scalable text, and a logical screen-reader traversal order. Aim for at least 48dp interactive targets; where compact key rows cannot achieve that width, evaluate expanded hit areas and alternative layouts. Candidate touch height must remain comfortable at large font sizes. Do not claim full accessibility conformance until TalkBack, switch access, large text, and contrast checks pass. [Android accessibility guidance](https://developer.android.com/guide/topics/ui/accessibility/apps).

Leave space for navigation/gesture insets. Test landscape and large-screen resizing, dark mode changes while the keyboard is visible, 200% font scale, and display-size scaling. Android fonts normally shape joined Arabic-script letters; review every Sindhi letter, ligature sequence, and combining mark on the Xiaomi and in receiving apps. Use a licensed bundled font only if testing identifies a real rendering need.

## Visual direction for the next polish pass

Use a calm teal accent, neutral light/dark surfaces, rounded keys with clear boundaries, and generous candidate text. Keep decorative cultural motifs away from character glyphs and touch boundaries. The language and candidate choices should be more prominent than branding. Four useful release screenshots are Roman-to-Sindhi candidates, script/English switching, the mapping tutorial, and local privacy controls; capture them from the actual signed app with synthetic text.
