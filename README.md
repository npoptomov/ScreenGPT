# ScreenGPT

Capture the screen you are viewing and share it with the ChatGPT Android app using a shortcut, Quick Settings tile, or optional floating button.

**[Download the latest APK](https://github.com/npoptomov/ScreenGPT/releases/latest/download/ScreenGPT.apk)** · **[All releases](https://github.com/npoptomov/ScreenGPT/releases)**

ScreenGPT is an independent project, not an official OpenAI app. You review and send the image in ChatGPT; ScreenGPT does not automatically submit a message. No OpenAI API key is needed for this workflow.

## Requirements

- **Android 11 or newer recommended:** screenshot accessibility access and the floating button.
- Android 8–10 can install the app and use temporary screen sharing. These older versions and devices other than the development Honor phone have not been verified on hardware.
- Install and sign in to the official [ChatGPT Android app](https://play.google.com/store/apps/details?id=com.openai.chatgpt) to receive screenshots. Its own device requirements also apply.

## Install and enable screenshot access

1. Open [Releases](https://github.com/npoptomov/ScreenGPT/releases/latest) on your phone and download **ScreenGPT.apk** under **Assets**. The source-code ZIP is not the installable app.
2. Open the APK. If prompted, allow your browser or file manager to **install unknown apps**, then complete installation.
3. Open ScreenGPT. Before screenshot access is enabled, it opens setup. Tap **Enable screenshot access**.
4. In Android Accessibility settings, find **ScreenGPT screen capture** under installed/downloaded apps or services, and enable it. Menu names vary by manufacturer.
5. If Android reports a restricted setting, open **Settings → Apps → ScreenGPT → ⋮ → Allow restricted settings**, if offered, then return to Accessibility. Only grant this access if you trust the app. See [Google's restricted-settings instructions](https://support.google.com/android/answer/12623953?hl=en).
6. Choose a trigger below. Return to another app and trigger ScreenGPT while the phone is unlocked. It captures the screen, attempts to save it in **Pictures/ScreenGPT**, copies an image URI to the clipboard, and opens ChatGPT with the image.

**Return to ScreenGPT Settings:** long-press the ScreenGPT launcher icon and select **Settings**. After setup, a normal tap on the app icon triggers capture instead of opening Settings. Android's Accessibility service details also link to ScreenGPT Settings.

## Choose how to capture

### Any supported phone: Quick Settings tile

This is the recommended starting point for Pixel, Samsung, OnePlus, Xiaomi, Motorola, and other Android phones when a custom hardware shortcut is unavailable.

1. Swipe down to expand Quick Settings, then select **Edit**, the pencil, or the device's tile-editing control.
2. Find **ScreenGPT** in the available tiles and add it to the active panel.
3. While viewing the screen you want to capture, open Quick Settings and tap **ScreenGPT**. The panel closes and capture starts. Unlock first if prompted.

Tile editing varies by device; see [Google's Quick Settings guide](https://support.google.com/android/answer/9083864?hl=en). Manufacturer-specific behavior still needs testing on your phone.

### Any Android 11+ phone: optional floating button

In ScreenGPT Settings, enable **Floating capture button**. It uses the same screenshot accessibility service; no separate “display over other apps” permission is needed.

- The button initially appears on the right side.
- Tap it to capture and share; drag it to reposition it.
- While dragging, an **×** target appears near the bottom. Drop the button there to hide it and turn its setting off.
- Enable the switch again to bring it back. You can also disable it directly in Settings.
- The button hides during capture so it is not included in the screenshot.

### Honor: double-press power

On Honor models that offer an **open a specific app** action for double-pressing power, select **ScreenGPT** in that system setting. With screenshot access enabled, the gesture performs capture and sharing immediately.

The setting's name and location vary by MagicOS version. Search device Settings for “double press” or “power button.” If your model only offers Camera, Wallet, or an assistant, use the tile or floating button instead.

### Other manufacturers: configurable buttons or gestures

If your phone supports assigning a button or gesture to **open an app**, select **ScreenGPT**. The same capture flow will run. This applies only where the manufacturer allows custom app selection; ScreenGPT cannot remap a reserved power-button action itself.

ScreenGPT also offers **Optional: set as digital assistant** in Settings. Assistant invocation and screenshot delivery depend on the phone, Android version, and system screenshot settings. Treat this as an alternative to try; use the tile or floating button if it does not work reliably.

Some launchers let you long-press the ScreenGPT icon and drag its capture shortcut onto the home screen. The older **ScreenGPT volume shortcut** accessibility service is optional and can change volume when used; leave it disabled unless you want it.

## Temporary screen sharing / older Android versions

If screenshot accessibility is unavailable or you prefer not to enable it, open ScreenGPT Settings and select **Start temporary capture session**, then accept Android's screen-sharing prompt. Use the Quick Settings tile or an app-launch shortcut to capture while the session is active. On Android 8–10, starting capture can also request this consent directly.

The session uses a persistent notification. End it using **Stop temporary session** in Settings or the notification's stop action. Closing Settings does not intentionally stop the session, but Android can end it when permission is revoked, the process is killed, or the phone locks on recent versions. Consent is required again after it ends; screen-sharing permission cannot be made permanent. The floating button requires Android 11+ screenshot accessibility and is not part of this fallback.

On Android 8–9, saving to the public gallery may fail; sharing can still use the app's cached image.

## Troubleshooting

| Problem | What to try |
| --- | --- |
| Trigger opens setup | Enable **ScreenGPT screen capture**, or start a temporary session. |
| Service enabled but not ready | Wait for Android to connect it; if necessary, switch the accessibility service off and on. |
| Capture stops working in the background | Allow ScreenGPT background activity in the phone's Battery / App launch settings. Honor and other manufacturers may restrict services. Force-stop or reboot can require reconnection; the app cannot guarantee continuous operation. |
| Floating button disappeared | If you dragged it onto ×, turn **Floating capture button** on again. Otherwise check screenshot accessibility access. |
| Image does not attach in ChatGPT | Install or update ChatGPT. If available, manually attach the saved image from **Pictures/ScreenGPT**. Attachment handling is controlled by ChatGPT. |
| Protected screen error | Android prevents screenshots of protected content, including some private media and banking screens. ScreenGPT does not bypass this protection. |
| APK will not update an existing installation | Android requires matching signing certificates. A build signed with a different key cannot update it in place; uninstalling resets app settings and may be required. |

## Privacy and permissions

The screenshot accessibility service captures only when triggered. It does not read the UI tree, monitor accessibility events, filter keys, or control other apps. The optional legacy volume service listens for its button shortcut only when separately enabled.

Screenshots are saved locally when possible, placed on the clipboard as an image URI, and shared with the ChatGPT app. Consider what is visible before triggering capture. What you subsequently send in ChatGPT is handled by that app. Temporary MediaProjection sessions keep screen-sharing access active until ended, while the recommended accessibility flow does not run continuous screen recording.

## Builds and verification

The initial GitHub APK is a **debug-signed build**, using the same signing configuration as the existing development APKs. It is a development distribution, not a Play Store release. Releases include a **SHA256SUMS.txt** checksum for the APK.

Build locally using JDK 17+ and Android SDK 35. Configure `ANDROID_HOME` or an untracked `local.properties` with your SDK location, then run:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

Build, lint, and the existing six unit tests pass for the initial release. These tests cover capture-request gating and volume-button chord logic, not end-to-end device interaction. Hardware gestures, floating-button dragging, background retention, and ChatGPT attachment behavior need verification on each device.
