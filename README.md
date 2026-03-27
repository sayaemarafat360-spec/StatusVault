# SnapVault: Status Saver

A premium WhatsApp Status Saver app built with Kotlin and Jetpack Compose.

## Features

- 📱 Auto-detect viewed WhatsApp statuses
- 💾 Save images and videos with one tap
- 📁 Bulk save multiple statuses
- ❤️ Favorites system with persistence
- 🔒 Hidden Vault with PIN protection (Premium)
- 🎨 Sleek, modern dark UI
- ✨ Smooth Lottie animations
- 🚀 Fast and lightweight
- 🔔 Advanced notification engine with smart reminders
- 📊 Room Database for full data persistence

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Repository Pattern
- **DI**: Hilt
- **Database**: Room
- **Image Loading**: Coil
- **Animations**: Lottie
- **Video Playback**: Media3 ExoPlayer
- **Ads**: AdMob
- **Background Tasks**: WorkManager
- **Storage**: MediaStore API (Scoped Storage)

## Requirements

- Android 8.0 (API 26) or higher
- WhatsApp installed on the device

## Building

### First Time Setup (Unix/Linux/macOS)
```bash
# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug
```

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## 🎨 Replacing Lottie Animations

Placeholder Lottie JSON files are located at:

```
app/src/main/assets/
├── onboarding_step1.json    → Replace with "watch statuses" animation
├── onboarding_step2.json    → Replace with "return to app" animation
├── onboarding_step3.json    → Replace with "save & organize" animation
├── save_success.json        → Replace with "checkmark/success" animation
├── loading.json             → Replace with "loading spinner" animation
├── empty_state.json         → Replace with "no statuses" animation
├── empty_folder.json        → Replace with "empty folder" animation
├── empty_vault.json         → Replace with "empty vault" animation
├── permission.json          → Replace with "permission shield" animation
├── vault_lock.json          → Replace with "vault/lock" animation
├── premium_lock.json        → Replace with "premium lock" animation
└── premium_crown.json       → Replace with "premium crown" animation
```

### How to Replace:

1. Download animations from [LottieFiles](https://lottiefiles.com/) (free account)
2. Search for relevant animations (e.g., "success", "loading", "empty state")
3. Download as JSON format
4. Replace the placeholder files with your downloaded files
5. Keep the same filename or update the code reference

### Recommended Lottie Animations:

| File | Recommended Search Terms |
|------|-------------------------|
| onboarding_step1.json | "phone", "watch", "eye" |
| onboarding_step2.json | "arrow", "refresh", "back" |
| onboarding_step3.json | "download", "save", "folder" |
| save_success.json | "checkmark", "success", "done" |
| loading.json | "loading", "spinner", "progress" |
| empty_state.json | "empty", "no data", "search" |
| premium_crown.json | "crown", "premium", "star" |

## 📢 Replacing AdMob IDs

The app uses test AdMob IDs. Replace with your real IDs in these locations:

### 1. AndroidManifest.xml
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="YOUR_REAL_APP_ID" />
```

### 2. AdManager.kt
```kotlin
// Located at: app/src/main/java/com/zuvix/snapvault/ads/AdManager.kt

private const val INTERSTITIAL_AD_ID = "YOUR_REAL_INTERSTITIAL_ID"
private const val REWARDED_AD_ID = "YOUR_REAL_REWARDED_ID"
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/zuvix/snapvault/
│   │   ├── ads/                # AdMob integration
│   │   ├── data/
│   │   │   ├── local/          # Local data sources
│   │   │   │   ├── database/   # Room database
│   │   │   │   ├── FileManager.kt
│   │   │   │   ├── StatusScanner.kt
│   │   │   │   └── VideoThumbnailExtractor.kt
│   │   │   ├── model/          # Data models
│   │   │   └── repository/     # Repositories
│   │   ├── di/                 # Dependency injection
│   │   ├── domain/
│   │   │   ├── model/          # Domain models
│   │   │   └── usecase/        # Use cases
│   │   ├── service/
│   │   │   └── notification/   # Advanced notification engine
│   │   ├── ui/
│   │   │   ├── components/     # Reusable components
│   │   │   ├── navigation/     # Navigation
│   │   │   ├── screens/        # Screen composables
│   │   │   └── theme/          # Theme and styling
│   │   ├── util/               # Utilities
│   │   ├── MainActivity.kt
│   │   └── SnapVaultApp.kt
│   ├── assets/                 # Lottie animations (REPLACE THESE)
│   └── res/                    # Resources
└── build.gradle.kts
```

## Permissions

- `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` (Android 13+)
- `READ_EXTERNAL_STORAGE` (Android 12 and below)
- `INTERNET` (for AdMob)
- `POST_NOTIFICATIONS` (for notifications)
- `FOREGROUND_SERVICE` (for background monitoring)

## Codemagic CI/CD

Two workflows are configured in `codemagic.yaml`:
- **SnapVault-Debug**: Builds debug APK
- **SnapVault-Release**: Builds release APK with ProGuard

Both trigger on push to `main` branch.

## License

Copyright © 2024 ZuvixApps. All rights reserved.

## Disclaimer

This app is not affiliated with WhatsApp or Meta. Users are responsible for content they save.
