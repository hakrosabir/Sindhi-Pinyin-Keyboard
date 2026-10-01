# Test and evaluation plan

Status: the Xiaomi 11 Ultra is not connected. This document is a runnable protocol and result template, not evidence of device compatibility, accuracy, crash-free operation, or Play readiness. Record actual results separately and never convert a proposed threshold into a measured result.

## Build and test record

For every tested artifact record commit/source snapshot, APK SHA-256, application ID, versionCode/versionName, build type, mapping/lexicon versions and hashes, JDK/Gradle/AGP versions, Android SDK level, device model, RAM, Android version, MIUI/HyperOS build and region, security patch, app versions, display/refresh rate, font scale, and battery settings. Use synthetic text only in debug traces, screenshots, bugreports, and memory captures.

Run the root README's build and engine test commands. Once Android SDK tools are installed, run unit tests, Android lint, debug assembly, and the applicable connected/instrumentation tests. A missing SDK or dependency download is an unexecuted build, not a passing test. Test a release-like build for performance because debug/instrumentation overhead can distort it.

## Device matrix

| Environment | Required coverage |
| --- | --- |
| Xiaomi 11 Ultra, actual installed build | All release-blocking rows below; primary compatibility and performance evidence. Do not assume its current OS is HyperOS or Android 17. |
| API 24 emulator | Minimum-version install, IME enablement, input, deletion, script shaping, flags, picker and settings. |
| API 37 emulator/device | Current-target behavior, system insets, light/dark transitions, resizing, input lifecycle, restore exclusions. |
| Small phone and tablet/resizable emulator | Candidate overflow, long strict tokens, landscape, large text, hardware keyboard interaction. |

Android 17 / API 37 is the verified stable major release at this plan's date. [Official release](https://developer.android.com/blog/posts/android-17-is-here?hl=en).

## Xiaomi setup when the phone is available

1. Record the installed OS/build first. Back up important personal data through the user's existing process; use synthetic test messages throughout.
2. Install the debug APK from a trusted local build. For USB testing, enable developer options and USB debugging, connect to the trusted workstation, authorize its fingerprint on the phone, and check `adb devices`. Xiaomi may show a separate install-via-USB control; change only if needed for this install. No bootloader unlock/root is needed.
3. Open the app, tap **Enable keyboard**, enable Sindhi Pinyin Keyboard, review Android's keyboard warning, then tap **Choose keyboard** and select it. Xiaomi's usual manual path is Settings → Additional settings → Language(s) & input → Manage keyboards / Current keyboard. Exact labels vary with region and OS build. [Xiaomi keyboard selection](https://www.mi.com/global/support/article/KA-06428/), [Xiaomi keyboard settings](https://www.mi.com/uk/support/article/KA-06440/).
4. Leave battery optimization and autostart at their defaults for the first full pass. An OS-bound IME should not need a blanket background exemption, overlay permission, notification permission, or autostart request. Record keyboard appearance after reboot/unlock, app switches, and battery-saver transitions.
5. If the keyboard disappears, first recheck enabled/default state, input focus, competing keyboard behavior, force-stop state, crash/ANR evidence, and whether it happens in all apps or one editor. If an OEM restriction is reproducible, document the exact device/build and test one reversible setting change at a time. Do not instruct every user to disable power protection. Xiaomi's own autostart guidance associates unnecessary autostart with resource use; it is not evidence that this IME requires autostart. [Xiaomi autostart guidance](https://dev.mi.com/xiaomihyperos/documentation/detail?pId=1624).
6. After testing, revoke USB debugging authorization or turn USB debugging off if it is no longer wanted. Keep another keyboard enabled so recovery is straightforward.

## Functional release-blocking cases

Run each applicable case in the app practice field, WhatsApp chat, Messages/SMS compose, Gmail subject/body, Chrome search/address bar and an ordinary HTML textarea. Use an unsent draft or consenting test recipient; automated tests must not send messages to real contacts.

| ID | Action | Expected outcome |
| --- | --- | --- |
| I01 | Enable, select, hide/show 30 times; switch apps 50 times | IME appears for standard editable fields; no overlay; host apps remain usable; no stale candidates. |
| I02 | Type bundled everyday exact word, choose candidate | Correct Unicode text replaces only current composition once. Verify against the reviewed mapping/lexicon. |
| I03 | Enter scholarly distinction tokens, then equivalent everyday forms | Strict distinctions survive normalization; everyday ambiguity is offered. Never silently treat the two modes as identical. |
| I04 | Type unknown input, prefix, typo, and rare/name entry | Mapping/literal fallback remains usable; completions and corrections require explicit selection. |
| I05 | Select next word and a curated phrase | Complete chosen text appears once; spacing/punctuation are predictable; no existing committed text overwritten. |
| I06 | Type rapidly and immediately change field/mode | Old worker responses never repopulate candidates or change another field. |
| I07 | Move cursor inside/outside composition; select text; paste; undo | Composition ends safely; replacement respects selection; preceding/following host text remains intact; context resets. |
| I08 | Backspace through Roman token, Sindhi with combining marks, surrogate-pair emoji, skin tone, flag and ZWJ family | No broken surrogate, stray joiner, unrelated deletion or crash. Document exact grapheme behavior by OS/editor. |
| I09 | Space, punctuation, repeated space, newline, Send/Search/Done/Next | Safe composition commits; host-requested action occurs only on explicit action-key press; multiline newline works. |
| I10 | Home/back, rotate, split-screen, lock/unlock, switch IME, reopen | No duplicate, lost or leaked composing text; keyboard picker works; no stuck fullscreen editor. |
| I11 | English, Sindhi script pages, numbers and symbols | All promised characters reachable; Roman row order stays LTR; direct script order is logical Unicode. |
| I12 | Mixed Sindhi, English, digits, brackets, URLs and punctuation | Direction/shaping acceptable in each receiving app; no reversed strings or presentation-form substitution. |
| I13 | System/light/dark, silent/DND, haptic toggles | Theme updates; no unwanted sound/vibration; controls persist. |
| I14 | Valid mapping import, invalid UTF-8/TSV, huge file, duplicate/empty token, control characters | Atomic valid import, bounded work, actionable rejection, prior mapping survives failure; reset works. |
| I15 | Add/delete manual word, learn eligible word after opt-in, restart, clear | Manual entries persist as disclosed; opt-in learns only eligible commits; removal/clear reflected in current candidates. |
| I16 | Clear/disable learning while writes are queued | Cleared data stays cleared; queued writes cannot recreate it; no main-thread database stall. |
| I17 | Kill/recreate IME process; host returns null/rejects InputConnection calls | Graceful recovery and literal input fallback where possible; no crash or corruption. |
| I18 | TalkBack, switch access, 200% text, large display size | Keys/candidates have understandable labels and focus order, no clipped required controls, accessible switching. |

## Privacy tests

1. Inspect the merged manifest and packaged dependencies: no `INTERNET`, ads/analytics, boot receiver, network job, contact/SMS permission, clipboard history, or overlay permission. Confirm no typed contents appear in application logs.
2. Test text password, visible password, web password, numeric PIN/password, no-personalized-learning flag, no-suggestions flag, email, URI, phone and numeric fields. Verify the actual service policy: no personalization in restricted fields, literal/appropriate layout and no secret candidates. Enter a unique synthetic canary, leave the field, inspect local database, restart, and verify it is absent.
3. With learning off from a fresh install, type synthetic ordinary text in several apps. Local learned records must stay empty. Manually added dictionary records are an explicit separate feature and may remain available.
4. With learning on, commit approved synthetic words. Verify saved schema contains only disclosed fields and bounded counts, not keystroke timestamps, app identifiers, message transcripts or unselected composing strings. Word/input-form records still count as sensitive local information.
5. Switch from a public field to a private one during a delayed prediction query. No older candidates or context may appear. Clear data during queued work and verify again after process recreation.
6. Inspect both legacy backup and Android 12+ cloud/device-transfer exclusion rules. Test backup/restore or migration on supported local lab devices; learned and manual dictionary data must not reappear. OEM restore tools need a separate check.
7. Test offline/airplane mode. All essential typing/settings/tutorial functions work. A user-selected document provider can itself use the network for import/export; that is distinct from the keyboard's prediction engine and must be explained.

Android flags and backup behavior require deliberate implementation rather than assumptions. [EditorInfo](https://developer.android.com/reference/android/view/inputmethod/EditorInfo), [backup guidance](https://developer.android.com/identity/data/autobackup).

## Language accuracy study

The bundled seed list and tests are development fixtures. Do not report their success as top-1/top-3 language accuracy. Recruit Sindhi speakers with relevant regional/dialect expertise, obtain consent for research material, confirm corpus rights, and remove personal content before inclusion. Have at least two independent reviewers label acceptable script outputs; adjudicate disagreements and report their agreement. Split by speaker and source, deduplicate phrase families, and freeze the test set before tuning.

For a first benchmark, propose at least 1,000 independent word examples plus separate prefix, next-word, and phrase sets large enough to support useful intervals. Balance common words, names, out-of-vocabulary words, dialect forms, English code-switching, common misspellings, vowels/diacritics, and Sindhi-specific consonants. Publish coverage and per-slice counts. These are proposed study sizes, not gathered data.

Use a TSV/CSV with `id, split, mode, roman_input, context, accepted_outputs, task, prefix_length, source_id, speaker_group, review_status`. Keep private participant metadata outside the released benchmark. List all acceptable spellings before evaluation; use documented Unicode normalization, not arbitrary stripping of meaningful letters/marks.

Report each task independently:

- **Exact word top-k:** number whose acceptable complete output appears in first k *displayed, deduplicated* candidates divided by all eligible complete-word queries, for k=1 and 3. Count empty lists and unknown words as misses. Report in-vocabulary/OOV separately and also combined.
- **Prefix completion:** evaluate fixed prefix lengths or fractions, with only the prefix exposed to the engine. Report top-1/top-3 at each prefix position; do not give the evaluator the full intended Roman word as input.
- **Next word:** use only prior allowed context; measure the next token in a held-out sequence. Reset at sentence/session boundaries, with frozen learning off for the generic model baseline.
- **Phrase completion:** exact normalized match or a predeclared set of accepted phrases, reported separately from word accuracy. Do not infer sentence quality from a single next-word success.
- **Correction:** report intended-word recovery and unwanted-correction suggestion rate on clean words. The MVP offers corrections for selection, not automatic replacement.

Report numerator/denominator, 95% confidence interval (Wilson for simple proportions or participant/source cluster bootstrap when examples are correlated), mapping/corpus/ranker version, candidate cap, and average list size. For adaptation, evaluate chronological held-out sessions: learn only earlier permitted commits, never the current answer or future test examples.

## Keystroke savings and usability

Use a randomized/counterbalanced within-participant study with native-script-capable Sindhi typists and the same reviewed text in both conditions. Compare the same app's direct-script layout and optionally a named common Sindhi keyboard; report baseline layout, predictive features, familiarity and practice time. Give equal practice. Long-press/page switches, globe/mode switches, correction keys, candidate taps and candidate scrolling all count as input actions; do not omit actions that make Roman input look better.

`KSPC = total input actions / final target grapheme count`.

`keystroke savings (%) = 100 × (baseline actions − Roman-IME actions) / baseline actions`.

Report negative savings honestly, alongside completion time, corrected/uncorrected error rate, and participant confidence intervals. Define grapheme counting and spaces/punctuation consistently. A multiword candidate tap counts as one selection action but any scrolling/searching remains in time and action totals. Separate theoretical offline savings from observed human study results.

## Performance and stability protocol

Use fixed synthetic input scripts. Record CPU, memory and latency distributions after warmup and after cold service start; compare the same device/build/brightness/refresh rate/thermal state. Run at least three independent repeated trials per condition. Perfetto, Android Studio Profiler, `adb shell dumpsys meminfo <applicationId>`, and local `adb shell dumpsys batterystats` can provide evidence. Memory/trace files can contain text: only synthetic inputs are allowed.

| Metric | Proposed initial gate, to validate on Xiaomi |
| --- | --- |
| Engine query | p95 under 20ms for supported bounded inputs and data size. |
| Key tap to visible composition/candidates | p95 under 50ms while typing; no repeated perceptible stalls. |
| Cold keyboard appearance | p95 under 300ms; document device OS and warm/cold definition. |
| Memory | Target under 60MiB PSS after warm typing; no upward trend after 100 show/hide cycles or a 30-minute typing session. |
| Background behavior | No scheduled jobs/wake locks/network; no sustained app CPU while keyboard hidden. |
| Energy | Compare 30-minute standardized typing and ≥2-hour screen-off idle against a baseline keyboard. Report absolute energy/drain and variability; investigate consistent extra idle drain. No unsupported “zero battery” claim. |
| Stability | Zero observed crashes/ANRs/text-loss defects across the full acceptance run and closed beta; report session/device/time denominator. |

Track crash-free sessions as `1 − sessions_with_crash / sessions`, and similarly ANR-affected sessions for the controlled test. These are different denominators from Play's user-perceived rates; do not compare them directly. After release use Google Play Android vitals where available, without adding a keyboard telemetry SDK. No observed failure in a small sample is not proof of zero population risk. [Android vitals](https://developer.android.com/google/play/vitals).

## Result template and release decision

For each case record `case_id | build | device/build | host app/version | mode | setup | expected | actual | pass/fail/not run | sanitized evidence | issue`. Leave unexecuted cases as **not run**. Release blockers include leaked private text, any text corruption, unsupported mandatory characters, unusable switching, repeated keyboard nonappearance, crash/ANR, failed data deletion, broken input at supported API levels, and unresolved accessibility blockers. Language and performance thresholds require owner/reviewer agreement before results are examined.
