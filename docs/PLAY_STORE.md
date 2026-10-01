# Google Play release checklist

Checked against official sources on 28 September 2026. The project is a research MVP. No Play Console submission, signed production release, real-device screenshots, owner verification, or Xiaomi validation is implied by this file.

## Release identity and artifact

- Choose a permanent application ID under a namespace the publisher controls. The current development ID is `org.sindhipinyin.keyboard.dev`; the source namespace is `org.sindhipinyin.keyboard`. Review the permanent ID before creating the Play app. Do not impersonate an existing organization by choosing its namespace/name.
- Decide the final public name and verify availability. “Sindhi Pinyin Keyboard” is a working title; explain “Roman Sindhi typing” so users do not mistake it for an official standardized romanization.
- Create/verify the owner's Play developer account and complete all identity/contact/device verification that Play Console requests. Keep distribution free with no ads or in-app purchases. A publisher's account costs or hosting costs are separate from charging users.
- Set an increasing versionCode, readable versionName and release notes. Build and inspect a release Android App Bundle. Retain the upload key securely outside source control; use Play App Signing and document key recovery. Never put passwords or private keystores in the repository. [Android signing guide](https://developer.android.com/studio/publish/app-signing).
- Compile/target API 37 for the verified latest stable major Android release. The current Play submission floor for new mobile apps/updates is API 36 as of 31 August 2026; that floor is distinct from the user's latest-stable target requirement. Recheck at submission. [Play target requirement](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en).
- Run unit tests, lint, release build checks, merged-manifest/dependency review, supported-API tests and the Xiaomi/host-app test plan. Verify the release artifact has no debug feature or permission that contradicts the policy. If a later model adds native libraries, audit the applicable 64-bit and page-size requirements before including it.

## Privacy policy and Data Safety

Replace every placeholder in [PRIVACY_POLICY.md](PRIVACY_POLICY.md) with the real publisher name, contact and effective date. Publish an accessible, stable HTTPS HTML policy page and provide both an in-app policy and the Play Console URL. The developer identity and app name must match the listing. A local Markdown draft alone is not a published policy. [Prepare app content for review](https://support.google.com/googleplay/android-developer/answer/9859455?hl=en).

Complete Data Safety from the actual final APK and all its SDKs. For this design, local-only processing is outside Play's definition of collection; on-device transfers to other apps can still be sharing, while an expected transfer triggered by a specific user action is an exception. Normal user-requested IME commits appear to fit that exception; the publisher must assess the final behavior. Therefore “no data collected/shared” is a proposed declaration after audit, not a conclusion based solely on the absence of `INTERNET`. Optional saved words remain sensitive local data and belong in the policy. Do not claim “encrypted in transit” as proof of safety when the app sends nothing. Reassess any later cloud, telemetry, backup or export feature. [Official Data Safety definitions](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en).

Explain no account, no keystroke-event log, optional on-device word/transition learning, manual dictionary entries, clear/delete controls, temporary editor interaction, and no automatic cloud backup/device transfer. A user may choose a cloud-backed provider in Android's document picker; describe that boundary honestly. An email the user separately sends to support is outside automatic keyboard operation and must be handled under the publisher's stated policy.

## Listing and content declarations

Complete the console's current app-content items: ads **No** for this build; app access instructions (no login, explain enabling/selecting the keyboard); target audience; content rating questionnaire; Data Safety; privacy policy; and any other declarations displayed for this package. Answer the rating questionnaire based on the app's included content and capabilities; do not invent a rating. Choose an honest target audience and review Families requirements if children are targeted. [App-content review checklist](https://support.google.com/googleplay/android-developer/answer/9859455?hl=en).

Proposed short description (under 80 characters):

> Type Roman Sindhi and choose Sindhi words. Offline and free.

Draft full description for review after device validation:

> Sindhi Pinyin Keyboard helps you write Sindhi using familiar Roman letters. Type a word, then choose a Sindhi suggestion from the candidate bar.
>
> Use everyday Roman spellings or explore a precise scholarly-style mode. Switch to direct Sindhi letters or English, and use the globe to choose another installed keyboard.
>
> Transliteration and suggestions work offline. There are no ads or accounts. Optional local learning remembers selected words on your device, and you can delete it in Settings. The app includes a mapping guide and a practice area.
>
> This early version uses a small dictionary and a developing Romanization convention. Suggestions can be incomplete or incorrect. The scholarly-style mode is pending language-expert review.
>
> To start, open the app, enable the keyboard in Android settings, and choose it as your input method. It is designed for standard Android text fields. Compatibility with individual apps depends on their editor behavior.

Before production, replace early-version wording with the actual validated status; do not promise perfect accuracy, compatibility with every app, or a measured benefit without evidence. Provide English and native-reviewed Sindhi listings when feasible. Keep the app free in pricing/distribution settings.

## Store assets

Prepare the Play icon as 512×512 PNG and feature graphic as 1024×500 JPEG/24-bit PNG. Supply at least two genuine app screenshots following Play's current size/aspect constraints; four useful 1080×1920 portrait captures are typing/candidates, mode switching, tutorial, and privacy controls. Use synthetic text and the release UI. A mockup is not evidence of tested functionality, and other apps' logos/chats should not imply endorsement. Current screenshot rules allow 320–3840px dimensions, with the longer dimension at most twice the shorter; check the console's accepted specifications again. [Official preview asset requirements](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en-GB).

Do not capture private WhatsApp chats for the listing. Use the app's own practice field where possible; if a host-app compatibility screenshot is needed, create a consented/synthetic test conversation and review branding rights. Include tablet assets only after actually validating tablet layouts.

## Test tracks and submission

1. Run an internal track for trusted testers and review automated/pre-launch reports where available. IME setup can require additional reviewer instructions and manual testing; automated results alone are insufficient.
2. Run a closed community beta with reviewed test material, clear issue reporting, and informed optional research participation. For personal developer accounts created after 13 November 2023, the current rule is at least 12 testers opted in continuously for the preceding 14 days before applying for production access. Meeting the count does not itself grant production approval. Check account-specific Console instructions. [Personal-account testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB).
3. Record failures and language feedback, fix release blockers, repeat affected tests, and make the exact candidate release artifact reviewable.
4. The owner reviews package identity, signed AAB, privacy/contact details, Data Safety answers, screenshots, audience/rating, supported countries and the “free” setting before submitting.
5. After approval, monitor Play-provided vitals and consented reports. Triage crash/ANR and text/privacy defects promptly; pause rollout or issue a fix when warranted. Keep mapping/model changes versioned and update the privacy declaration if behavior changes.

## What still needs the owner

The permanent application ID, verified publisher account/identity, public support contact, final app name, policy hosting, release signing ownership, real-device results, authentic screenshots, language review, and required beta testers cannot be fabricated. This repository prepares those steps; it does not publish the app or claim that Google will approve it.
