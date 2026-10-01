# Sindhi Pinyin Keyboard — privacy policy draft

**Publication status:** draft for the research MVP. Replace the publisher/contact/date placeholders, review the final build, and publish an accessible policy before a public release. The implementation and policy must agree.

**Publisher:** [OWNER LEGAL OR ORGANIZATION NAME]  
**Contact:** [PUBLIC SUPPORT EMAIL]  
**Effective date:** [RELEASE DATE]  
**App:** Sindhi Pinyin Keyboard [FINAL APPLICATION ID]

## What the keyboard does

Sindhi Pinyin Keyboard is an Android keyboard that converts Roman Sindhi input into Sindhi-script suggestions. It can also enter Sindhi letters directly and type English. When you choose this keyboard, it handles the keys you press and sends the text you choose to the app in which you are typing.

## Processing on your device

Typing, transliteration and suggestions run on your device. This version does not have Android's internet permission and contains no advertising, analytics, or cloud prediction SDK. It does not automatically transmit your typing, dictionary, or device identifiers to the publisher. No account is required.

The keyboard temporarily holds your current composing input and recent words committed through it in the current editing session to provide suggestions. That temporary context is reset when the session ends or editing context changes. It does not read your conversation history for prediction. It may read a short section immediately before the cursor to perform deletion safely; that text is not retained or used to train suggestions.

The keyboard does not keep a chronological keystroke-event log, timestamps of individual keys, or message transcripts. This differs from the optional local word records described below.

## Optional local learning and your dictionary

Local learning is off by default. If you enable it, the keyboard may save selected/committed words, their Roman input forms, usage counts, and short previous-word-to-next-word counts in private app storage. These records can include names or other personal words. They are not anonymous, and they can reveal short fragments of what you type.

You may also manually add dictionary entries. These are stored because you explicitly add them and remain available even when automatic learning is off. Settings lets you remove manual entries, clear learned entries, or clear the whole local dictionary. Turning learning off stops new automatic learning; it is not a substitute for deleting records already saved. Clear/delete actions also stop learning to prevent queued work from immediately recreating data.

The app avoids personalized learning in fields identified by Android as passwords or other restricted input, and honors a field's request not to personalize learning. Apps supply those labels. A secret entered into an ordinary unmarked field cannot always be recognized as secret; you can leave learning off or clear local records at any time.

## Settings and mapping files

Theme, sound, vibration, Romanization mode and other preferences are stored locally. Custom Roman-to-Sindhi mappings selected by you are stored locally. Import/export uses Android's document picker for the file you choose; the app does not request broad storage access. A document provider you select may itself be a cloud service and follows its own privacy practices. Mapping export exports the mapping, not an automatic transcript of your typing.

## Storage, retention and deletion

Local records remain until you remove them, clear app storage, or uninstall the app. Learned data is bounded rather than allowed to grow without limit. The app uses Android private storage and does not advertise its SQLite database as separately encrypted. Android/device security protects that storage subject to the security of your device.

The app disables automatic backup and excludes its private data from supported Android cloud-backup and device-transfer paths. Manufacturer migration tools may behave differently; do not use external backup tools on private dictionary data unless you intend that transfer. Deleting data removes it from the app's active storage; this is not a claim of forensic erasure from all device/media copies.

## Other apps and system services

Text you choose to enter is delivered to the active app as the expected function of a keyboard. That receiving app controls what it subsequently saves or sends. This keyboard does not control WhatsApp, Messages, Gmail, Chrome, Android, Google Play, Xiaomi services, your document provider, or their separate privacy practices.

This app does not request access to contacts, SMS history, location, microphone, or an accessibility service. Enabling a keyboard in Android is required for it to act as your input method. Android's standard keyboard warning describes the trust involved in granting that role.

## Support and children

If you separately contact the publisher, include only information you choose to send. Do not send private chat transcripts, passwords, or dictionary exports with a bug report. [OWNER: state how voluntarily submitted support correspondence is used, retained and deleted, and identify the support provider before publication.]

The app's final audience and any child-directed features will be declared in its Play listing. [OWNER: finalize the audience statement and any required child-privacy practices before publication. Do not assert compliance or a minimum age without reviewing the actual audience and applicable requirements.]

## Changes and contact

The publisher will update this policy when the app's data practices change. Any future online or research-data collection feature requires a clear explanation and an appropriate user choice; it is not part of this version. For privacy questions, contact [PUBLIC SUPPORT EMAIL].

## Publisher review note — remove before hosting

Reconcile every claim above with the final manifest, dependencies, source behavior, Data Safety form and store listing. Google requires a policy and developer declarations that describe actual data practices, including SDK behavior. Device-only processing and user-initiated expected transfers have specific definitions; “no internet permission” does not independently settle all Data Safety answers. [Google Play Data Safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en), [app review preparation](https://support.google.com/googleplay/android-developer/answer/9859455?hl=en).
