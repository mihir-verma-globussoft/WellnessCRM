# WellnessCRM Patient App — Implementation Status

Last updated: 2026-06-03 (session 4 — Auth migrated to email+password; PhoneEntry/OTP removed)
Current phase: Phase 3 — Dashboard Feature

## Legend
✅ Done &nbsp; 🔄 In Progress &nbsp; ⬜ Not started &nbsp; 🔴 Blocked (reason inline)

---

## Backend Gap Endpoints
*Re-audited against CRM backend at `/home/glb-blr-214/StudioProjects/globussoft-crm` on 2026-06-03 (session 2)*

### Originally flagged as missing — now resolved as REUSABLE
Of the 12 initially flagged gaps, 10 have functional backend equivalents that can be wired with portal-scoped auth or composed from existing patient-portal endpoints. Only 2 require genuinely new backend code.

| Endpoint | Resolution |
|----------|-----------|
| `POST /portal/register` | Partial equivalent exists (`/auth/customer/register`). Needs Patient-row linking + OTP flow wiring. Build on backend before Screen 4. |
| `GET /portal/me/dashboard` | No single aggregator. **Compose on Android** from `portal/visits?upcoming=true` + `portal/me/wallet` + `portal/me/memberships` (parallel calls in DashboardUseCase). |
| `GET /portal/slots` | Endpoint exists in wellness.js (`/wellness/portal/slots` area). Needs patient-auth variant wiring — build on backend before Screen 6 Step 3. |
| `PUT /portal/me` | Reusable — add `verifyPatientToken` to the existing patient update path. Build before Screen 15 edit. |
| `GET /portal/me/wallet` | Reusable — scope existing wallet ledger to `req.patient.id`. Build before Screen 12. |
| `GET /portal/me/memberships` | Reusable — scope `patients/:id/memberships` to `req.patient.id`. Build before Screen 17. |
| `GET /portal/me/treatment-plans` | Reusable — scope `patients/:id/treatment-plans` to `req.patient.id`. Phase 2, build before Screen 10. |
| `GET /portal/me/consents` | Reusable — scope `patients/:id/consents` to `req.patient.id`. Phase 2, build before Screen 11. |
| `GET /portal/me/consents/:id/pdf` | Reusable — portal-scoped PDF download. Phase 2, build before Screen 11. |
| `GET /portal/me/loyalty` | Reusable — scope loyalty data to `req.patient.id`. Phase 2, build before Screen 14. |

### Truly new backend endpoints needed
| Endpoint | Status | Blocks |
|----------|--------|--------|
| `POST /portal/me/fcm-token` | 🔴 BLOCKED — no patient-portal push registration exists | Phase 9 FCM |
| `DELETE /portal/me/fcm-token` | 🔴 BLOCKED — no patient-portal push deregister exists | Phase 9 FCM |

### Newly discovered gaps (found during session 2 auth audit)
These routes exist but use **staff JWT** (`verifyToken` + `req.user.tenantId`). Patients sending portal JWT will get 401. Need patient-portal versions.

| Endpoint | Issue | Blocks |
|----------|-------|--------|
| `POST /appointments/book` | Uses staff `verifyToken` — patient JWT rejected | Screen 6 (Book Appointment) |
| `GET /appointments/my` | Uses staff `verifyToken` — patient JWT rejected | Screen 7 (My Appointments) |
| `POST /appointments/:id/cancel` | Uses staff `verifyToken` — patient JWT rejected | Screen 7 (My Appointments cancel) |
| `GET /services` | Uses `tenantWhere(req.user.tenantId)` — crashes for patient JWT | Screen 6 Step 1 (service picker) |
| `GET /locations` | Uses `tenantWhere(req.user.tenantId)` — crashes for patient JWT | Screen 6 Step 2 (location picker) |
| `GET /membership-plans` | Uses `tenantWhere(req.user.tenantId)` — crashes for patient JWT | Screen 6 Step 4 + Screen 17 |

**Fix pattern for services/locations/membership-plans**: add patient-portal routes (`portal/services`, `portal/locations`, `portal/membership-plans`) using `verifyPatientToken` and `req.patient.tenantId` for the DB query — same data, different auth path.

**Fix pattern for appointment routes**: add `portal/appointments/book`, `portal/appointments/my`, `portal/appointments/:id/cancel` using `verifyPatientToken`, mirroring how `portal/visits` wraps the internal visit data.

### Confirmed working patient-portal endpoints
`GET /public/tenant/:slug` ✅ | `POST /api/auth/login` ✅ (email+password → CUSTOMER JWT) | `POST /api/auth/customer/register` ✅ | `GET /portal/me` ✅ | `GET /portal/visits` ✅ (supports `?upcoming=true`) | `GET /portal/prescriptions` ✅ | `GET /portal/prescriptions/:id/pdf` ✅ | `POST /portal/export` ✅ | `GET /giftcards/storefront` ✅ | `POST /giftcards/:id/purchase/order` ✅ | `POST /giftcards/:id/purchase/confirm` ✅

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

| File | Status |
|------|--------|
| `DashboardDto.kt` + mapper | ⬜ — compose from `portal/visits?upcoming=true` + `portal/me/wallet` + `portal/me/memberships` (no aggregator endpoint needed) |
| `DashboardRepository.kt` + impl | ⬜ |
| `GetDashboardUseCase.kt` + test | ⬜ (parallel calls; `wallet` + `memberships` still need backend wiring) |
| `DashboardScreen.kt` + `DashboardViewModel.kt` | ⬜ |

---

## Phase 4 — Booking Feature

### Data layer
| File | Status |
|------|--------|
| DTOs: `VisitDto`, `ServiceDto`, `LocationDto`, `SlotDto`, `AppointmentDto` | ✅ (created in Phase 1) |
| `CachedVisit` DAO | ✅ (created in Phase 1) |
| `AppointmentRepositoryImpl.kt` | ⬜ |

### Domain layer
| File | Status |
|------|--------|
| `AppointmentRepository.kt` (interface) | ⬜ |
| `GetAvailableSlotsUseCase.kt` + test | 🔴 BLOCKED — `GET /portal/slots` needs patient-auth wiring on backend |
| `BookAppointmentUseCase.kt` + test | 🔴 BLOCKED — `POST /appointments/book` uses staff JWT; needs `POST /portal/appointments/book` |
| `GetMyAppointmentsUseCase.kt` + test | 🔴 BLOCKED — `GET /appointments/my` uses staff JWT; needs `GET /portal/appointments/my` |
| `CancelAppointmentUseCase.kt` + test | 🔴 BLOCKED — `POST /appointments/:id/cancel` uses staff JWT; needs patient-auth variant |
| `GetVisitHistoryUseCase.kt` + test | ⬜ |

### Presentation layer
| File | Status |
|------|--------|
| `BookAppointmentScreen.kt` (4-step) + `BookAppointmentViewModel.kt` | 🔴 BLOCKED — services/locations/slots/book all need patient-auth backend routes |
| `MyAppointmentsScreen.kt` + `MyAppointmentsViewModel.kt` | 🔴 BLOCKED — appointments/my + cancel need patient-auth backend routes |
| `VisitHistoryScreen.kt` + `VisitHistoryViewModel.kt` | ⬜ |

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
| `TreatmentPlanDto.kt` + mapper + Repository + UseCase | 🔴 BLOCKED — `GET /portal/me/treatment-plans` needs patient-auth wiring (reusable, see gap table) |
| `ConsentFormDto.kt` + mapper + Repository + UseCase | 🔴 BLOCKED — `GET /portal/me/consents` needs patient-auth wiring (reusable, see gap table) |

---

## Phase 6 — Membership Feature

### Data layer
| File | Status |
|------|--------|
| `MembershipDto.kt` + `MembershipPlanDto.kt` + mappers | ✅ (DTOs created in Phase 1, mappers ⬜) |
| `CachedMembership` entity + DAO | ✅ (created in Phase 1) |
| `MembershipRepositoryImpl.kt` | 🔴 BLOCKED — `GET /portal/me/memberships` needs patient-auth wiring on backend |

### Domain layer
| File | Status |
|------|--------|
| `MembershipRepository.kt` (interface) | ⬜ |
| `GetMyMembershipsUseCase.kt` + test | 🔴 BLOCKED — backend wiring needed |
| `GetMembershipPlansUseCase.kt` + test | 🔴 BLOCKED — `GET /membership-plans` uses staff JWT; needs `GET /portal/membership-plans` |

### Presentation layer
| File | Status |
|------|--------|
| `MembershipsScreen.kt` + `MembershipsViewModel.kt` | 🔴 BLOCKED |
| `RedemptionHistorySheet.kt` | 🔴 BLOCKED |

---

## Phase 7 — Wallet & Gift Cards Feature

### Data layer
| File | Status |
|------|--------|
| `WalletDto.kt` + `GiftCardDto.kt` + mappers | ✅ (DTOs created in Phase 1, mappers ⬜) |
| `WalletRepositoryImpl.kt` + `GiftCardRepositoryImpl.kt` | 🔴 BLOCKED — `GET /portal/me/wallet` needs patient-auth wiring on backend |

### Domain layer
| File | Status |
|------|--------|
| `GetWalletUseCase.kt` + test | 🔴 BLOCKED — backend wiring needed |
| `GetGiftCardStorefrontUseCase.kt` + test | ⬜ |
| `InitiateGiftCardPurchaseUseCase.kt` + test | ⬜ |
| `ConfirmGiftCardPurchaseUseCase.kt` + test | ⬜ |

### Presentation layer
| File | Status |
|------|--------|
| `WalletScreen.kt` + `WalletViewModel.kt` | 🔴 BLOCKED — backend wiring needed |
| `GiftCardsScreen.kt` + `GiftCardsViewModel.kt` | ⬜ |
| `GiftCardPurchaseSheet.kt` (Razorpay flow) | ⬜ |

---

## Phase 8 — Profile & Notifications Feature

### Profile
| File | Status |
|------|--------|
| `ProfileRepositoryImpl.kt` | ⬜ |
| `GetProfileUseCase.kt` + test | ⬜ |
| `UpdateProfileUseCase.kt` + test | 🔴 BLOCKED — `PUT /portal/me` needs patient-auth wiring on backend |
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
