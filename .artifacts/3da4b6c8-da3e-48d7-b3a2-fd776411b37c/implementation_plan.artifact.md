# Google Login & UX Polish Implementation Plan

This plan outlines the integration of Google Login using the modern Credential Manager API and a comprehensive UX polish pass covering accessibility, validation feedback, skeleton loaders, and string externalization.

## User Review Required

> [!IMPORTANT]
> **Google Cloud Console Setup:** Google Login requires a Web Client ID configured in the Google Cloud Console. I will assume a placeholder or that it's already configured in the Firebase project.
> **Shimmer Library:** I will implement a custom shimmer modifier to avoid adding external library bloat unless preferred otherwise.

## Proposed Changes

### 1. Build & Dependencies

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Administrator/AndroidStudioProjects/LifeLine/app/build.gradle.kts)
- Add Credential Manager and Google ID dependencies.

---

### 2. Google Login Integration

#### [NEW] [GoogleSignInHelper.kt](file:///C:/Users/Administrator/AndroidStudioProjects/LifeLine/app/src/main/java/com/perru/lifeline/util/GoogleSignInHelper.kt)
- Create a helper class to encapsulate the `GetCredentialRequest` and interaction with the `CredentialManager`.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/LifeLine/app/src/main/java/com/perru/lifeline/presentation/auth/LoginScreen.kt)
- Update "Continue with Google" button to trigger the sign-in flow.
- Replace the placeholder logo with a standard Google icon (or a stylized version).

---

### 3. UX Polish: Accessibility & Strings

#### [MODIFY] [strings.xml](file:///C:/Users/Administrator/AndroidStudioProjects/LifeLine/app/src/main/res/values/strings.xml)
- Externalize hardcoded strings from all major screens.

#### [MODIFY] [All Screens]
- Add `contentDescription` to all interactive and meaningful image elements.
- Specifically fix the null descriptions in `DonorFeedScreen` and `LoginScreen`.

---

### 4. UX Polish: Validation Feedback

#### [MODIFY] [CreateRequestScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/LifeLine/app/src/main/java/com/perru/lifeline/presentation/hospital/CreateRequestScreen.kt)
- Add `supportingText` to fields (Units Needed, Contact Phone) to show inline validation errors instead of silent filtering.
- Update `Button` enabled state to reflect validation status.

---

### 5. UX Polish: Skeleton Loaders

#### [MODIFY] [CommonComponents.kt](file:///C:/Users/Administrator/AndroidStudioProjects/LifeLine/app/src/main/java/com/perru/lifeline/presentation/common/CommonComponents.kt)
- Implement a `ShimmerRequestCard` that mimics the layout of `RequestCard` but with animated grey boxes.
- Add a reusable `Modifier.shimmer()` extension.

#### [MODIFY] [DonorFeedScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/LifeLine/app/src/main/java/com/perru/lifeline/presentation/donor/DonorFeedScreen.kt)
- Show a list of `ShimmerRequestCard`s while `state.isLoading` is true (needs to be added to `DonorUiState`).

## Verification Plan

### Automated Tests
- `gradlew test` to ensure existing logic remains intact.

### Manual Verification
- Deploy to device/emulator.
- Test Google Login flow (success/cancel).
- Verify accessibility via TalkBack or Layout Inspector.
- Verify validation messages appear in `CreateRequestScreen`.
- Verify shimmer appears during feed refresh/load.
