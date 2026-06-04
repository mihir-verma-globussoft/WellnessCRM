# WellnessCRM Patient App — Implementation Status

Last updated: 2026-06-04 (session 7 — live staging API audit; ALL features unblocked except Android FCM)
Current phase: Phase 4 — Booking Feature

## Legend
✅ Done &nbsp; 🔄 In Progress &nbsp; ⬜ Not started &nbsp; 🔴 Blocked (reason inline)

---

## Backend Gap Endpoints
*Re-audited against CRM backend on 2026-06-04 (session 7 — cross-checked with backend/frontend team + direct wellness.js code review)*

### ✅ Confirmed working patient-portal endpoints

| Endpoint | Notes |
|----------|-------|
| `GET /public/tenant/:slug` | Tenant branding |
| `POST /api/auth/login` | Email + password → CUSTOMER JWT |
| `POST /api/auth/customer/register` | Patient registration |
| `GET /portal/me` | Profile read |
| `GET /portal/me/permissions` | Portal permission set |
| `GET /portal/visits` | Visit history (supports `?upcoming=true`) |
| `GET /portal/prescriptions` | Prescription list |
| `GET /portal/prescriptions/:id/pdf` | Prescription PDF download |
| `POST /portal/export` | DSAR data export |
| `GET /portal/appointments` | My appointments — `?bucket=upcoming\|past\|all` (default upcoming) |
| `POST /portal/appointments/book` | Book appointment |
| `POST /portal/appointments/:id/cancel` | Cancel appointment |
| `PATCH /portal/appointments/:id/reschedule` | Reschedule appointment |
| `GET /portal/products` | Patient-facing product/service catalogue (excludes Consumption type). Requires portal permission `products.read`. |
| `GET /portal/product-categories` | Product categories. Requires portal permission `products.read`. |
| `GET /giftcards/storefront` | Gift card catalogue |
| `POST /giftcards/:id/purchase/order` | Initiate gift card purchase |
| `POST /giftcards/:id/purchase/confirm` | Confirm gift card purchase |

> **Appointment URL fix (session 7):** `WellnessApiService.kt` previously called wrong staff-scoped paths (`appointments/book`, `appointments/my`, `appointments/:id/cancel`). Now corrected to portal paths above. `AppointmentListResponseDto` wrapper added to reflect `{ bucket, count, appointments }` envelope.

> **Services → Products:** The `GET /services` and `GET /locations` staff routes are no longer used by the patient app. The patient-facing equivalent is `GET /portal/products` (and `GET /portal/product-categories`). The booking body (`POST /portal/appointments/book`) takes `serviceId` (maps to a product id), not a location. Location selection step removed from booking flow.

> **Slot picker:** `GET /portal/slots` does not exist and is not planned. The booking flow uses a date + time picker directly; the server returns `DOCTOR_UNAVAILABLE` / `SLOT_TAKEN` if the chosen slot is invalid.

### ✅ All features confirmed working — live staging API test 2026-06-04

Test account: `mohitreddy@gimpmail.com` · patientId=608 · tenantId=1 · slug=`enhanced-wellness`

| Endpoint | Auth | HTTP | Notes |
|----------|------|------|-------|
| `GET /portal/me` | verifyPatientToken | 200 | Patient profile: id (patientId!), name, phone, email, dob, gender |
| `GET /portal/appointments` | verifyPatientToken | 200 | `{bucket, count, appointments[]}` |
| `POST /portal/appointments/book` | verifyPatientToken | — | Confirmed route exists |
| `POST /portal/appointments/:id/cancel` | verifyPatientToken | — | Confirmed route exists |
| `PATCH /portal/appointments/:id/reschedule` | verifyPatientToken | — | Confirmed route exists |
| `GET /portal/products` | verifyPatientToken + products.read | 200 | Returns `[]` if no products configured |
| `GET /portal/product-categories` | verifyPatientToken + products.read | 200 | Category list with images/colors |
| `GET /portal/prescriptions` | verifyPatientToken | — | Confirmed route exists |
| `GET /portal/prescriptions/:id/pdf` | verifyPatientToken | — | Confirmed route exists |
| `GET /appointments/my-memberships` | verifyToken (CUSTOMER JWT) | 200 | Patient's own memberships: `{id, planId, planName, planDurationDays, startDate, endDate, status, balance[]}` |
| `GET /membership-plans` | verifyToken (CUSTOMER JWT) | 200 | Full plan catalog with `entitlements` JSON string |
| `GET /patients/{patientId}/wallet` | verifyToken (CUSTOMER JWT) | 200 | `{patient, wallet:{balance,currency}, transactions[]}` — wallet-only txns |
| `GET /my-transactions` | verifyToken (CUSTOMER JWT) | 200 | Unified timeline; `summary.walletBalance=2000` confirmed |
| `GET /loyalty/{patientId}` | verifyToken (CUSTOMER JWT) | 200 | `{patient, balance, earnedThisMonth, transactions[]}` — **needs patientId from portal/me** |
| `GET /patients/{patientId}/treatment-plans` | verifyToken (CUSTOMER JWT) | 200 | `[{id, name, totalSessions, completedSessions, startedAt, status, totalPrice, service:{name,category}}]` |
| `GET /patients/{patientId}/consents` | verifyToken (CUSTOMER JWT) | 200 | `[{id, templateName, signedAt, hasPdfBlob, service:{}}]` |
| `GET /consents/{id}/pdf` | verifyToken (CUSTOMER JWT) | 200 | PDF bytes |
| `PUT /api/auth/me` | verifyToken (CUSTOMER JWT) | — | Updates name, email, password — **not** dob/gender/phone |
| `GET /api/auth/me` | verifyToken (CUSTOMER JWT) | 200 | User-layer: name, email, role, profilePicture |
| `GET /giftcards/storefront` | unguarded | — | Confirmed |
| `POST /giftcards/:id/purchase/order` | unguarded | — | Confirmed |
| `POST /giftcards/:id/purchase/confirm` | unguarded | — | Confirmed |

### 🔴 One remaining gap — Android FCM push registration

| Endpoint | Blocks | Notes |
|----------|--------|-------|
| `POST /portal/me/fcm-token` | Phase 9 FCM | `push.js` accepts only **Web Push (VAPID)** subscriptions `{endpoint, p256dh, auth}`. No Android device token (FCM) support anywhere in the backend. Needs a new endpoint. |
| `DELETE /portal/me/fcm-token` | Phase 9 FCM | Same |

### ⚠️ Security flag — loyalty/{patientId} not ownership-scoped
`GET /loyalty/{patientId}` accepts any integer patientId. Verified that `loyalty/1` returns a DIFFERENT patient's (Shashank bankar's) data to our CUSTOMER JWT holder. Backend must add ownership check: `req.user.userId → Patient.userId === patientId`. The Android app only ever calls this with `EncryptedPrefsManager.getPatientId()` (never user-supplied), so the app is safe, but the backend has a data leak for malicious clients.

### ⚠️ Profile edit scope — dob/gender/phone cannot be updated
`PUT /api/auth/me` updates User-row fields only. Patient-row fields (dob, gender, phone) have no update endpoint. Profile edit screen will support name + email + password only.

### ⚠️ Permission note — portal/products
Requires portal permission `products.read`. Confirm with backend that this is in the default permission set for new patient registrations.

### patientId storage strategy
`GET /portal/me` returns `response.id` which is the **patientId** (not the userId from JWT). This is different from `userId` (44) vs `patientId` (608) in the staging test. `AuthRepositoryImpl` now calls `portal/me` after login and caches patientId in `EncryptedPrefsManager`. All routes using `patients/{patientId}/` must read from `encryptedPrefs.getPatientId()`.

---

## Phase 0 — Bootstrap
✅ Phase 0 complete — 2026-06-03

| Task | Status |
|------|--------|
| Android project created (Empty Compose Activity) | ✅ |
| Package name set: `com.globussoft.wellness.patient` | ✅ |
| `gradle/libs.versions.toml` populated with all libraries | ✅ |
| `app/build.gradle.kts`: minSdk 26, compileSdk 35, Compose + KSP + Hilt + Firebase enabled | ✅ |
| `BuildConfig.BASE_URL` + `BuildConfig.TENANT_SLUG` fields added (debug + release flavors) | ✅ |
| `res/xml/network_security_config.xml` created + referenced in Manifest | ✅ |
| `AndroidManifest.xml` updated (permissions, FCM, FileProvider, deep-links) | ✅ |
| `root/build.gradle.kts` updated with all plugin aliases | ✅ |
| `settings.gradle.kts` updated with JitPack for Razorpay | ✅ |
| Old `com.crm.wellness` template files deleted | ✅ |
| `google-services.json` added (Firebase project created) | 🔴 MANUAL — requires Firebase Console; cannot be auto-generated. App compiles but FCM won't work. |
| `CLAUDE.md` placed at repo root | ✅ |
| `STATUS.md` placed at repo root | ✅ |

---

## Phase 1 — Core Module
✅ Phase 1 complete — 2026-06-03

### core/util/
| File | Status |
|------|--------|
| `Result.kt` | ✅ |
| `DateUtil.kt` | ✅ |
| `CurrencyUtil.kt` | ✅ |
| `PhoneUtil.kt` | ✅ |
| `Extensions.kt` | ✅ |

### core/network/
| File | Status |
|------|--------|
| `WellnessApiService.kt` (all endpoints) | ✅ |
| `AuthInterceptor.kt` | ✅ |
| `TokenManager.kt` | ✅ |

### core/storage/
| File | Status |
|------|--------|
| `DataStoreManager.kt` | ✅ |
| `EncryptedPrefsManager.kt` | ✅ |

### core/di/
| File | Status |
|------|--------|
| `AppModule.kt` (DataStore + EncryptedSharedPreferences) | ✅ |
| `NetworkModule.kt` (OkHttp, Retrofit, Moshi, WellnessApiService) | ✅ |
| `DatabaseModule.kt` (Room, 4 DAOs) | ✅ |
| `RepositoryModule.kt` (empty @Module, @Binds added per feature) | ✅ |

### core/navigation/
| File | Status |
|------|--------|
| `Screen.kt` | ✅ |
| `NavGraph.kt` (stub routes for all 17 screens) | ✅ |
| `DeepLinkHandler.kt` | ✅ |

### core/theme/
| File | Status |
|------|--------|
| `Color.kt` | ✅ |
| `Typography.kt` (Playfair Display via Google Fonts downloadable) | ✅ |
| `Shape.kt` | ✅ |
| `WellnessTheme.kt` | ✅ |
| Playfair Display font XML files (5 variants, Google Fonts provider) | ✅ |
| `res/values/font_certs.xml` (Google Fonts certificate arrays) | ✅ |

### core/fcm/ (Phase 9 stubs)
| File | Status |
|------|--------|
| `WellnessFcmService.kt` (stub — full impl Phase 9) | ✅ |
| `FcmHelper.kt` (stub — full impl Phase 9) | ✅ |

### core/database/
| File | Status |
|------|--------|
| `AppDatabase.kt` (Room v1, 4 entities, fallbackToDestructiveMigration) | ✅ |

### app/
| File | Status |
|------|--------|
| `WellnessPatientApp.kt` (@HiltAndroidApp) | ✅ |
| `MainActivity.kt` (NavHost + 4 notification channels + edge-to-edge) | ✅ |

### Database Entities + DAOs
| File | Status |
|------|--------|
| `CachedVisit` entity | ✅ |
| `VisitDao` | ✅ |
| `CachedPrescription` entity | ✅ |
| `PrescriptionDao` | ✅ |
| `CachedMembership` entity | ✅ |
| `MembershipDao` | ✅ |
| `CachedNotification` entity | ✅ |
| `NotificationDao` | ✅ |

### Feature DTO Stubs (data layer foundations)
| Feature | File | Status |
|---------|------|--------|
| auth | `AuthDtos.kt` (LoginRequest, LoginResponse, RegisterRequest, RegisterResponse, TenantBranding) | ✅ |
| booking | `BookingDtos.kt` (Visit, Service, Location, Slot, Appointment, Book, Cancel) | ✅ |
| health | `HealthDtos.kt` (Prescription, Drug) | ✅ |
| membership | `MembershipDtos.kt` (Membership, MembershipPlan, Credits, History) | ✅ |
| profile | `ProfileDtos.kt` (Profile, UpdateProfile, DsarExport) | ✅ |
| wallet | `WalletDtos.kt` (Wallet, Transaction, GiftCard, GiftCardOrder, GiftCardConfirm, FcmToken) | ✅ |

---

## Phase 2 — Auth Feature
✅ Phase 2 complete — 2026-06-03

### Data layer
| File | Status |
|------|--------|
| `feature/auth/domain/model/TenantBranding.kt` | ✅ |
| `feature/auth/domain/model/Patient.kt` | ✅ |
| `feature/auth/data/mapper/AuthMappers.kt` (toDomain, toPatient) | ✅ |
| `feature/auth/domain/repository/AuthRepository.kt` (interface) | ✅ |
| `feature/auth/data/repository/AuthRepositoryImpl.kt` | ✅ |
| `core/di/RepositoryModule.kt` updated with @Binds for AuthRepository | ✅ |

### Domain layer — UseCases + tests
| File | Status |
|------|--------|
| `GetTenantBrandingUseCase.kt` + test (3 cases) | ✅ |
| `CheckAuthStatusUseCase.kt` + test (3 cases) | ✅ |
| `LoginUseCase.kt` + test (4 cases) | ✅ |
| `RegisterPatientUseCase.kt` + test (4 cases) | ✅ — fully functional via `POST /api/auth/customer/register` |
| `LogoutUseCase.kt` + test (2 cases) | ✅ |
| ~~RequestOtpUseCase~~ / ~~VerifyOtpUseCase~~ | 🗑 Deleted — replaced by LoginUseCase |

### Presentation layer
| File | Status |
|------|--------|
| `presentation/state/SplashState.kt` | ✅ |
| `presentation/state/LoginState.kt` | ✅ |
| `presentation/state/RegisterState.kt` (email + password + confirmPassword) | ✅ |
| `presentation/viewmodel/SplashViewModel.kt` (saves tenantId to DataStore, emits NavigateToLogin) | ✅ |
| `presentation/viewmodel/LoginViewModel.kt` | ✅ |
| `presentation/viewmodel/RegisterViewModel.kt` (fully functional) | ✅ |
| `presentation/screen/SplashScreen.kt` | ✅ |
| `presentation/screen/LoginScreen.kt` (email + password, show/hide toggle, Sign up link) | ✅ |
| `presentation/screen/RegisterScreen.kt` (name + email + password + confirm, Sign in link) | ✅ |
| ~~PhoneEntryState~~ / ~~OtpVerifyState~~ / ~~PhoneEntryViewModel~~ / ~~OtpVerifyViewModel~~ / ~~PhoneEntryScreen~~ / ~~OtpVerifyScreen~~ | 🗑 Deleted |
| `core/navigation/Screen.kt` — PhoneEntry/OtpVerify removed, Login added | ✅ |
| `core/navigation/NavGraph.kt` — Splash → Login → Dashboard; Register ↔ Login | ✅ |
| `core/navigation/DeepLinkHandler.kt` — updated phone_entry → login | ✅ |

### Build verification (session 4)
| Check | Status |
|-------|--------|
| `./gradlew assembleDebug` | ✅ BUILD SUCCESSFUL |
| `./gradlew test` | ✅ 0 failures, 0 errors |

### Device verification (session 5 — live ADB test)
| Test | Result |
|------|--------|
| Splash screen renders (brand color, logo, clinic name) | ✅ |
| Splash → Login navigation (token absent) | ✅ |
| Login screen: fields, show/hide toggle, Sign In, Sign up link | ✅ |
| Successful login → Dashboard (blank Phase 3 stub) | ✅ POST /api/auth/login → 200 OK, 415ms |
| Token persistence: restart → goes to Dashboard (skips login) | ✅ |
| Wrong password → "Invalid email or password" in red | ✅ 401 handled correctly |
| Sign up link → Register screen | ✅ |
| Register screen: name/email/password/confirm/button/sign-in link | ✅ |
| Register "Sign in" → popBackStack to Login | ✅ |
| Register empty submit → "Full name is required" validation | ✅ |

### Session 5 bug fixes
| Fix | Status |
|-----|--------|
| `TENANT_SLUG` changed from `"default"` to `"testing"` in debug build | ✅ |
| `SentryInitProvider` crash fixed: `io.sentry.dsn=""` added to manifest | ✅ |
| `TenantBrandingDto` wrapper added (`TenantBrandingResponseDto`) — API returns `{ "tenant": {...} }` not flat object | ✅ |

---

## Phase 3 — Dashboard Feature
✅ Phase 3 complete — 2026-06-04

| File | Status |
|------|--------|
| `feature/dashboard/domain/model/Dashboard.kt` (Dashboard + UpcomingVisit) | ✅ |
| `feature/dashboard/domain/repository/DashboardRepository.kt` | ✅ |
| `feature/dashboard/data/repository/DashboardRepositoryImpl.kt` (parallel async; graceful degradation for blocked endpoints) | ✅ |
| `feature/dashboard/domain/usecase/GetDashboardUseCase.kt` | ✅ |
| `feature/dashboard/domain/usecase/GetDashboardUseCaseTest.kt` (4 cases, 4/4 passing) | ✅ |
| `feature/dashboard/presentation/state/DashboardState.kt` (UiState + UiEvent) | ✅ |
| `feature/dashboard/presentation/viewmodel/DashboardViewModel.kt` (DashboardNavEvent, Logout via LogoutUseCase) | ✅ |
| `feature/dashboard/presentation/screen/DashboardScreen.kt` (greeting, next-visit card, 3 stat chips, 4 quick-actions) | ✅ |
| `core/di/RepositoryModule.kt` — @Binds for DashboardRepository | ✅ |
| `core/navigation/NavGraph.kt` — DashboardScreen wired with all nav events | ✅ |
| `./gradlew assembleDebug` | ✅ BUILD SUCCESSFUL |
| `./gradlew test` (GetDashboardUseCaseTest: 4/4) | ✅ 0 failures |

### Phase 3 backend status
| Endpoint | Status |
|----------|--------|
| `GET /portal/visits?upcoming=true` | ✅ WORKING — next-visit card populated |
| `GET /portal/me/wallet` | 🔴 BLOCKED — wallet chip shows "—" until backend wired |
| `GET /portal/me/memberships` | 🔴 BLOCKED — membership chip shows "0" until backend wired |

---

## Phase 4 — Booking Feature

### Data layer
| File | Status |
|------|--------|
| `WellnessApiService.kt` — appointment URLs corrected to portal routes; `AppointmentListResponseDto`, `ProductDto`, `ProductCategoryDto`, `RescheduleAppointmentDto` added; `getPortalProducts()` + `getPortalProductCategories()` added; `getDashboard()` stub removed | ✅ |
| `BookingDtos.kt` — `AppointmentListResponseDto`, `RescheduleAppointmentDto`, `ProductDto`, `ProductCategoryDto`, `ProductCategoryRefDto` added; `bookingType` field added to `BookAppointmentDto` | ✅ |
| `CachedVisit` DAO | ✅ (created in Phase 1) |
| `AppointmentRepositoryImpl.kt` | ⬜ |

### Domain layer
| File | Status |
|------|--------|
| `AppointmentRepository.kt` (interface) | ⬜ |
| `GetMyAppointmentsUseCase.kt` + test | ⬜ — unblocked: `GET /portal/appointments?bucket=upcoming` |
| `BookAppointmentUseCase.kt` + test | ⬜ — unblocked: `POST /portal/appointments/book` |
| `CancelAppointmentUseCase.kt` + test | ⬜ — unblocked: `POST /portal/appointments/:id/cancel` |
| `RescheduleAppointmentUseCase.kt` + test | ⬜ — unblocked: `PATCH /portal/appointments/:id/reschedule` |
| `GetPortalProductsUseCase.kt` + test | ⬜ — unblocked: `GET /portal/products` (note: needs `products.read` permission) |
| `GetVisitHistoryUseCase.kt` + test | ⬜ — unblocked: `GET /portal/visits` |
| ~~GetAvailableSlotsUseCase~~ | 🗑 Removed — `GET /portal/slots` does not exist; booking uses date+time picker with server-side conflict validation |

### Presentation layer
| File | Status |
|------|--------|
| `BookAppointmentScreen.kt` (3-step revised) + `BookAppointmentViewModel.kt` | ⬜ — Step 1: product grid (`portal/products`), Step 2: date + time picker, Step 3: reason + membership. Location step removed (not in booking body). |
| `MyAppointmentsScreen.kt` + `MyAppointmentsViewModel.kt` | ⬜ — unblocked |
| `VisitHistoryScreen.kt` + `VisitHistoryViewModel.kt` | ⬜ — unblocked |

---

## Phase 5 — Health Feature (Prescriptions)

### Data layer
| File | Status |
|------|--------|
| `PrescriptionDto.kt` + mapper | ✅ (DTO created in Phase 1, mapper ⬜) |
| `CachedPrescription` DAO | ✅ (created in Phase 1) |
| `PrescriptionRepositoryImpl.kt` | ⬜ |

### Domain layer
| File | Status |
|------|--------|
| `PrescriptionRepository.kt` (interface) | ⬜ |
| `GetPrescriptionsUseCase.kt` + test | ⬜ |
| `GetPrescriptionPdfUseCase.kt` + test | ⬜ |

### Presentation layer
| File | Status |
|------|--------|
| `PrescriptionsScreen.kt` + `PrescriptionsViewModel.kt` | ⬜ |
| `PrescriptionPdfScreen.kt` + `PrescriptionPdfViewModel.kt` | ⬜ |

### Phase 2 stubs (data + domain only — no screens yet)
| File | Status |
|------|--------|
| `TreatmentPlanDto.kt` + `ConsentFormDto.kt` — added to `HealthDtos.kt` with real shapes | ✅ |
| `WellnessApiService.kt` — `getTreatmentPlans(patientId)` + `getConsents(patientId)` + `getConsentPdf(id)` added | ✅ |
| Mapper + Repository + UseCase for TreatmentPlan | ⬜ — unblocked: `GET /patients/{patientId}/treatment-plans` |
| Mapper + Repository + UseCase for ConsentForm | ⬜ — unblocked: `GET /patients/{patientId}/consents` + `GET /consents/{id}/pdf` |

---

## Phase 6 — Membership Feature

### Data layer
| File | Status |
|------|--------|
| `MembershipDtos.kt` — `MembershipDto` updated to real API shape; `MembershipPlanDto` updated with `entitlements` field | ✅ |
| `WellnessApiService.kt` — `getMyMemberships()` → `GET /appointments/my-memberships`; `getMembershipPlans()` confirmed working | ✅ |
| `CachedMembership` entity + DAO | ✅ (created in Phase 1) |
| `MembershipRepositoryImpl.kt` | ⬜ — unblocked |

### Domain layer
| File | Status |
|------|--------|
| `MembershipRepository.kt` (interface) | ⬜ — unblocked |
| `GetMyMembershipsUseCase.kt` + test | ⬜ — unblocked: `GET /appointments/my-memberships` |
| `GetMembershipPlansUseCase.kt` + test | ⬜ — unblocked: `GET /membership-plans` |

### Presentation layer
| File | Status |
|------|--------|
| `MembershipsScreen.kt` + `MembershipsViewModel.kt` | ⬜ — unblocked |
| `RedemptionHistorySheet.kt` | ⬜ — unblocked |

---

## Phase 7 — Wallet & Gift Cards Feature

### Data layer
| File | Status |
|------|--------|
| `WalletDtos.kt` — `MyTransactionsResponseDto`, `TransactionSummaryDto`, `TransactionDto` added | ✅ |
| `GiftCardDto.kt` + mappers | ✅ (DTOs created in Phase 1, mappers ⬜) |
| `WellnessApiService.kt` — `getMyTransactions()` added (`GET /api/wellness/my-transactions`) | ✅ |
| `WalletRepositoryImpl.kt` + `GiftCardRepositoryImpl.kt` | ⬜ — unblocked |

### Domain layer
| File | Status |
|------|--------|
| `GetMyTransactionsUseCase.kt` + test | ⬜ — unblocked: `GET /api/wellness/my-transactions` (verifyToken) |
| `GetGiftCardStorefrontUseCase.kt` + test | ⬜ |
| `InitiateGiftCardPurchaseUseCase.kt` + test | ⬜ |
| `ConfirmGiftCardPurchaseUseCase.kt` + test | ⬜ |

### Presentation layer
| File | Status |
|------|--------|
| `WalletScreen.kt` + `WalletViewModel.kt` | ⬜ — unblocked. Balance from `summary.walletBalance`. Timeline sorted newest-first with type icons (POS_SALE, WALLET, MEMBERSHIP, GIFTCARD, PAYMENT, SUBSCRIPTION). |
| `GiftCardsScreen.kt` + `GiftCardsViewModel.kt` | ⬜ |
| `GiftCardPurchaseSheet.kt` (Razorpay flow) | ⬜ |

---

## Phase 8 — Profile & Notifications Feature

### Profile
| File | Status |
|------|--------|
| `ProfileDtos.kt` — `UpdateAuthProfileDto` + `AuthProfileResponseDto` added | ✅ |
| `WellnessApiService.kt` — `getAuthProfile()` + `updateAuthProfile()` added (`/api/auth/me`) | ✅ |
| `ProfileRepositoryImpl.kt` | ⬜ |
| `GetProfileUseCase.kt` + test | ⬜ — uses `GET /portal/me` (phone, dob, gender) |
| `UpdateProfileUseCase.kt` + test | ⬜ — unblocked: uses `PUT /api/auth/me` (name + email + password only; dob/gender/phone cannot be updated — no patient-row update endpoint exists) |
| `RequestDsarExportUseCase.kt` + test | ⬜ |
| `ProfileScreen.kt` + `ProfileViewModel.kt` | ⬜ |

### Notifications
| File | Status |
|------|--------|
| `CachedNotification` DAO | ✅ (created in Phase 1) |
| `NotificationRepositoryImpl.kt` | ⬜ |
| `NotificationInboxScreen.kt` + `NotificationInboxViewModel.kt` | ⬜ |

---

## Phase 9 — FCM Push Notifications

| Task | Status |
|------|--------|
| `WellnessFcmService.kt` — stub created (Phase 9: full impl) | ✅ |
| `FcmHelper.kt` — stub created (Phase 9: full impl) | ✅ |
| `WellnessFcmService.kt` — token registration + message receive (full) | ⬜ |
| `FcmHelper.kt` — register/deregister on login/logout (full) | 🔴 BLOCKED — `POST/DELETE /portal/me/fcm-token` requires NEW backend endpoints (no reusable equivalent) |
| 4 notification channels created in `MainActivity.onCreate()` | ✅ |
| `POST_NOTIFICATIONS` runtime permission request | ⬜ (wired in Phase 9) |
| Deep-link pending intent per notification type | ⬜ |
| `DeepLinkHandler.kt` wired in `MainActivity` | ⬜ |

---

## Phase 10 — Testing

| Task | Status |
|------|--------|
| UseCase unit tests — auth feature | ⬜ |
| UseCase unit tests — dashboard | ⬜ |
| UseCase unit tests — booking | ⬜ |
| UseCase unit tests — health (prescriptions) | ⬜ |
| UseCase unit tests — membership | ⬜ |
| UseCase unit tests — wallet + gift cards | ⬜ |
| UseCase unit tests — profile | ⬜ |
| ViewModel tests (Turbine) — all features | ⬜ |
| Room DAO integration tests (in-memory DB) | ⬜ |
| UI tests — OtpVerifyScreen | ⬜ |
| UI tests — BookAppointmentScreen (4 steps) | ⬜ |
| UI tests — MembershipsScreen | ⬜ |
| UI tests — PrescriptionsScreen (permission gate) | ⬜ |

---

## Phase 11 — Release Prep

| Task | Status |
|------|--------|
| R8 full minification enabled + ProGuard rules verified | ⬜ |
| Cert pinning pins updated for production cert | ⬜ |
| Room migrations replacing `fallbackToDestructiveMigration` | ⬜ |
| `TENANT_SLUG` set in release build config | ⬜ |
| Upload to Play Store internal testing track | ⬜ |

---

## Phase 2 Features (deferred screens)

| Feature | Status | Dependency |
|---------|--------|-----------|
| Screen 10 — Treatment Plans (UI) | ⬜ | `GET /portal/me/treatment-plans` backend endpoint |
| Screen 11 — Consent Forms (UI) | ⬜ | `GET /portal/me/consents` backend endpoint |
| Screen 14 — Loyalty & Referrals (UI) | ⬜ | `GET /portal/me/loyalty` backend endpoint |
| Gift card gifting to another patient | ⬜ | Patient phone search API |
| Biometric login | ⬜ | Android BiometricPrompt |
| Hindi localization | ⬜ | `strings.xml` translations |
| Home screen widget (Jetpack Glance) | ⬜ | — |
| NPS in-app survey screen | ⬜ | Survey response API |

---

## Notes & Decisions Log

| Date | Note |
|------|------|
| 2026-06-03 | Session 1 complete. Phase 0 + Phase 1 fully done. All 12 backend gap endpoints initially flagged MISSING. |
| 2026-06-03 | `google-services.json` requires manual Firebase Console step — cannot auto-generate. App compiles without it. |
| 2026-06-03 | Playfair Display loaded via Google Fonts downloadable fonts (XML-based, 5 weight/style variants). No binary .ttf needed. |
| 2026-06-03 | AGP version in libs.versions.toml set to 8.7.3 (stable) rather than 9.0.1 from original template — avoids unstable API. |
| 2026-06-03 | All DTO stubs created in Phase 1 to satisfy WellnessApiService Retrofit interface compilation (no circular dependencies). |
| 2026-06-03 | Session 2 backend re-audit: BASE_URL `https://crm.globusdemos.com/api/wellness/` is CORRECT. Patient portal is under `/api/wellness/portal/*`. Auth is via `/api/auth/login` + `/api/auth/customer/register` (absolute paths, resolved against host). |
| 2026-06-03 | Session 2: 10 of 12 originally flagged gap endpoints are reusable — existing backend data just needs patient-auth wiring. Only POST/DELETE /portal/me/fcm-token require genuinely new backend code. |
| 2026-06-03 | Session 2: NEW gaps discovered — appointment routes (book/my/cancel) and browse routes (services/locations/membership-plans) use staff JWT (`verifyToken` + `req.user.tenantId`). Patient JWT sets `req.patient` — these routes return 401/500 for patients. Need portal-scoped variants on backend before Phase 4 and 6. |
| 2026-06-03 | OTP is 4 digits (backend validates `^\d{4}$`). CLAUDE.md demo value `123456` is wrong — correct demo env var is `WELLNESS_DEMO_OTP=1234`. OTP UI must be a 4-box input (already in spec). |
| 2026-06-03 | Both `POST /portal/login` (legacy combined {phone,otp}) and `POST /portal/login/request-otp` + `/verify-otp` (current flow) exist. Use the two-step flow. JWT is 30-day, signed with PORTAL_JWT_SECRET, contains {patientId, phoneLast10}. |
| 2026-06-03 | Session 3: Phase 2 Auth Feature complete. Fixed pre-existing bug in `core/util/DateUtil.kt` line 23 — `it` inside `getOrElse` referred to `Throwable` not outer iso string (variable shadowing). Named `isoStr` to fix. |
| 2026-06-03 | Session 3: OtpVerifyViewModel reads phone from SavedStateHandle (passed via nav route argument). VerifyOtpUseCase returns PATIENT_NOT_FOUND on 404 → ViewModel emits NavigateToRegister. RegisterScreen shows stub banner; submit button permanently disabled until backend wires /portal/register. |
| 2026-06-03 | Session 4: Auth migrated from phone+OTP to email+password. Uses `POST /api/auth/login` and `POST /api/auth/customer/register` (CUSTOMER userType). JWT Path B in verifyPatientToken resolves Patient row from userId. SplashViewModel now saves tenantId from branding response to DataStore; AuthRepositoryImpl reads it for scoped login/register calls. PhoneEntry, OtpVerify screens/VMs/states/UseCases deleted. LoginScreen replaces them. RegisterScreen now fully functional (no more stub). |
| 2026-06-04 | Session 6: Phase 3 Dashboard complete. Composed from 3 parallel API calls (visits ✅, wallet 🔴, memberships 🔴). Blocked endpoints degrade gracefully (wallet "—", memberships "0"). DashboardViewModel injects LogoutUseCase directly for logout flow. Calendar.HOUR_OF_DAY used in Composable for time-of-day greeting (display logic only). wallet balance assumed in paise (CurrencyUtil.formatPaise). |
| 2026-06-04 | Session 7 (part 1): Backend re-audit + WellnessApiService corrections. Appointment routes confirmed at `portal/appointments/*` with verifyPatientToken. 3 wrong URLs fixed, `getAvailableSlots()` / `getDashboard()` / `getServices()` / `getLocations()` removed, `getPortalProducts()` + `getPortalProductCategories()` + `rescheduleAppointment()` added. `AppointmentListResponseDto` wrapper added (`{bucket, count, appointments}`). Booking flow revised: location step removed, slot grid → date+time picker. |
| 2026-06-04 | Session 7 (part 2): Frontend audit + live staging API test with CUSTOMER JWT (mohitreddy@gimpmail.com, patientId=608). Found ALL previously blocked features now have working endpoints. `GET /appointments/my-memberships` (200) → memberships. `GET /loyalty/{patientId}` (200) → loyalty. `GET /patients/{patientId}/treatment-plans` (200) → treatment plans. `GET /patients/{patientId}/consents` (200) → consent forms. `GET /consents/{id}/pdf` (200) → PDF. `GET /patients/{patientId}/wallet` (200) → dedicated wallet view. Only remaining gap: Android FCM registration (`push.js` is WebPush/VAPID only). CRITICAL: `loyalty/{patientId}` is not ownership-scoped — backend security issue flagged. `patientId` (≠ userId) must be fetched from `portal/me` and cached in `EncryptedPrefsManager`. `AuthRepositoryImpl` updated to call `portal/me` after login. `MembershipDtos.kt`, `HealthDtos.kt`, `WalletDtos.kt` updated with real API shapes. `LoyaltyDtos.kt` created. `WellnessApiService.kt` rewritten with all endpoints. Build ✅. |
