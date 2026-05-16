# Grock Glasses Android

Native Android version of the Grock Glasses assistant. This repo is meant to be a real Android APK project, not just an HTML page.

## Current native features

- Real Android APK project using Gradle.
- Uses the xAI/Grok API, not OpenAI.
- Saves your `xai-...` API key locally on the phone.
- Uses xAI Responses API at `https://api.x.ai/v1/responses` with the `grok-4.3` model.
- Requests Android microphone permission with `RECORD_AUDIO`.
- Requests Bluetooth permissions and checks paired Bluetooth devices for Ray-Ban / Meta names.
- Opens the phone Bluetooth settings page for pairing.
- Foreground voice service with notification.
- Wake phrase support for **Hey Grok** / **Hey Grock**.
- Stores conversation memory locally on the phone.
- Speaks replies back using Android text-to-speech.

## What came from the gold HTML concept

The uploaded gold Grok Glasses HTML concept included the gold/dark UI direction, xAI key entry, microphone test behavior, Bluetooth status panel, wake-word controls, voice style testing, system status, and quick chat. This native version keeps those ideas but moves the important pieces into Android-native permissions and services.

## How to build the APK

1. Open this repo in Android Studio.
2. Let Gradle sync.
3. Build → Build Bundle(s) / APK(s) → Build APK(s).
4. Install the generated debug APK on your Android phone.

Or from a terminal with Gradle available:

```bash
gradle assembleDebug
```

The APK will be created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Phone setup notes

- Android may warn that a debug APK is from an unknown source.
- For continuous wake listening, allow microphone permission, notification permission, and unrestricted battery usage for the app.
- Bluetooth audio routing still depends on Android and the glasses firmware. Pair the glasses in Android Bluetooth settings first, then use the app to check whether they appear as paired.

## Important security note

For testing, the API key is stored locally on the phone. For a production app, the key should eventually be moved behind a small private backend so the key is not exposed inside the installed app or device storage.
