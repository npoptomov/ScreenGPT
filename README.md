# ScreenGPT

Honor double-press power launches ScreenGPT to capture the visible screen and share it with ChatGPT. There is no floating window and no overlay permission.

## Recommended setup (Android 11+)

1. Install the latest APK. Open ScreenGPT; setup appears until screenshot access is enabled.
2. Tap **Enable screenshot access** and enable **ScreenGPT screen capture** under Accessibility. This is a separate service from the old volume-button shortcut; disable that old shortcut if unwanted.
3. In Honor's double-press power app setting, choose **ScreenGPT**.
4. Return to another app and double-press power while unlocked. ScreenGPT saves, copies and shares the screenshot directly. ChatGPT controls attachment handling and sending the message.

The screenshot service is system-managed and only captures when triggered. Closing Settings does not revoke its accessibility authorization. It does not monitor accessibility events, read the UI tree, listen to keys, or control other apps. It does not keep continuous screen recording running. Protected screens remain unavailable.

If Android blocks enabling a sideloaded service, use ScreenGPT App info → Allow restricted settings if offered. If Honor stops it in the background, allow ScreenGPT background activity in Battery / App launch settings. Force-stop can interrupt the service; reopen the app or re-enable access if needed. No app can guarantee that Android or Honor will keep its process alive forever.

Long-press the app icon → **Settings** to return to setup. Tapping its icon normally performs capture.

## Alternatives

- The Quick Settings tile runs the same capture flow.
- A native digital-assistant service supports Android's assistant screenshot delivery. Honor controls assistant gesture mapping.
- **Start temporary capture session** uses MediaProjection on older devices or when accessibility is not enabled. It runs in a foreground service with a persistent notification and a Stop action, without a floating window. Closing Settings or removing its task does not intentionally stop this service. Android ends the capture on revocation, process death, and on lock on recent versions. Fresh consent is required after it ends; tokens are never reused or restored after process death.

## Validation

Build: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` with JDK 17+ and the Android SDK. Physical Honor gesture, OEM service retention and actual ChatGPT image attachment must be checked on the phone.
