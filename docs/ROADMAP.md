# Sindhi Pinyin Keyboard: scope and roadmap

Planning baseline: 28 September 2026. This is an offline research MVP, not a validated language product or a published Play Store release.

## Decisions already made

- Support everyday Roman spellings and a separate strict scholarly input mode. The strict mode is an explicit, versioned project convention until Sindhi linguists approve its relationship to a named romanization standard.
- Use a real Android `InputMethodService`, Kotlin, Gradle Kotlin DSL, and local SQLite. Minimum Android version: Android 7.0 / API 24.
- The first physical test device will be a Xiaomi 11 Ultra. It is not connected now; its installed MIUI/HyperOS and Android versions are unknown.
- Keep the app free. No ads, account, analytics, network permission, cloud prediction, or keystroke event log.
- Local personalization is optional and starts off. Selected committed words and short word transitions can be stored locally when enabled; this is clearly disclosed.

Android 17 / API 37 was released on 16 June 2026 and is the latest stable major Android release verified for this baseline. Compile and target API 37; recheck the stable SDK and behavior changes before release. [Official release announcement](https://developer.android.com/blog/posts/android-17-is-here?hl=en), [SDK setup](https://developer.android.com/about/versions/17/setup-sdk).

## Phase 0 — language and privacy contract

Deliver the editable mapping, draft seed vocabulary, input-mode definitions, privacy policy draft, and reproducible evaluation protocol. Keep source/provenance and review status alongside each data release. Native speakers should review ambiguous everyday forms, vowels, implosives, aspiration, retroflex sounds, loanwords, dialect variation, and Sindhi-specific Unicode letters.

**Gate:** named reviewers approve an initial mapping version and a licensed seed lexicon; independent evaluation examples are kept separate from development examples. A community editor must be able to change a mapping without editing the engine.

## Phase 1 — runnable MVP

Implement Roman composition and explicitly selectable Sindhi candidates; dictionary matches, prefix completions, bounded typo suggestions, next-word suggestions, and small curated phrase suggestions. Use a pure Kotlin engine and native Android key/candidate views. Include direct Sindhi script pages, English, numbers/symbols, system keyboard picker, onboarding, tutorial/practice, editable mapping import/reset, light/dark/system theme, vibration/sound preferences, and local learning controls.

The phrase feature looks up a small authored list. It does not generate arbitrary sentences. A small seed dictionary demonstrates the architecture; it cannot substantiate a claim of high Sindhi accuracy. An engine test passing establishes behavior for that test, not language quality.

**Gate:** clean build, unit tests and lint pass; sensitive-input protections and lifecycle behavior pass instrumentation/manual tests; no permission or dependency introduces network transmission. Record build tools and test results in the release notes.

## Phase 2 — real-device and language validation

Run the complete [test plan](TEST_PLAN.md) on Xiaomi 11 Ultra and API 24/API 37 emulators. Test WhatsApp, Messages, Gmail, Chrome, portrait/landscape, rotation, keyboard switching, lock/unlock, process recreation, long messages, mixed RTL/LTR text, accessibility, and sensitive fields. Benchmark latency, memory, active and idle battery impact. Use a consented, licensed, held-out Sindhi evaluation set across speakers and genres.

**Gate:** all release-blocking tests pass; top-1/top-3 and keystroke savings are reported with sample sizes and uncertainty; expert review is complete; no untriaged crash, ANR, lost/duplicated text, or sensitive-data retention defect remains. Performance budgets in the test plan are proposed gates, not measured results.

## Phase 3 — community beta and Play submission

Expand the lexicon with licensed/community-reviewed entries, polish accessible layouts, translate onboarding and privacy explanations into reviewed Sindhi, and complete a closed beta. Prepare the owner identity/contact, permanent application ID, upload/signing setup, hosted policy URL, real screenshots, Data Safety declaration, rating questionnaire, and store listing. Follow [the release checklist](PLAY_STORE.md).

**Gate:** owner reviews the exact signed release artifact and final policy/listing; Play Console account-specific test requirements are satisfied; real-device findings are resolved. Publication is a separate owner-account action. Source delivery alone does not mean the app is ready to ship.

## Phase 4 — measured accuracy improvements

Prioritize confusion analysis over model size: expand spelling aliases and word coverage, improve context ranking and tokenization, add compound words/names with explicit consent, and evaluate dialect-specific options. Introduce a compact word bigram/trigram model only with corpus rights, documented preprocessing, and held-out gains. Keep local learning resettable and bounded.

Only consider a quantized LiteRT/TFLite or ONNX model after comparing it with the rule/dictionary baseline for accuracy, typing effort, cold start, memory, energy, and APK size. Run inference only while typing, with a hard compute budget and deterministic fallback. More fluent completion is not automatically more useful, and must not be advertised as factual or culturally authoritative.

## Later community features

- Reviewed Sindhi UI, regional spelling preferences, and an accessible mapping editor.
- User-curated dictionary import/export with a clear warning that the chosen destination may be a cloud provider.
- Better punctuation, digits, diacritics, emoji handling, split/tablet layouts, and hardware keyboard support.
- Optional teaching exercises and typing-effort studies; no passive research telemetry.
- Public error-report template containing synthetic examples and version numbers, never automatic uploads of private chats.

## Known boundaries

Standard Android editors can work with the IME; custom editors may violate input-connection expectations and need individual fixes. Glyph shaping is provided by Android and the receiving app's font; this app cannot guarantee every third-party app's rendering. The user's phone remains the authority for keyboard enablement and selection. No app should promise zero battery impact or perfect crash-free operation without measured evidence.
