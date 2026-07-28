# Troubleshooting Data Not Appearing on Realtime Database

The user reports that data is not appearing on the Firebase Realtime Database. Based on my research, the application is currently implemented using **Cloud Firestore**, not **Realtime Database**. This is likely the reason why no data is visible in the Realtime Database tab of the Firebase Console.

## User Review Required

> [!IMPORTANT]
> The application code is currently written for **Cloud Firestore**. If you check the **Firestore** section in your Firebase Console, you should see your data there.
>
> Please confirm if you intended to use **Realtime Database** instead of Firestore. They are two different database products offered by Firebase.

## Open Questions

- Did you intend to use **Realtime Database** specifically, or were you just looking for your "real-time" data and checked the wrong tab?
- Are you seeing any error messages in the app's UI or Logcat when trying to save data?

## Proposed Changes

If you want to migrate from Cloud Firestore to Realtime Database, the following changes will be needed:

### [Firebase Configuration]

#### [MODIFY] [AppModule.kt](file:///C:/Users/Administrator/AndroidStudioProjects/LifeLine/app/src/main/java/com/perru/lifeline/di/AppModule.kt)
- Add a provider for `FirebaseDatabase`.
- Optionally remove `FirebaseFirestore` provider if it's no longer needed.

### [Data Repositories]

#### [MODIFY] [AuthRepositoryImpl.kt](file:///C:/Users/Administrator/AndroidStudioProjects/LifeLine/app/src/main/java/com/perru/lifeline/data/repository/AuthRepositoryImpl.kt)
- Replace `FirebaseFirestore` with `DatabaseReference`.
- Update CRUD operations to use Realtime Database syntax (e.g., `child().setValue()` instead of `collection().document().set()`).

#### [MODIFY] [RequestRepositoryImpl.kt](file:///C:/Users/Administrator/AndroidStudioProjects/LifeLine/app/src/main/java/com/perru/lifeline/data/repository/RequestRepositoryImpl.kt)
- Replace `FirebaseFirestore` with `DatabaseReference`.
- Update queries and transactions to use Realtime Database syntax.

## Verification Plan

### Manual Verification
1. Open the Firebase Console.
2. Navigate to **Firestore Database** and check if the data exists there (under `users`, `requests`, and `pledges` collections).
3. If migration is performed, verify that data appears in the **Realtime Database** tab.
