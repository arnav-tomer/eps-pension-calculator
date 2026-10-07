# Build the APK without Android Studio

## 1. Create a GitHub repository
Create a new GitHub repository, for example `eps-pension-calculator`.

## 2. Upload the project
Extract this ZIP on your PC. Upload the contents so the repository contains the `EPSPensionNative` folder and its files.

## 3. Important workflow location
The file below must be exactly here:
`.github/workflows/build-apk.yml`

In this package it is already included inside `EPSPensionNative/.github/workflows/`.

## 4. Run the build
Open the repository on GitHub > Actions > `Build Android APK` > `Run workflow` > select the `main` branch > `Run workflow`.

## 5. Download the APK
Wait for the green check. Open the workflow run and scroll to **Artifacts**. Download `EPS-Pension-Calculator-debug-apk`.

Extract the downloaded artifact ZIP. Inside it is `app-debug.apk`.

## Notes
- No Android Studio is required on your PC for this cloud build.
- The workflow uses JDK 17, Android SDK 34 and Gradle 8.7 to match this project.
- This is a debug APK. It is suitable for testing/installing on an Android phone. A Play Store release build requires signing/configuration separately.
