# Grock Glasses Android

Native Android version of the Grock Glasses assistant.

## What it does
- Real Android APK project, not just an HTML file
- Requests phone permissions for microphone, Bluetooth, notifications, foreground service, and internet
- Foreground background voice service with notification
- Listens for the wake phrase: **Hey Grok** / **Hey Grock**
- Stores conversation memory locally on the phone
- Saves your xAI API key locally on the phone
- Uses xAI Responses API with `web_search` enabled for current internet-backed answers
- Checks paired Bluetooth devices for Ray-Ban / Meta glasses names

## Important notes
Android may still stop continuous speech recognition depending on battery settings, lock screen settings, and manufacturer restrictions. Keep the app unrestricted in Android battery settings for best results.

For production security, the API key should eventually be moved to a small private backend instead of stored directly on the phone.
