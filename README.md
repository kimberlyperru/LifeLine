# LifeLine — Mobile Health & Blood Donation Platform

Package: `com.perru.lifeline` · Backend: Firebase Auth + Realtime Database · Uploads: Cloudinary (plain HTTPS, no native SDK)

## What changed in this pass

- **Firestore → Realtime Database.** Both repositories were rewritten against
  `FirebaseDatabase`. Data lives under three top-level nodes: `users/{uid}`,
  `requests/{requestId}`, `pledges/{pledgeId}`. Pledging uses an RTDB
  `Transaction.Handler` on the request node (RTDB's equivalent of Firestore's
  transactional read-modify-write) to safely increment `unitsPledged` even
  under concurrent pledges.
- **Cloudinary SDK removed.** The old `cloudinary-android` dependency bundled
  outdated native Fresco libraries that fail Android's 16 KB page-size
  alignment check (the compatibility dialog you saw on install). Uploads now
  go through a plain OkHttp multipart POST to Cloudinary's REST endpoint —
  same unsigned-preset flow, zero native code, warning gone for good.
- **Splash + onboarding.** A branded animated splash (gradient + logo scale-in)
  now runs first, followed by a one-time 3-page onboarding carousel (skippable,
  shown only once via SharedPreferences) before landing on sign-in.
- **Role switching.** Both dashboards have a swap-icon in their header. It
  resets the account's role to unset, which routes back through the existing
  role-selection/onboarding screen — profile data (name, city, blood group,
  hospital name) is kept, so switching back and forth doesn't lose anything.
- **Visual redesign.** Login/Sign Up/Role Selection/Donor Feed/Hospital
  Dashboard all got a gradient hero header with the logo in a soft circular
  badge, following the logo's terracotta/crimson/sage palette more directly.

## Setup

### 1. Firebase — enable Realtime Database

Firestore is no longer used. In the Firebase console: **Build → Realtime
Database → Create database**. Pick a region, start in locked mode, then
publish the rules from `realtime-database.rules.json` (Rules tab, paste and
publish).

**Important:** after enabling Realtime Database, re-download
`google-services.json` from Project settings and replace the one in `app/` —
the old file (generated before RTDB was enabled) won't contain the database
URL, and the app won't be able to connect without it.

### 2. Cloudinary

Same as before — an **unsigned** upload preset named to match
`CLOUDINARY_UPLOAD_PRESET` in `app/build.gradle.kts`, cloud name `sckangrp`
already set as `CLOUDINARY_CLOUD_NAME`.

### 3. Opening in Android Studio

Standard Gradle sync — minSdk 26. Package is `com.perru.lifeline` throughout
(all-lowercase, matching Java package convention and avoiding the
case-sensitivity issues that come up on Windows' case-insensitive filesystem
if a project ever ends up with two folders differing only by case).

## Package structure

```
com.perru.lifeline
├── data
│   ├── remote        # CloudinaryUploader (OkHttp multipart, no native SDK)
│   └── repository    # Realtime Database-backed repository implementations
├── di                # Hilt modules (Firebase Auth + Database, repo bindings)
├── domain
│   ├── model          # LifeLineUser, BloodRequest, Pledge, enums
│   └── repository     # Repository interfaces (data layer contracts)
├── presentation
│   ├── splash            # Branded animated splash
│   ├── onboarding        # One-time 3-page onboarding carousel
│   ├── auth               # Login, SignUp, RoleSelection + AuthViewModel
│   ├── donor                # DonorFeed (+ role switch), RequestDetail
│   ├── hospital               # HospitalDashboard (+ role switch), CreateRequest
│   ├── common                # Shared composables (RequestCard, UrgencyBadge, ...)
│   └── navigation              # Screen routes + auth-driven NavHost
├── ui/theme            # Cozy cream/terracotta/sage Compose theme
└── util               # BloodCompatibility matrix, OnboardingPrefs
```

## Still on the roadmap

- Nutrition Hub, Hemoglobin Log (per original spec, not yet built)
- True geo-radius distance filtering (currently city-text based)
- Push notifications for new matching requests
