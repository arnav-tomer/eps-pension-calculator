EPS Pension Calculator - native Android app (Kotlin + Jetpack Compose)

1. Install Android Studio (Koala 2024.1 or newer; JDK 17 is bundled).
2. File > Open > select this folder (EPSPensionNative). Let Gradle sync finish (needs internet the first time).
3. Run on a phone (USB debugging on) or an emulator with the green Run button.
4. To get an APK: Build > Build Bundle(s) / APK(s) > Build APK(s).
   Output: app/build/outputs/apk/debug/app-debug.apk. Copy it to your phone and install it.

Code:
- app/src/main/java/com/example/epspension/EpsCalc.kt     calculation logic and ceiling history
- app/src/main/java/com/example/epspension/MainActivity.kt   screen (Compose)
