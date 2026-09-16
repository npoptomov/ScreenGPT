<p align="center">
  <img src="docs/banner.svg" alt="ScreenGPT — Capture your screen. Ask ChatGPT." width="960">
</p>

<p align="center">
  <a href="https://github.com/npoptomov/ScreenGPT/releases/latest/download/ScreenGPT.apk"><img alt="Download APK" src="https://img.shields.io/badge/Download-APK-59F2C4?style=for-the-badge&amp;labelColor=10182D"></a>
  <a href="https://github.com/npoptomov/ScreenGPT/releases"><img alt="GitHub Releases" src="https://img.shields.io/badge/GitHub-Releases-7395FF?style=for-the-badge&amp;labelColor=10182D"></a>
</p>

See something you want to ask about? Use a button or gesture to send a screenshot to ChatGPT. Add your question and press send when you're ready.

**Android 11+ recommended · No API key · No root**

[Get started](#install-and-enable-screenshot-access) · [Find your phone](#phone-shortcuts) · [Need help?](#help)

## 🟢 Get started

<a id="install-and-enable-screenshot-access"></a>

1. Install the official [ChatGPT app](https://play.google.com/store/apps/details?id=com.openai.chatgpt) and sign in.
2. [Download ScreenGPT.apk](https://github.com/npoptomov/ScreenGPT/releases/latest/download/ScreenGPT.apk), open it, and install. Allow your browser or file manager to install apps if Android asks.
3. Open ScreenGPT → **Enable screenshot access** → turn on **ScreenGPT screen capture** in Accessibility.
4. Set up a **physical button or back-tap shortcut** using the [guide for your phone](#phone-shortcuts). Open the screen you want to share and trigger it while your phone is unlocked.

> **To open Settings later:** long-press the ScreenGPT app icon → **Settings**. A normal tap starts capture once setup is done.

<details>
<summary>Android says “Restricted setting”?</summary>

Open **Settings → Apps → ScreenGPT → ⋮ → Allow restricted settings**, if available. Then go back and enable screenshot access. Only allow this for apps you trust. [Google's instructions](https://support.google.com/android/answer/12623953?hl=en).

</details>

<a id="phone-shortcuts"></a>

## 📱 Start here: physical buttons and gestures

Start with your phone’s button or back-tap shortcut, where supported. **Select ScreenGPT as the app to open** to start capture immediately. Choose your phone below. These are documented system options, not a claim that every model has been tested. Menus vary by software version.

<details>
<summary><b>Samsung Galaxy — double-press the Side button</b></summary>

Go to **Settings → Advanced features → Side button** (or **Side key**) → **Double press**. Choose **Apps** or **Open app**, then **ScreenGPT**.

Now double-press the Side button while viewing another app. This replaces its previous double-press action, usually Camera.

[Samsung's setup guide](https://www.samsung.com/ie/support/mobile-devices/how-to-customise-the-side-button-with-new-features-on-your-galaxy-phone-and-tablet/)

</details>

<details>
<summary><b>Google Pixel — double-tap the back</b></summary>

Go to **Settings → System → Gestures → Quick Tap** (or **Quick Tap to start actions**). Enable it, select **Open app**, tap its settings gear, and choose **ScreenGPT**.

Tap the back of the phone twice while the screen is on and unlocked. Google lists Quick Tap for **Pixel 4a (5G) and later**; if the option is missing, use ScreenGPT's tile or floating button.

[Google's gesture guide](https://support.google.com/pixelphone/answer/7443425?hl=en-AU)

</details>

<details>
<summary><b>Honor — double-press power</b></summary>

Search phone Settings for **double press** or **power button**. If your MagicOS version lets this gesture open an app, choose **ScreenGPT**.

This is the setup used for this project. Not every Honor model exposes the same choices; use the tile or floating button if yours doesn't offer app selection.

</details>

<details>
<summary><b>Motorola — Quick Launch back tap</b></summary>

On models with Quick Launch, go to **Settings → Gestures → Quick Launch**, or **Moto → Gestures → Quick Launch**. Enable it and select **ScreenGPT** as the app.

Double-tap the back while viewing the screen you want to share. If Quick Launch isn't listed, use the tile or floating button.

[Motorola's instructions](https://help.motorola.com/hc/3473/13/na/en-us/T0943956357.html)

</details>

<details>
<summary><b>OnePlus, OPPO &amp; realme — other shortcut options</b></summary>

If your model has no suitable button or back-tap app shortcut, try the alternatives below.

On phones with **Smart Sidebar**, search Settings for that name, enable it, open the sidebar, and use **Edit** or **+** to add ScreenGPT if it appears. This is worth trying, but some versions launch apps in a floating window, so the capture may include extra system UI. Use the tile if that happens.

Screen-off gestures and fingerprint-launch menus aren't ideal here: you want to capture the app you're already looking at, with the phone unlocked.

Manufacturer references: [OnePlus manual](https://service.oneplus.com/content/dam/support/user-manuals/common/OnePlus_Nord_CE4_Lite_User_Manual.pdf) · [OPPO sidebar demo](https://www.youtube.com/watch?v=bI7rnwnrPKc) · [realme manual](https://r1.realme.net/general/20230130/1675070322576.pdf)

</details>

<details>
<summary><b>Xiaomi, Redmi &amp; POCO — other shortcut options</b></summary>

If your model offers a button or gesture that opens a chosen app, assign ScreenGPT. A built-in screenshot action alone won't send the image to ChatGPT.

You can also check **Settings → Additional settings → Quick ball → Select shortcuts** on models that have it. If your version offers an app picker, choose ScreenGPT. Xiaomi documents Quick ball customization, but doesn't promise arbitrary app selection on every model.

[Xiaomi's Quick ball guide](https://www.mi.com/global/support/faq/details/KA-507654/)

</details>

**Another Android phone?** First check whether its settings let a button or gesture open any app, and assign ScreenGPT. If not, use one of the alternatives below. The digital-assistant option in ScreenGPT Settings is experimental and depends on the phone.

## 🟣 Alternatives: tile or floating button

If your phone doesn’t offer a suitable button or gesture, use either of these.

| Quick Settings tile | Floating button |
| --- | --- |
| Expand Quick Settings → **Edit** → add **ScreenGPT**. Tap it over any app to capture. [Tile setup help](https://support.google.com/android/answer/9083864?hl=en). | In ScreenGPT Settings, turn on **Floating capture button**. It appears on the right. Tap to capture, drag to move. |
| Good if you want to keep the screen clear. | Drag onto **×** to hide it. Turn it back on in Settings. It hides itself during screenshots. |

<a id="help"></a>

## 🛠 Need help?

<details>
<summary><b>Capture stopped working, or the button disappeared</b></summary>

Check that **ScreenGPT screen capture** is still enabled in Accessibility. Turn it off and on if the service isn't responding. If your phone closes it in the background, allow ScreenGPT background activity in Battery / App launch settings.

Dragged the button onto ×? Turn **Floating capture button** back on in ScreenGPT Settings.

</details>

<details>
<summary><b>ChatGPT didn't receive the image / a screen won't capture</b></summary>

Update ChatGPT, or attach the saved image from **Pictures/ScreenGPT** manually. ScreenGPT shares the image; you still choose what to send in ChatGPT.

Some apps block screenshots of protected content. ScreenGPT can't capture those screens.

</details>

<details>
<summary><b>Older Android or temporary screen sharing</b></summary>

Android 8–10 can install ScreenGPT, but the recommended screenshot service and floating button need Android 11+. ChatGPT has its own device requirements.

In ScreenGPT Settings, choose **Start temporary capture session** and accept Android's prompt. Then use the tile or an app-launch shortcut. Stop the session from its notification or Settings.

Android asks for consent again after the session ends. Locking the phone or stopping the process may end it. On Android 8–9, gallery saving may fail, though sharing can still use a cached image. This fallback hasn't been verified on older phones.

</details>

## A few things to know

- Screenshot access is used when you trigger a capture. The screenshot service doesn't read screen text through Accessibility or monitor your taps. The separate legacy volume shortcut is optional; leave it off if you don't use it.
- Images are saved locally when possible, copied to the clipboard as an image reference, and shared with ChatGPT. Check what's on screen before capturing.
- The downloadable APK is **debug-signed**. A checksum is included in each release. Updates need the same signing key; builds signed differently may require uninstalling first, which resets settings.
- ScreenGPT is an independent project, not an official OpenAI app.

<details>
<summary><b>Build it yourself</b></summary>

Use JDK 17+ and Android SDK 35. Set `ANDROID_HOME` or add your SDK path to an untracked `local.properties`, then run:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The APK is at `app/build/outputs/apk/debug/app-debug.apk`.

The current release passed build, lint, and six unit tests. The tests cover capture-request gating and volume-button logic; gestures, widget dragging, and ChatGPT attachment still need testing on each phone.

</details>
