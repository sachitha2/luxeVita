# Fix: Android Studio "Module not specified"

This guide fixes the Run Configuration error **Module not specified** / **&lt;no module&gt;** so the TechCare Services app can deploy to a device (e.g. Pixel 6).

## What was fixed (latest)

- **Run configuration was missing `<module name="TechCareServices.app" />`** — this is the direct cause of "Module not specified"
- **App module had no Android facet** — `.idea/modules/app/TechCareServices.app.iml` now includes the `android-gradle` facet
- Added `.idea/AndroidProjectSystem.xml` so Android Studio treats this as an Android Gradle project (not plain Java)
- Added `.idea/sdk.xml` and set JDK 17 in `misc.xml` / `gradle.xml`

## Package / namespace note

Java sources use package `com.example.ecostay` (kept to avoid breaking existing TechCare code). Gradle `namespace` and `applicationId` match: `com.example.ecostay`.

## Steps in Android Studio

1. **Open the correct folder**  
   Open `/Users/sachitha/AndroidStudioProjects/EcoStay` (the folder that contains `settings.gradle` and the `app/` directory).

2. **Set Gradle JDK to Embedded JDK**  
   You are on **Android Studio 2021.1** (Java 11). The project now uses **AGP 7.2.2** (compatible with your Studio).  
   - **Settings** → **Build Tools** → **Gradle**  
   - **Gradle JDK**: **Embedded JDK** (`/Applications/Android Studio.app/Contents/jre/Contents/Home`)

3. **Sync Gradle** (required — do this before editing Run Configuration)
   - **File** → **Sync Project with Gradle Files**
   - Open the **Build** tool window and confirm sync finishes **without errors**
   - If sync fails, fix that first (SDK path, JDK 17, network)

4. **Fix Run Configuration**
   - **Run** → **Edit Configurations…**  
   - Delete any broken configuration showing `<no module>`  
   - Click **+** → **Android App**  
   - Name: `app`  
   - **Module**: select **TechCareServices.app** (if empty, sync Gradle first — see step 3)  
   - **Launch**: Default Activity  
   - **Apply** → **OK**

5. **Connect Pixel 6**  
   - Enable **Developer options** and **USB debugging**  
   - Connect via USB and accept the debugging prompt on the phone  
   - Select the device in the toolbar dropdown

6. **Run**  
   Click **Run** (green play button) with configuration **app**.

## Verify from terminal

```bash
export JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/ms-17.0.19/Contents/Home"
cd /Users/sachitha/AndroidStudioProjects/EcoStay
./gradlew clean :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## If sync still fails

- Confirm `local.properties` exists with `sdk.dir` pointing to your Android SDK (Android Studio creates this automatically).
- Install **Android SDK Platform 34** via **SDK Manager** (matches `compileSdk 34` in `app/build.gradle`).
- **File** → **Invalidate Caches** → **Invalidate and Restart**, then sync again.
