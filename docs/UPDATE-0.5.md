# 0.5.0-alpha — visual refresh

Install the new APK over 0.4.0. The transliteration engine, supplied aliases, candidate ranking, single-word Space behavior and stable candidate update logic are unchanged.

## Design

- Neutral light and dark surfaces with blue accents, rounded key shapes and restrained key elevation.
- The default word has a visible outline, soft blue background and the existing checkmark. Numbers remain beside each choice. Selection is not indicated by color alone.
- Monochrome, locally drawn globe, settings, shift, delete and expand icons replace font-dependent emoji symbols.
- Familiar QWERTY geometry: inset middle row, consistently sized letter keys and wider Shift/Delete controls.
- Wider spacebar, direct punctuation, and a contrasting editor action key (Send, Search, Enter and so on).
- Language switching moves to the top toolbar. Tap **SD / سنڌي / EN** to cycle modes. The bottom globe still opens the installed-keyboard picker.
- Portrait letter-key height is 50dp within a 54dp row. Toolbar controls are 48dp; sentence choices are 48dp high in portrait. Letter-key widths necessarily remain narrower on phones with ten keys across. Landscape keeps compact rows.
- Matching app setup/settings colors, rounded actions and text fields, selected navigation tabs, improved paragraph spacing and dark-mode status-bar contrast.

The design uses familiar mobile keyboard conventions and the guidance in [Apple's virtual keyboard HIG](https://developer.apple.com/design/human-interface-guidelines/virtual-keyboards), [Material foundations](https://m3.material.io/foundations/), and [Android touch-target guidance](https://support.google.com/accessibility/android/answer/7101858). It is an original Android interface, not a claim to reproduce a specific iOS or HyperOS keyboard release. No blur effects, external fonts, network assets or animation loops were added.

## Verification

Calculated foreground/background contrast ratios: light letters 15.54:1, dark letters 10.73:1, light secondary text 5.24:1, dark secondary text 9.52:1, light action 6.41:1, dark action 8.16:1, light selected numbers 5.20:1, dark selected numbers 6.51:1. These cover the listed enabled color pairs, not a complete accessibility audit.

Build, lint and existing tests are recorded in BUILD_REPORT.md. Android UI tests compile, but no phone or emulator is connected for visual execution. Check portrait/landscape, light/dark, larger system text, TalkBack, the new toolbar language control and the wider spacebar on Xiaomi. Confirm that numbered word choices remain stable in WhatsApp as they did in 0.4.
