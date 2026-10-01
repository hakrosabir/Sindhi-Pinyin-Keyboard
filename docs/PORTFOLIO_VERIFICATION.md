# Portfolio preparation verification

Verified locally on **1 October 2026** against the current 0.5.0-alpha source. This report supersedes the README's old 0.1 installation instructions; earlier reports remain historical records.

## Executed checks

The following Gradle tasks were forced to execute again (`--rerun-tasks`):

```text
:engine:test :app:testDebugUnitTest :app:assembleDebug
:app:assembleDebugAndroidTest :app:lintDebug
```

Result: **BUILD SUCCESSFUL**, 84 tasks executed.

- Engine: 36 tests, zero failures or errors.
- App JVM composition tests: 10 tests, zero failures or errors.
- Debug APK and instrumentation APK: compiled successfully.
- Lint: zero errors, nine warnings. Warnings cover inlined API constants, compilation SDK version, a static context reference, the programmatic view constructor, and untranslated/concatenated UI text. They are not suppressed by the portfolio workflow.
- The Windows packaging helper now builds through Gradle and reads version metadata from its actual APK output, replacing an obsolete fallback that hard-coded 0.1.0. The helper was run against the cached toolchain after this change.

The local toolchain was Temurin JDK 21.0.8, Gradle 9.6.0, Android Gradle Plugin 9.4.0, SDK Platform 36, and Build Tools 36.0.0. Cached dependencies allowed an offline build. This host used a per-process `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=Z:/nonexistent-sindhi-socket-dir` workaround for its Gradle socket issue; it is not required in the repository or CI configuration.

## Repository preparation

- Updated README: current version, actual Space behavior, dictionary provenance, build instructions, architecture, and explicit research limitations.
- Added GitHub Actions build/test/lint workflow, report/APK artifacts, contribution guide, bug report form, and PR template.
- Added third-party notices and preserved the bundled dictionary license.
- Added line-ending rules and exclusions for signing material, local toolchains, private research, generated packages, and duplicate release snapshots.
- Kept original local research and release-kit files on disk; excluded them from the public source package.

## Remaining validation

No device or emulator was connected (`adb devices` returned an empty device list). Instrumentation execution, visual screenshots, real editor compatibility, accessibility, and performance therefore remain unverified. No held-out language accuracy result is claimed. GitHub Actions execution must be checked after the repository is published; local results are not remote CI results.

This is a source/portfolio preparation, not Play Store publication or production certification. Consult [TEST_PLAN.md](TEST_PLAN.md) and [PLAY_STORE.md](PLAY_STORE.md) for the remaining release work.
