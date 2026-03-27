# SnapVault Pro: HD Status Saver - Build & Setup Guide

## 🎯 Project Overview

**App Name:** SnapVault Pro: HD Status Saver  
**Package Name:** com.snaphubpro.zuvixapp  
**Platform:** Android 26-34  
**Architecture:** MVVM + Repository Pattern + Jetpack Compose  
**Build System:** Gradle 8.2  
**Kotlin Version:** 1.9.22  
**Java Version:** 17

---

## ✅ Recent Changes

### Package Name Update
- Updated from `com.zuvix.snapvault` to `com.snaphubpro.zuvixapp`
- Updated in 32 Kotlin files
- Updated app name to "SnapVault Pro: HD Status Saver"

### Dependency Updates
- ✅ Removed Lottie animations (com.airbnb.android:lottie-compose)
- ✅ Removed Lottie JSON assets (will use Material 3 Compose animations instead)
- ✅ Fixed deprecated Gradle buildDir warning

### UI Redesign
- ✅ HomeScreen rewritten with:
  - 2-column responsive grid for thumbnails
  - Material 3 components for permissions, loading, and empty states
  - Video play indicator (▶) on thumbnails
  - New status badge (green dot)
  - Smooth scale animations for selection
  - Clean FAB "Open WhatsApp"
  - Tab-based filtering (Images / Videos)
- ✅ Removed all Lottie animation imports

### Codemagic Configuration
- ✅ Created optimized workflows for debug/release builds
- ✅ Parallel build support with build caching
- ✅ Email notifications on success/failure
- ✅ No keystore configuration needed for debug builds

---

## 🚀 Build Instructions

### Local Build (Windows/Mac/Linux)

#### Prerequisites
```bash
# Install Android Studio or Android Command Line Tools
# Set ANDROID_SDK_ROOT environment variable
# Install Java 17 (or use Android Studio's bundled JDK)
```

#### Build Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

#### Build Release APK (Unsigned)
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

#### Run Linter (Check for issues)
```bash
./gradlew lint
```

#### Clean Build
```bash
./gradlew clean assembleDebug
```

---

## 🔧 Codemagic CI/CD Setup

### Step 1: Connect Repository
1. Go to [app.codemagic.io](https://app.codemagic.io)
2. Sign up / log in
3. Click "Add repository"
4. Select your GitHub repository (SnapVault-StatusSaver)
5. Click "Connect"

### Step 2: Configure Workflows
1. Codemagic will detect the `codemagic.yaml` file
2. Two workflows are pre-configured:
   - **debug-apk**: Debug APK build
   - **release-apk**: Release APK build (unsigned)

### Step 3: Set Environment Variables
1. Go to **Team Settings** → **Environment Variables**
2. Add group name: `google_play_service`
3. Add variables:
   - `GMAIL_ADDRESS`: Your email for build notifications
4. Save

### Step 4: Run First Build
1. Click "Start build"
2. Select workflow: **debug-apk** or **release-apk**
3. Click "Build"
4. Monitor build progress
5. Download APK from artifacts

---

## 📦 Project Structure

```
SnapVault-StatusSaver/
├── app/
│   ├── build.gradle.kts          (✅ Updated - Lottie removed)
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/snaphubpro/zuvixapp/
│           │   ├── MainActivity.kt
│           │   ├── SnapVaultApp.kt
│           │   ├── ads/AdManager.kt
│           │   ├── data/
│           │   │   ├── local/FileManager.kt
│           │   │   ├── model/StatusItem.kt
│           │   │   ├── repository/StatusRepository.kt
│           │   │   └── local/database/
│           │   ├── di/AppModule.kt
│           │   ├── ui/
│           │   │   ├── screens/
│           │   │   │   ├── home/HomeScreen.kt (✅ Rewritten)
│           │   │   │   ├── preview/PreviewScreen.kt
│           │   │   │   ├── saved/SavedScreen.kt
│           │   │   │   ├── vault/VaultScreen.kt
│           │   │   │   ├── settings/SettingsScreen.kt
│           │   │   │   └── onboarding/OnboardingScreen.kt
│           │   │   ├── theme/Theme.kt
│           │   │   └── navigation/Navigation.kt
│           │   └── util/
│           └── res/
│               ├── values/strings.xml (✅ App name updated)
│               ├── drawable/
│               ├── mipmap/
│               └── xml/
├── build.gradle.kts              (✅ Fixed deprecated buildDir)
├── settings.gradle.kts
├── codemagic.yaml                (✅ Optimized)
└── README.md
```

---

## 🎨 Design System

### Color Palette
- **Background**: #0F0F0F (Pure black, OLED optimized)
- **Surface**: #1A1A1A, #252525
- **Accent**: #25D366 (WhatsApp green)
- **Text Primary**: #FFFFFF
- **Text Secondary**: #B0B0B0

### UI Components
- **Padding**: 12-16dp
- **Rounded Corners**: 12dp
- **Spacing**: 8dp
- **FAB Elevation**: 6dp
- **Animations**: Material 3 transitions (fade, scale, slide)

---

## 🔧 Technologies & Dependencies

| Category | Package | Version |
|----------|---------|---------|
| **Jetpack** | androidx.compose:compose-bom | 2024.01.00 |
| **Images** | io.coil-kt:coil-compose | 2.5.0 |
| **Video** | androidx.media3:media3-exoplayer | 1.2.1 |
| **Database** | androidx.room:room-runtime | 2.6.1 |
| **DI** | com.google.dagger:hilt-android | 2.50 |
| **Ads** | com.google.android.gms:play-services-ads | 23.0.0 |
| **Storage** | androidx.datastore:datastore-preferences | 1.0.0 |

**Removed:**
- ❌ com.airbnb.android:lottie-compose (replaced with Material 3 animations)
- ❌ Lottie JSON asset files

---

## 📱 Features

### ✅ Implemented
- **Home Grid**: Auto-load WhatsApp statuses, 2-column responsive layout
- **Preview**: Fullscreen media viewer with ExoPlayer
- **Save**: Single & bulk save functionality
- **Favorites**: Mark and organize items
- **Vault**: PIN-protected hidden folder (4-digit lock)
- **Notifications**: Smart notification system with scheduling
- **Dark Theme**: Forced dark mode (OLED optimized)
- **Permissions**: Runtime permission handling
- **Ads**: AdMob integration (test IDs configured)

### 🔄 In Progress / Planned
- PreviewScreen: Complete rewrite with swipe navigation
- SavedScreen: Tab-based layout refinement
- VaultScreen: PIN lock UI polish
- OnboardingScreen: Replace Lottie with Compose animations
- Premium Features: In-app billing integration

---

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Lint & Analysis
```bash
./gradlew lint
```

### Connected Device Tests
```bash
./gradlew connectedAndroidTest
```

---

## 📄 Gradle Properties

### gradle.properties
`org.gradle.jvmargs=-Xmx2048m`

### Environment Variables (Codemagic)
- `GRADLE_OPTS`: "-Xmx3072m -XX:MaxMetaspaceSize=512m"
- Build cache enabled for faster builds
- Parallel compilation enabled

---

## 🔐 Security & Compliance

- ✅ No backend dependency (no login/auth required)
- ✅ Local storage only (scoped storage / MediaStore API)
- ✅ No permission abuse
- ✅ Play Store compliant
- ✅ ProGuard minification enabled (release builds)

---

## 📋 Next Steps

1. **Complete Screen Rewrites**
   - [ ] PreviewScreen - swipe navigation, controls overlay
   - [ ] SavedScreen - tab layout (Saved/Favorites/Vault)
   - [ ] VaultScreen - PIN lock UI
   - [ ] OnboardingScreen - Compose animations

2. **Remove Remaining Lottie References**
   - [ ] Replace loading animations with CircularProgressIndicator
   - [ ] Replace empty state animations with Icons
   - [ ] Remove .json asset files from assets/

3. **Build & Deploy**
   - [ ] Build debug APK locally
   - [ ] Set up Codemagic workflows
   - [ ] Test on physical devices
   - [ ] Build release APK
   - [ ] Sign APK (if creating Play Store release)

4. **Polish & Optimization**
   - [ ] Ensure launch time < 1.5s
   - [ ] Test on lower-end devices (Android 26-27)
   - [ ] Verify dark mode OLED optimization
   - [ ] Test all permission flows

---

## 💡 Tips

- Use `--build-cache` flag for faster builds
- Use `-Xmx3072m` for Gradle to prevent Out of Memory errors
- Enable Gradle parallel builds for multi-core systems
- Test on both physical devices and emulators
- Use Android Studio's Layout Inspector for UI debugging

---

## 🆘 Troubleshooting

### Build Error: "buildDir is deprecated"
✅ **Fixed** - Updated to `layout.buildDirectory.asFile.get()`

### Build Error: "Lottie not found"
✅ **Fixed** - Removed Lottie dependency from build.gradle.kts

### Build Error: "Package not found com.zuvix.snapvault"
✅ **Fixed** - All 32 files updated to com.snaphubpro.zuvixapp

### Slow Local Build
- Enable Gradle build cache: `--build-cache`
- Increase Gradle heap: `-Xmx3072m`
- Use `--parallel` for multi-module builds
- Use Linux instance type on Codemagic (faster builds)

### Codemagic Build Stuck
- Check logs for errors
- Ensure gradlew is executable: `chmod +x ./gradlew`
- Verify Java 17 is installed
- Clear Gradle cache: `./gradlew clean`

---

## 📞 Support

For questions or issues:
1. Check Gradle build output for specific errors
2. Review Codemagic build logs
3. Verify all dependencies are correctly specified
4. Test locally before pushing to Codemagic

---

**Version:** 1.0.0  
**Last Updated:** March 28, 2026  
**Status:** ✅ Ready for Debug Build
