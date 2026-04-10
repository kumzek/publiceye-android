# PublicEye — Developer Setup Guide

Follow these steps exactly, in order. Do not skip Firebase setup — the app will not compile without `google-services.json`.

---

## Step 1: Prerequisites

Install the following before opening the project:

- **Android Studio** (latest stable — Ladybug or newer)
- **JDK 17** (bundled with Android Studio, or install separately)
- **Git**

---

## Step 2: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/publiceye-android.git
cd publiceye-android
```

---

## Step 3: Create a Firebase Project

1. Go to [https://console.firebase.google.com](https://console.firebase.google.com)
2. Click **Add project** → Name it `PublicEye`
3. Enable Google Analytics → Continue

### Enable Authentication
4. Go to **Authentication → Sign-in method**
5. Enable **Google** (set a support email)
6. Enable **Email/Password**
7. Copy the **Web Client ID** from Google Sign-In settings — you'll need it in Step 5

### Enable Firestore
8. Go to **Firestore Database → Create database**
9. Start in **Test mode** (we'll lock down rules before launch)
10. Choose a region close to your users (e.g., `asia-south1` for India)

### Enable Storage
11. Go to **Storage → Get started**
12. Use default rules for now

### Enable Cloud Messaging
13. Go to **Cloud Messaging** — it's enabled by default

### Enable Crashlytics
14. Go to **Crashlytics → Get started**

---

## Step 4: Download google-services.json

1. In Firebase Console → Project settings (gear icon) → **General**
2. Scroll to **Your apps** → Add app → Android
3. Package name: `com.publiceye.app`
4. App nickname: `PublicEye`
5. Download `google-services.json`
6. Place it at: `app/google-services.json` (replace the placeholder if present)

---

## Step 5: Add Your Web Client ID

1. Open `app/build.gradle.kts`
2. Find this line:
   ```kotlin
   buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"YOUR_WEB_CLIENT_ID_HERE\"")
   ```
3. Replace `YOUR_WEB_CLIENT_ID_HERE` with the Web Client ID from Step 3

---

## Step 6: Add SHA-1 Fingerprint (for Google Sign-In)

Google Sign-In requires your app's SHA-1 certificate fingerprint in Firebase.

```bash
# Get debug SHA-1
./gradlew signingReport
```

Copy the SHA-1 from the `debug` variant, then:
1. Firebase Console → Project settings → Your apps → `com.publiceye.app`
2. **Add fingerprint** → paste SHA-1 → Save

---

## Step 7: Open in Android Studio

1. Open Android Studio → **Open** → select the `publiceye-android` folder
2. Wait for Gradle sync to complete
3. Run on an emulator or physical device (API 26+)

---

## Step 8: Create GitHub Repository & Push

```bash
git init   # (if not already a git repo)
git remote add origin https://github.com/YOUR_USERNAME/publiceye-android.git
git add .
git commit -m "feat: Phase 1 — project scaffold, auth, notification rationale"
git push -u origin main
```

The GitHub Actions CI will automatically run lint + build on every push.

---

## Project Structure

```
publiceye-android/
├── app/
│   ├── src/main/java/com/publiceye/app/
│   │   ├── MainActivity.kt               ← App entry point
│   │   ├── PublicEyeApp.kt               ← Hilt application class
│   │   ├── di/AppModule.kt               ← Dependency injection
│   │   ├── navigation/
│   │   │   ├── Screen.kt                 ← All route definitions
│   │   │   └── NavGraph.kt               ← Navigation host
│   │   ├── ui/
│   │   │   ├── theme/                    ← Brand colors, theme, typography
│   │   │   ├── auth/                     ← Login, SignUp, AuthViewModel
│   │   │   ├── onboarding/               ← Notification rationale screen
│   │   │   └── home/                     ← Placeholder (Phase 2)
│   │   └── data/
│   │       ├── model/User.kt             ← User data class
│   │       └── repository/AuthRepository.kt
│   ├── google-services.json              ← YOUR file (not committed to git)
│   └── build.gradle.kts
├── gradle/libs.versions.toml             ← All dependency versions
├── .github/workflows/android.yml         ← CI/CD
└── SETUP.md                              ← This file
```

---

## What's Built (Phase 1)

- ✅ Login screen (email/password + Google Sign-In)
- ✅ Sign-up screen with validation
- ✅ Firebase Auth integration (Google + email)
- ✅ Auto-create Firestore user document on first sign-in
- ✅ Account deletion (Play Store requirement)
- ✅ Notification rationale screen (skippable, first-launch only)
- ✅ Full navigation graph (auth-gated)
- ✅ Brand theme (civic blue + amber, light mode)
- ✅ CI/CD via GitHub Actions

## Coming Next (Phase 2)

- Map view (Google Maps SDK)
- Issue feed
- Report submission (photo + GPS)
- Issue detail screen

> ⚠️ Do not build Phase 2 screens until wireframes are reviewed and approved.
