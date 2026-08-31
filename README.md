# ScreenGPT 🚀

An Android floating widget assistant that bridges whatever you are currently looking at on your screen directly into the official **ChatGPT** Android app with a single tap.

---

## ✨ Features

- **⚡ 1-Tap Instant Workflow**: Tap the floating bubble over any app, game, browser, video, or PDF $\rightarrow$ ScreenGPT silently captures the screen in $<20\text{ms}$, copies the screenshot to your clipboard, and launches the official ChatGPT app with the image ready to paste and ask.
- **🔒 Zero API Key / No Billing**: Directly bridges with your existing official ChatGPT Android app (`com.openai.chatgpt`) and account.
- **🪟 Persistent Screen Projection**: Configured with Android 14/15/16 foreground service lifecycles so you only grant screen permission **once** when starting the widget, with unlimited instant captures thereafter.
- **📋 Smart Clipboard & OCR**: Copies rich image stream and runs on-device Google ML Kit text recognition offline.
- **🎨 Native Kotlin Performance**: Built 100% natively in Kotlin for minimal battery consumption and smooth 60fps overlay animations.

---

## 🛠 Tech Stack

- **Language**: Kotlin 2.0+
- **Platform**: Android SDK 35 (Android 14 / Android 15 / Android 16 compatible)
- **Architecture**: Foreground Service (`mediaProjection | specialUse`) + WindowManager Application Overlays
- **Libraries**:
  - Google Play Services ML Kit Text Recognition
  - Android Jetpack Core KTX & Activity KTX
  - AndroidX Security Crypto MasterKey
  - Google Material Design 3 Components

---

## 🚀 How to Build & Install

1. Clone this repository:
   ```bash
   git clone git@github.com:npoptomov/screen-chatgpt-overlay.git
   cd screen-chatgpt-overlay
   ```
2. Build the Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
3. Install on your connected device:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📱 How to Use

1. Open **ScreenGPT** on your phone.
2. Grant **Overlay Permission** (Allow display over other apps).
3. Tap **Start Floating Widget** and choose **Entire screen $\rightarrow$ Start now** once.
4. Go to any app and tap the floating bubble anytime to ask ChatGPT about what's on your screen!

---

## 📄 License
MIT License
