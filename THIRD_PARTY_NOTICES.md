# Third-party notices

## Sindhi spellchecking dictionary

`engine/src/main/resources/dictionaries/` contains the Sindhi Spellchecking Dictionary 2011.01.10, maintained by **nizamani** as identified on its OpenOffice extension page. These dictionary files are distributed under **GNU LGPL 2.1**, separately from the application's Apache-2.0 code license.

- [Original extension](https://extensions.openoffice.org/en/project/sindhi-spellchecking-dictionary.html)
- [Preserved provenance and attribution](engine/src/main/resources/dictionaries/NOTICE.txt)
- [Full dictionary license](engine/src/main/resources/dictionaries/LGPL.txt)

The original `sd.dic`, `sd.aff`, `desc.txt`, and `description.xml` are preserved. The dictionary supplies script spellings; the application's Roman retrieval heuristics are not pronunciations validated by the dictionary's author.

## Gradle wrapper

The generated Gradle wrapper launchers and wrapper JAR are redistributed with the project. Gradle is licensed under Apache-2.0; the launcher copyright and license headers are preserved.

## Build dependencies

Gradle resolves Android, Kotlin, JUnit, and AndroidX tooling and libraries from the repositories configured in `settings.gradle.kts`. Their respective upstream licenses apply. Downloaded SDKs, dependency caches, and signing keys are excluded from this repository.
