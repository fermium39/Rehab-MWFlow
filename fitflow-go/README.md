# FitFlow Go — Android offline build

Standalone Android WebView app with bundled HTML, CSS, JS and all PNG assets. The existing GitHub Pages app is unchanged.

## Build

Run GitHub Actions workflow **Build FitFlow Go APK** and download the `fitflow-go-debug-apk` artifact. The workflow copies the current website `index.html` and `assets/` into the Android app and removes web-only service worker registration and update controls. No network permission is requested by the app.

To build locally: from this directory, run `gradle :app:assembleDebug` with JDK 17, Android SDK 35, and Gradle 8.9. Before building, populate `app/src/main/assets/` with the website's `index.html` and `assets/` directory, and apply the transformations in the workflow.

The debug APK is for personal testing. For durable upgrades, configure a private release signing key and preserve it between releases. Web and Android localStorage do not synchronize. Test on the device in airplane mode before relying on it.
