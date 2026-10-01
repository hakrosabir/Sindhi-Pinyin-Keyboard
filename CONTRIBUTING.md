# Contributing

Useful contributions include native-speaker review, reproducible editor bugs, accessibility feedback, and small, tested fixes. Open an issue before proposing a large change.

## Development

1. Fork and clone the repository, then create a branch.
2. Follow the [README](README.md) to install Java and the Android SDK.
3. Run `./gradlew :engine:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest` (use `gradlew.bat` on Windows).
4. For IME, layout, or editor behavior, also run `:app:connectedDebugAndroidTest` on a device or emulator and record the Android version and editor tested.
5. Open a pull request describing the problem, behavior after the change, and checks performed. State any checks you could not run.

## Language data

Use UTF-8 TSV files in `engine/src/main/resources`. Include the Roman input, expected Sindhi spelling, mode, meaning/context, and provenance when changing a mapping or alias. Explain dialect or spelling variants; do not silently remove alternatives. Add an engine regression test for the intended behavior. Keep ranking weights distinct from measured corpus frequency. Do not add private conversations or copyrighted corpora without redistribution rights.

## Privacy and licensing

Keep prediction offline and honor sensitive-field and no-personalized-learning policies. Do not include keys, credentials, learned dictionaries, or private screenshots in issues or commits. The application code is Apache-2.0; bundled dictionary files retain their own LGPL-2.1 license and attribution. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
