# APK build and verification — 2026-09-29

## Latest update: 0.5.0-alpha

- Artifact: `dist/Sindhi-Pinyin-Keyboard-0.5.0-alpha.apk`, 1,113,531 bytes.
- SHA-256: `6982f2c57bdb2b02b40ab1ccb6898c27aab58af1a55e805eebee2f76b32c1e56`.
- Version code 5; same package and signing certificate, APK v2 signature and ZIP alignment verified. No requested permissions found in APK inspection.
- Offline build, unit-test checks, application APK assembly, Android test APK assembly and lint completed successfully. 46 JVM tests pass (engine checks reused unchanged outputs where Gradle marked them up to date); lint reports 0 errors and 12 warnings.
- Changes are appearance and layout only. Candidate ranking, aliases and IME composition logic are unchanged from the working 0.4 version.
- Listed enabled text color pairs have calculated contrast ratios of at least 5.20:1. See UPDATE-0.5.md for the full list and design references.
- Android UI tests are compiled only. The new design has not been rendered or exercised on a connected device/emulator by the agent. Xiaomi font, touch, orientation and accessibility checks remain manual.

Older reports below are retained as history.

## Latest update: 0.4.0-alpha

- Artifact: `dist/Sindhi-Pinyin-Keyboard-0.4.0-alpha.apk`, 1,132,712 bytes.
- SHA-256: `3cf2025a21ad39e1d28ee8382b430ae04edab9b40d9c4f6300453c72c88442d4`.
- Version code 4, same package and signing certificate as the previous update. Signature v2 and ZIP alignment verified. No requested permissions found in APK inspection.
- Final offline build: engine tests, app JVM tests, APK assembly, Android test APK assembly and lint succeeded.
- 46 JVM tests passed, 0 failures/errors: 36 engine tests and 10 composition tests. User alias tests cover 139 new aliases with top-four assertions; these are supplied fixtures, not held-out language accuracy.
- Lint: 0 errors, 12 warnings.
- New native UI test compiles: checks constant candidate-area height, retaining identical candidate views and tapping numbered choice 2. Device execution has NOT occurred.
- The code paths that double-refreshed candidates and changed keyboard height were removed. The user's actual Xiaomi flicker is not yet confirmed resolved on a phone. New English behavior, font sizing and real-app editor integration still need device checks in UPDATE-0.4.md.

Older reports below are retained as history.

## Latest update: 0.3.0-alpha

- Delivered: `dist/Sindhi-Pinyin-Keyboard-0.3.0-alpha.apk`, 1,142,983 bytes.
- SHA-256: `66bca9d689f9c6fe13e07006a5ad499e23cdb8aa05f877c9875b9a1fd5893866`.
- Same development package, versionCode 3; min SDK 24, target 37, compile SDK 36.
- Final offline Gradle engine tests, Android JVM tests, assembly and lint completed successfully.
- **38 JVM tests passed**: 20 engine regression tests, 8 conversation-model tests and 10 composition tests. These include all three supplied conversations, suffix-only completion, model validation, scholarly isolation and learned phrase safety.
- Lint: **0 errors, 12 warnings**. Signature v2 verification and ZIP alignment passed. APK inspection found no requested permissions.
- No emulator or phone was available. New native UI layout, real app integration, latency, battery, memory, accessibility and Xiaomi behavior have not been tested on a device by the agent.
- No held-out accuracy result or Sogou-equivalent prediction quality is claimed. See `UPDATE-0.3.md` for behavior and phone checks.

The report below records the original 0.1 build and is retained as history.

## Delivered phone build

- File: `dist/Sindhi-Pinyin-Keyboard-0.1.0-alpha.apk`
- Size: 978,315 bytes
- SHA-256: `feec8a1f79de5f5daa42b4e6ab9da5153aff73ebecf3f993f917c24435d14f32`
- Package: `org.sindhipinyin.keyboard.dev`
- Version: `0.1.0-alpha`, versionCode 1
- Minimum Android API: 24; target API: 37; compilation SDK: 36
- Build: Gradle `:app:assembleDebug`, development signed. This is the delivered artifact; it supersedes the earlier standalone packaging experiment.
- APK Signature Scheme v2 verified successfully. v1/v3/v4 are not used by the delivered Gradle debug build; v2 is supported from the minimum API 24.
- ZIP alignment check passed.
- `aapt2` inspection confirms the launcher activity, IME service protected by `BIND_INPUT_METHOD`, input-method metadata, backup exclusions and no requested permissions, including no `INTERNET` permission.

## Executed checks

`gradle :engine:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` completed **BUILD SUCCESSFUL**.

- 17 candidate-engine JUnit tests passed.
- 8 composition-choice JUnit tests passed.
- Kotlin and Android resource/DEX compilation and APK assembly passed.
- `:app:assembleDebugAndroidTest` also passed after correcting the test harness's View constructor. Its 12 Android test cases are compiled and ready, but were not executed on a device.
- Android lint: **0 errors, 9 warnings**. Warnings concern safely inlined no-personalized-learning constants (3 findings), available newer compile/test dependency versions (3), application-context singleton lifetime (1), release resource-shrinking advice (1), and a programmatically created view lacking an XML-editor constructor (1). The repository retains application context, not an Activity.
- Seed smoke evaluation: top-1 **11/12**, top-3 **12/12**. These fixtures overlap the seed dictionary. They are engineering checks, **not** held-out language accuracy or evidence of production readiness.

## Toolchain and environment

Windows; Temurin JDK 21.0.8; Gradle 9.6.0; Android Gradle Plugin 9.4.0; Android SDK Build Tools 36.0.0. Gradle and initial SDK tool archives were checked against official download checksums before use. Kotlin source is compiled to JVM 17 bytecode before DEX conversion.

The available Google SDK metadata supplied platform 36, while the official Android documentation describes API 37. The updated SDK manager's embedded runtime was blocked by Windows Application Control. No security policy was changed. The test app therefore uses installed API 36 compile stubs with target 37 and no API 37-only calls. Upgrade compilation SDK and validate Android 17 behavior before a store release.

The existing Java runtime's Unix-domain loopback failed on this host. A per-process Java property selected the runtime's TCP fallback for Gradle. This has no effect on the APK and does not modify Windows networking or firewall settings.

## Not yet verified

No phone or emulator was available. Installation/launch, full IME lifecycle, real WhatsApp/Messages/Gmail/Chrome editors, Xiaomi fonts and shaping, TalkBack, orientation, OEM lifecycle restrictions, battery/CPU/memory/latency, crashes and ANRs remain **not run on device**. Android instrumentation sources are provided separately; compilation is not execution.

Language review, independent top-1/top-3 assessment, keystroke savings and general sentence-completion quality remain unmeasured. The scholarly scheme and seed data are marked draft. Predictions use the small lexicon and finite phrase table. No generative sentence model is included.

See `TEST_PLAN.md` for the phone procedure and result template. Do not publish this development-signed artifact to Google Play. Publisher identity/contact, a public privacy policy, release signing, store assets and release gates remain outstanding.
