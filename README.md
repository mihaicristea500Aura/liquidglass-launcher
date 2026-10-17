# Liquid Glass Launcher

A native Android launcher starter inspired by HyperOS motion and Liquid Glass styling. Built with Kotlin and Android Views, with no third-party runtime dependencies.

## Features in this starter

- Home-screen activity that can be selected as the default launcher
- Floating translucent dock using installed app icons
- App drawer that lists launchable apps
- Dock app selection (long-press the dock)
- Launcher Studio settings for blur amount, animation speed, and accent palette
- Fade/scale transitions between launcher screens
- Android 12+ RenderEffect blur where supported; translucent glass fallback on older Android versions
- Preferences saved locally on the device

## Device target

Designed as a starting point for Android 8.0+ devices, including Samsung Galaxy S9+ Exynos (`star2lte`). Actual blur quality and system behavior depend on Android version and vendor software.

## Build an APK on GitHub

1. Create a new GitHub repository.
2. Upload all files from this project (or use **Add file → Upload files** with the ZIP contents).
3. Open **Actions**. The included workflow builds a debug APK automatically.
4. Open the completed workflow run and download the `LiquidGlassLauncher-debug-apk` artifact.
5. Install the APK on your phone. For Android to use it as the home app, open **Settings → Apps → Choose default apps → Home app** and select Liquid Glass Launcher (wording may vary).

You can also open this project in Android Studio and run `:app:assembleDebug`.

## Important limitations

- This is a starter project, not a verified release build. Build it in GitHub Actions or Android Studio before installing.
- Android restricts what a third-party launcher can animate. It cannot replace all system app-opening/closing animations or force system-wide blur.
- The current dock picker and theme controls are intentionally simple; further work can add icon packs, widget hosting, folder editing, search, gesture configuration, and export/import of themes.
- `QUERY_ALL_PACKAGES` is used because a launcher needs to discover installed launchable apps. Review Google Play policy requirements before publishing publicly.

## Project structure

- `app/src/main/java/dev/solys/liquidglass/MainActivity.kt` — launcher UI and behavior
- `app/src/main/AndroidManifest.xml` — launcher intents
- `.github/workflows/android.yml` — GitHub Actions APK build

## License

MIT. See `LICENSE`.
