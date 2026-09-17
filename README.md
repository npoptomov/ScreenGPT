<p align="center">
  <img src="docs/banner.svg" alt="ScreenGPT — Capture your screen. Ask ChatGPT." width="960">
</p>

## 📥 Download

**[Download ScreenGPT.apk](https://github.com/npoptomov/ScreenGPT/releases/latest/download/ScreenGPT.apk)** — open this link on your Android phone, then tap the downloaded file to install.

[Release notes and checksums](https://github.com/npoptomov/ScreenGPT/releases/latest) · [Setup instructions](#install-and-enable-screenshot-access)

<p align="center">
  <a href="https://github.com/npoptomov/ScreenGPT/releases/latest/download/ScreenGPT.apk"><img alt="Download APK" src="https://img.shields.io/badge/Download-APK-59F2C4?style=for-the-badge&amp;labelColor=10182D"></a>
  <a href="https://github.com/npoptomov/ScreenGPT/releases"><img alt="GitHub Releases" src="https://img.shields.io/badge/GitHub-Releases-7395FF?style=for-the-badge&amp;labelColor=10182D"></a>
</p>

See something you want to ask about? Use a button or gesture to send a screenshot to ChatGPT. Add your question and press send when you're ready.

**Android 11+ recommended · No API key · No root**

[Get started](#install-and-enable-screenshot-access) · [Find your phone](#phone-shortcuts) · [Need help?](#help)

## Get started

<a id="install-and-enable-screenshot-access"></a>

### 1. Install the apps

Install the official [ChatGPT app](https://play.google.com/store/apps/details?id=com.openai.chatgpt) and sign in. Then [download ScreenGPT.apk](https://github.com/npoptomov/ScreenGPT/releases/latest/download/ScreenGPT.apk), open it, and install. Allow your browser or file manager to install apps if Android asks.

### 2. Enable screenshot access

Open **ScreenGPT → Enable screenshot access**. In Android Accessibility settings, find **ScreenGPT screen capture** and turn it on.

If Android says **Restricted setting**, open **Settings → Apps → ScreenGPT → ⋮ → Allow restricted settings**, if available. Then return to Accessibility and enable the service. Only allow this for apps you trust. [Google's instructions](https://support.google.com/android/answer/12623953?hl=en).

<a id="background-setup"></a>

### 3. Check background access — required setup step

**Check this before setting up your shortcut.** Some phones stop background apps when you clear recent apps or leave them unused. On Honor, switching App launch to manual fixed this in our setup.

Choose your brand below. These are phone settings adapted from the linked support guides, not a list of models tested with ScreenGPT. Menus depend on the Android version, region, and model; use Settings search if a path doesn't match. You don't need to disable battery saving for the whole phone, and not every phone needs an exception.

<details>
<summary><b>Honor — MagicOS / Magic UI</b></summary>

1. Open phone **Settings** and search for **App launch**.
2. Select **ScreenGPT** and turn off **Manage automatically**.
3. Under **Manage manually**, enable **Auto-launch**, **Secondary launch**, and **Run in background**. Confirm if asked.

**Don't skip this on Honor.** If Clear all still stops capture, open ScreenGPT Settings, open recent apps, and use the app's lock option if available.

[Honor's instructions](https://www.honor.com/mea/support/content/en-us00406923/)

</details>

<details>
<summary><b>Samsung Galaxy — One UI</b></summary>

1. Open **Settings → Battery → Background usage limits**. On older versions, start with **Battery and device care → Battery**.
2. Check **Sleeping apps** and **Deep sleeping apps**. Remove **ScreenGPT** if listed.
3. Open **Never sleeping apps**, tap **+**, and add **ScreenGPT** if available.

Samsung's deep-sleep list prevents apps from running in the background. Adding an exception can increase battery use; leave the settings for other apps alone.

[Samsung's instructions](https://www.samsung.com/us/support/galaxy-battery/optimization/)

</details>

<details>
<summary><b>Google Pixel — Pixel Android</b></summary>

1. Open **Settings → Apps → App battery usage** and select **ScreenGPT**. You can also reach it through **Battery → Battery usage → View by apps** if listed.
2. Open **Allow background usage** and make sure background use is allowed.
3. Keep **Optimized** initially, then test the shortcut after leaving the app and clearing recent apps.

Google recommends Optimized for apps generally. A blanket battery exemption isn't a required Pixel setup step. If capture still stops, check Accessibility using the troubleshooting section below.

[Google's battery settings guide](https://support.google.com/pixelphone/answer/6090599?hl=en)

</details>

<details>
<summary><b>Motorola — steps vary by model</b></summary>

On models with **Manage background apps** (documented for moto g77):

1. Open **Settings → Battery → Manage background apps**.
2. Open **Background use → Smart use**.
3. Select **ScreenGPT → Always allow**.

Motorola says this menu is available only on models with more than 4 GB of RAM. Other versions have **Battery → Auto launch management**: allow ScreenGPT under **App auto launch** and **App secondary launch**, if those controls are present. Don't assume every Moto has both menus.

[Motorola's background-use guide](https://help.motorola.com/hc/3777/16/global/en-us/CGd0e521.html) · [Alternative launch controls](https://help.motorola.com/hc/1814/14/global/en-us/CG2007980805.html)

</details>

<details>
<summary><b>Xiaomi — HyperOS / MIUI</b></summary>

1. Search phone Settings for **Background autostart** or **Autostart** and enable it for **ScreenGPT**, if available.
2. If capture stops in the background, open **Settings → Battery**, select **ScreenGPT**, and choose **No restrictions**, if offered.
3. Test again after clearing recent apps.

Xiaomi documents the battery path for **Xiaomi 15**. Other HyperOS and MIUI versions may place the app's battery controls elsewhere; search Settings rather than changing unrelated options.

[Xiaomi 15 battery guide](https://www.mi.com/my/support/faq/details/KA-538010/) · [Xiaomi's autostart controls](https://trust.mi.com/docs/miui-privacy-white-paper-global/3/1)

</details>

<details>
<summary><b>Redmi — HyperOS / MIUI</b></summary>

1. Open **Settings → Apps → Permissions → Background autostart**.
2. Enable **ScreenGPT**.
3. If capture still stops, find ScreenGPT's battery settings and select **No restrictions**, if offered.

The autostart path is documented for **Redmi 13C**. On other Redmi models, search Settings for **Autostart** if the menu differs. The battery option follows Xiaomi's guidance where the same control is available.

[Redmi 13C autostart guide](https://www.mi.com/my/support/faq/details/KA-497677/) · [Xiaomi battery guide](https://www.mi.com/my/support/faq/details/KA-538010/)

</details>

<details>
<summary><b>POCO — HyperOS / MIUI</b></summary>

1. Search Settings for **Background autostart** or **Autostart**. Enable **ScreenGPT** if listed.
2. Open ScreenGPT's battery settings. If capture stops in the background and **No restrictions** is available, select it.
3. Test after clearing recent apps.

These steps use the shared Xiaomi software controls; they haven't been verified on a specific POCO model. Follow them only where your phone shows the matching options.

[Xiaomi's autostart controls](https://trust.mi.com/docs/miui-privacy-white-paper-global/3/1) · [Xiaomi battery guide](https://www.mi.com/my/support/faq/details/KA-538010/)

</details>

<details>
<summary><b>OnePlus — OxygenOS</b></summary>

1. Search phone Settings for **App battery management**.
2. Select **ScreenGPT** and enable **Allow background activity**.
3. Enable **Allow auto launch** too, if shown.

The linked guide demonstrates these phone controls with Huawei Health; use ScreenGPT instead. Older OxygenOS versions may use different names.

[Huawei's OnePlus setup guide](https://consumer.huawei.com/ca/support/content/en-us15848666/)

</details>

<details>
<summary><b>OPPO — ColorOS</b></summary>

1. Search phone Settings for **App battery management**.
2. Select **ScreenGPT** and enable **Allow background activity**.
3. Enable **Allow auto launch** if your version offers it.

The linked guide demonstrates these controls with Huawei Health; select ScreenGPT instead. Menu names vary across ColorOS releases.

[Huawei's OPPO setup guide](https://consumer.huawei.com/nz/support/content/en-us15848664/)

</details>

<details>
<summary><b>realme — realme UI</b></summary>

1. Open **Settings → Battery → App battery management → ScreenGPT**.
2. Allow **Foreground activity**, **Background activity**, and **Auto launch**, where shown.
3. If Clear all still stops capture, open ScreenGPT Settings and lock its card in recent apps using the card's menu or lock gesture, if available.

Some versions also put autostart under **Phone Manager → Privacy permissions → Auto-launch apps**.

[realme's support FAQ](https://www.realme.com/global/support/faq)

</details>

<details>
<summary><b>vivo — Funtouch OS</b></summary>

1. Open phone **Settings → Battery**.
2. Find **Background power consumption management** and select **ScreenGPT**.
3. Choose the option that allows the app to continue running in the background.

Use these steps only on versions with this menu. The linked manual doesn't establish the same path for every vivo or iQOO model, or for OriginOS.

[vivo's battery settings manual](https://eu-exstatic-vivofs.vivo.com/8Xa6evfY85lu15Pb/1658104689682/c8570d4b762027b070c0f05851682905.pdf)

</details>

<details>
<summary><b>Huawei — Android-compatible models with App launch</b></summary>

1. Search phone Settings for **App launch**.
2. Find **ScreenGPT** and disable **Manage automatically**.
3. Enable **Run in background**. If manual management also shows **Auto-launch** and **Secondary launch**, enable them.

This applies where your phone can install and run both Android apps from step 1. It is not a compatibility claim for every Huawei phone or HarmonyOS version.

[Huawei's background-app guide](https://consumer.huawei.com/uk/support/content/en-gb00428704/)

</details>

**Another phone, or different menus?** Open ScreenGPT's app info and look for battery/background-use controls. Allow background use if restricted, then run the test in step 5. These settings help reduce automatic cleanup; they cannot guarantee the app stays alive, override **Force stop**, or make temporary screen-sharing consent permanent.

### 4. Set up your button or gesture

Use the [guide for your phone](#phone-shortcuts) below. Choose **ScreenGPT** as the app your physical button or back-tap gesture opens. On supported Honor phones, assign it to **double-press power**.

If your phone doesn't offer a suitable shortcut, use the Quick Settings tile or optional floating button described below.

### 5. Try a capture

Open another app with your phone unlocked, then use your shortcut. ScreenGPT should capture the screen and open ChatGPT with the image. Add your question and send it when you're ready.

Also try clearing recent apps, then using the shortcut again. Repeat after leaving the phone idle for a while. If capture stops working, recheck [your background settings](#background-setup). On Honor, all three manual App launch switches should be on. If the accessibility switch was already turned off, enable it again once after changing the background settings.

> **To open ScreenGPT Settings later:** long-press its app icon → **Settings**. A normal tap starts capture once setup is done.

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

## Alternatives: tile or floating button

If your phone doesn’t offer a suitable button or gesture, use either of these.

| Quick Settings tile | Floating button |
| --- | --- |
| Expand Quick Settings → **Edit** → add **ScreenGPT**. Tap it over any app to capture. [Tile setup help](https://support.google.com/android/answer/9083864?hl=en). | In ScreenGPT Settings, turn on **Floating capture button**. It appears on the right. Tap to capture, drag to move. |
| Good if you want to keep the screen clear. | Drag onto **×** to hide it. Turn it back on in Settings. It hides itself during screenshots. |

<a id="help"></a>

## 🛠 Need help?

<details>
<summary><b>Capture stopped working, or the button disappeared</b></summary>

Check that **ScreenGPT screen capture** is still enabled in Accessibility. If it stopped after clearing recent apps, follow the [background setup for your brand](#background-setup). On Honor, all three manual App launch switches need to be enabled.

If it still stops, try locking ScreenGPT Settings in the recent-apps screen if your phone offers that option. If Android has switched screenshot access off, enable it again in Accessibility; ScreenGPT cannot grant itself that permission.

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
