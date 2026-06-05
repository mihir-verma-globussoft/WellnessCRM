# WellnessCRM Patient App — Implementation Status

Last updated: 2026-06-05
Current phase: Post-Phase-11 — Web Portal Gap Fixes ✅ complete

---

## Legend
✅ Done  🔄 In Progress  ⬜ Not started  🔴 Blocked

---

## Last Session Summary
> Read this section first — it tells you exactly where the codebase is and what was last touched.

**Last worked on:** 2026-06-05
**Last git commit:** `70027fc` — `feat: portal dashboard menu, Poppins font, section header upgrade + bug fixes`
**Uncommitted work in progress:** Web portal gap fixes G1–G5 + BuildConfig.TENANT_ID=1 (see below)

**What changed in the last session (uncommitted):**
- `CLAUDE.md` — fixed OTP demo credential from 6-digit to 4-digit, noted 30s resend cooldown
- `app/build.gradle.kts` — added `buildConfigField("int", "TENANT_ID", "1")` to all 3 build variants
- `core/network/WellnessApiService.kt` — added `getPortalHealth()` and `getPatientPermissions()`
- `feature/auth/data/remote/dto/AuthDtos.kt` — added `PatientPermissionsDto` and `PortalHealthDto`
- `feature/auth/domain/model/PatientPermissions.kt` — new domain model with `has()` helper + constants
- `feature/auth/domain/repository/AuthRepository.kt` — added `getPatientPermissions()` and `isSmsAvailable()`
- `feature/auth/data/repository/AuthRepositoryImpl.kt` — implemented both + login/register now use `BuildConfig.TENANT_ID`
- `feature/auth/domain/usecase/GetPatientPermissionsUseCase.kt` — new UseCase
- `feature/auth/domain/usecase/CheckSmsAvailabilityUseCase.kt` — new UseCase
- `feature/auth/presentation/state/LoginState.kt` — added `smsUnavailable` + `smsBannerDismissed` flags
- `feature/auth/presentation/viewmodel/LoginViewModel.kt` — SMS health check on init
- `feature/auth/presentation/screen/LoginScreen.kt` — dismissible amber SMS unavailable banner
- `feature/health/presentation/state/HealthState.kt` — added `permissionBlocked` to `PrescriptionsUiState`
- `feature/health/presentation/viewmodel/PrescriptionsViewModel.kt` — permission gate via `GetPatientPermissionsUseCase`
- `feature/health/presentation/screen/PrescriptionsScreen.kt` — blocked state UI
- `feature/booking/data/remote/dto/BookingDtos.kt` — added `canCancel` and `canReschedule` to `AppointmentDto`
- `feature/booking/domain/model/Appointment.kt` — added `canCancel` and `canReschedule` to domain model
- `feature/booking/data/mapper/BookingMappers.kt` — mapped `canCancel`/`canReschedule` through to domain
- `feature/booking/presentation/state/BookingState.kt` — added `pending`/`cancelled` lists, reschedule sheet state, 3 new events
- `feature/booking/presentation/viewmodel/MyAppointmentsViewModel.kt` — 4-bucket parallel load + reschedule handler
- `feature/booking/presentation/screen/MyAppointmentsScreen.kt` — `ScrollableTabRow` 4 tabs + `ModalBottomSheet` reschedule picker
- `feature/auth/domain/usecase/GetPatientPermissionsUseCaseTest.kt` — 5 new tests
- `feature/health/presentation/viewmodel/PrescriptionsViewModelTest.kt` — updated mock signatures + permission test

**Device verified (2026-06-05):**
- Login fixed for Mohit Gupta (`mohitgupta@fivermail.com`) — `loginTenantId: 1` confirmed HTTP 200 in logcat ✅
- 4-tab appointment screen renders (Upcoming/Pending/Completed/Cancelled) ✅
- Empty state labels per tab work correctly ✅
- Book Appointment screen navigates (Step 1 shows "No services available" — tenant 1 has no services configured on staging) ⚠️

**Tests:** 162/162 passing (150 prior + 12 new: 5 GetPatientPermissionsUseCase + 1 PrescriptionsViewModel permission test + 6 updates) ✅

**What changed in the previous committed sessions:**

| Commit | Date | What |
|--------|------|------|
| `70027fc` | 2026-06-05 | feat: portal dashboard menu, Poppins font, section header upgrade + bug fixes |
| `3d6a804` | 2026-06-05 | feat: Wellness Lumina UI — warm palette, gradient heroes, fluid components |
| `6433348` | 2026-06-04 | feat: Enhanced Wellness Edition UI migration — design system overhaul |

---

## Phase 0 — Bootstrap
✅ Android project created (minSdk 26, compileSdk 35)
✅ libs.versions.toml configured with all library versions
✅ Firebase google-services.json added
✅ network_security_config.xml (cert pinning production domain)
✅ BuildConfig.BASE_URL + BuildConfig.TENANT_SLUG wired per build type
✅ Phase 0 complete

---

## Phase 1 — Core Module
✅ core/network/WellnessApiService.kt
✅ core/network/AuthInterceptor.kt
✅ core/network/TokenManager.kt
✅ core/storage/DataStoreManager.kt
✅ core/storage/EncryptedPrefsManager.kt
✅ core/util/Result.kt
✅ core/util/DateUtil.kt
✅ core/util/CurrencyUtil.kt
✅ core/util/PhoneUtil.kt
✅ core/di/AppModule.kt
✅ core/di/NetworkModule.kt
✅ core/di/DatabaseModule.kt
✅ core/di/RepositoryModule.kt
✅ core/theme/Color.kt
✅ core/theme/Shape.kt
✅ core/theme/Typography.kt
✅ core/theme/WellnessTheme.kt
✅ core/navigation/Screen.kt
✅ core/navigation/NavGraph.kt
✅ core/navigation/DeepLinkHandler.kt
✅ AppDatabase.kt (4 entities: CachedVisit, CachedPrescription, CachedMembership, CachedNotification)
✅ Phase 1 complete

---

## Phase 2 — Auth Feature
✅ feature/auth/data/remote/dto/AuthTokenDto.kt
✅ feature/auth/data/remote/dto/TenantBrandingDto.kt
✅ feature/auth/data/repository/AuthRepositoryImpl.kt
✅ feature/auth/domain/model/AuthToken.kt
✅ feature/auth/domain/model/TenantBranding.kt
✅ feature/auth/domain/repository/AuthRepository.kt
✅ feature/auth/domain/usecase/RequestOtpUseCase.kt
✅ feature/auth/domain/usecase/VerifyOtpUseCase.kt
✅ feature/auth/domain/usecase/LoadTenantBrandingUseCase.kt
✅ feature/auth/presentation/screen/SplashScreen.kt
✅ feature/auth/presentation/screen/LoginScreen.kt (PhoneEntry)
✅ feature/auth/presentation/screen/OtpVerifyScreen.kt
✅ feature/auth/presentation/screen/RegisterScreen.kt
✅ feature/auth/presentation/viewmodel/AuthViewModel.kt
✅ Phase 2 complete

---

## Phase 3 — Dashboard Feature
✅ feature/dashboard/data/ (dto, mapper, repository)
✅ feature/dashboard/domain/ (model, repository, usecases)
✅ feature/dashboard/presentation/screen/DashboardScreen.kt
✅ feature/dashboard/presentation/viewmodel/DashboardViewModel.kt
✅ Phase 3 complete

---

## Phase 4 — Booking Feature
✅ feature/booking/data/ (dto, local/entity, local/dao, mapper, repository)
✅ feature/booking/domain/ (model, repository, usecases)
✅ feature/booking/presentation/screen/BookAppointmentScreen.kt (4-step)
✅ feature/booking/presentation/screen/MyAppointmentsScreen.kt
✅ feature/booking/presentation/screen/VisitHistoryScreen.kt
✅ Phase 4 complete

---

## Phase 5 — Health Feature
✅ feature/health/data/ (dto, local/entity, local/dao, mapper, repository)
✅ feature/health/domain/ (model, repository, usecases)
✅ feature/health/presentation/screen/PrescriptionsScreen.kt
✅ Phase 5 complete

---

## Phase 6 — Membership Feature
✅ feature/membership/data/ (dto, local/entity, local/dao, mapper, repository)
✅ feature/membership/domain/ (model, repository, usecases)
✅ feature/membership/presentation/screen/MembershipsScreen.kt
✅ Phase 6 complete

---

## Phase 7 — Wallet + Gift Cards
✅ feature/wallet/data/ (dto, mapper, repository)
✅ feature/wallet/domain/ (model, repository, usecases)
✅ feature/wallet/presentation/screen/WalletScreen.kt
✅ feature/wallet/presentation/screen/GiftCardsScreen.kt (Razorpay)
✅ Phase 7 complete

---

## Phase 8 — Profile + Notifications
✅ feature/profile/data/ (dto, mapper, repository)
✅ feature/profile/domain/ (model, repository, usecases)
✅ feature/profile/presentation/screen/ProfileScreen.kt
✅ feature/notifications/data/local/ (entity, dao, repository)
✅ feature/notifications/presentation/screen/NotificationInboxScreen.kt
✅ Phase 8 complete

---

## Phase 9 — FCM Push Notifications
✅ core/fcm/WellnessFcmService.kt (onNewToken + onMessageReceived)
✅ FCM token registration on login / deregistration on logout
✅ 4 notification channels created in MainActivity.onCreate
✅ POST_NOTIFICATIONS runtime permission request
✅ Deep-link routing from notification tap
✅ Phase 9 complete

---

## Phase 10 — Testing
✅ UseCase unit tests (JUnit 5 + MockK) — all use cases covered
✅ ViewModel tests (Turbine StateFlow assertions)
✅ Room DAO integration tests (in-memory DB)
✅ Compose UI tests (BookAppointment, Memberships, Prescriptions)
✅ 75/75 tests passing
✅ Phase 10 complete

---

## Phase 11 — Release Prep
✅ R8 full minification enabled
✅ ProGuard rules (Retrofit, Moshi, Hilt, Razorpay)
✅ Cert pin updated in network_security_config.xml
✅ Room migration (removed fallbackToDestructiveMigration)
✅ Signing config in build.gradle.kts
✅ Phase 11 complete

---

## UI Design System (Post-Phase-11 — extra track)

These are presentation-layer-only improvements. No business logic, ViewModel (except nav events), UseCase, or navigation routing was changed.

### Enhanced Wellness Edition (commit `6433348` — 2026-06-04)
✅ Fonts: Manrope (headlines) + Inter (body) via Google Fonts XML provider
✅ Dark mode: full M3 dark colour scheme with isSystemInDarkTheme()
✅ core/ui/WellnessComponents.kt: WellnessCard, StatusChip (semantic colours), SectionLabel, ErrorState, EmptyState
✅ All 17 screens migrated to WellnessCard + shared StatusChip
✅ Button shape fixed: explicit shape param on all 10 Button/OutlinedButton instances

### Wellness Lumina (commit `3d6a804` — 2026-06-05)
✅ Color.kt: warm linen background `#F5F0E8`, forest emerald primary `#1A5C53`, WellnessGold `#F59E0B`, warm dark mode `#0E1410`
✅ Shape.kt: chips 8dp · inputs 12dp · cards 16dp · hero/sheets 24dp · buttons 12dp
✅ WellnessTheme.kt: surfaceContainer variants explicitly wired in both schemes
✅ WellnessComponents.kt: shadow elevation on WellnessCard, new GradientHeroCard (teal vertical gradient), new WellnessProgressBar (8dp rounded pill), pill-always StatusChip, tinted icon containers
✅ SplashScreen: full-screen vertical gradient, white branding text + spinner
✅ DashboardScreen: GradientHeroCard for next-visit hero, icon containers on stat chips
✅ WalletScreen: GradientHeroCard balance hero, coloured circle transaction icons
✅ LoyaltyScreen: GradientHeroCard, gold points number, circle transaction icons
✅ MembershipsScreen: WellnessProgressBar replaces LinearProgressIndicator

### Dashboard Portal Menu (commit `70027fc` — 2026-06-05)
✅ DashboardState.kt: 3 new UiEvent entries (VisitHistory, TreatmentPlans, ConsentForms)
✅ DashboardViewModel.kt: 3 new NavEvent entries + onEvent branches
✅ NavGraph.kt: 3 new route wires for dashboard
✅ DashboardScreen.kt: removed QuickActionsRow (4 tiles); added 5-section 2-column portal menu:
   - Appointments: Book Appointment | My Appointments | Visit History
   - Health Records: Prescriptions | Treatment Plans | Consent Forms
   - Finance: Wallet | Gift Cards
   - Programs: Memberships | Loyalty & Referrals
   - Account: Profile | Notifications
✅ Device verified on ADB device — all 5 sections, all 11 tiles, correct section colours
✅ Typography switched to Poppins (poppins_regular/medium/semi_bold/bold.xml font resources)
✅ SectionLabel upgraded to titleSmall SemiBold (14sp, high-contrast)

---

## Phase 2 (Deferred Screens) — Treatment Plans, Consent Forms, Loyalty
✅ feature/health/presentation/screen/TreatmentPlansScreen.kt (display, data layer done)
✅ feature/health/presentation/screen/ConsentFormsScreen.kt (display, data layer done)
✅ feature/loyalty/presentation/screen/LoyaltyScreen.kt (display, data layer done)
🔴 BLOCKED: GET /portal/me/treatment-plans — not yet built on CRM backend
🔴 BLOCKED: GET /portal/me/consents — not yet built on CRM backend
🔴 BLOCKED: GET /portal/me/consents/:id/pdf — not yet built on CRM backend
🔴 BLOCKED: GET /portal/me/loyalty — not yet built on CRM backend

---

## Web Portal Gap Fixes (Post-Phase-11 — 2026-06-05)

Gaps discovered by analyzing the live web patient portal source (PatientPortal.jsx).
All endpoints listed here are ✅ live on the backend.

✅ G1 — CLAUDE.md OTP demo credential: `123456` → `1234` (4-digit enforcement confirmed)
✅ G2 — GET /portal/me/permissions: PatientPermissions domain model + GetPatientPermissionsUseCase + GetPatientPermissionsUseCaseTest (5 tests) + PrescriptionsViewModel permission gate + PrescriptionsScreen blocked state
✅ G3 — PATCH /portal/appointments/:id/reschedule: ShowRescheduleSheet/DismissRescheduleSheet/ConfirmReschedule events + ViewModel reschedule() coroutine + ModalBottomSheet with 14-day date chips + time grid. Device test found button gate was `selectedTab==0`; fixed by mapping `canReschedule`/`canCancel` from AppointmentDto → Appointment domain model → AppointmentCard (now uses API flags, not tab index)
✅ G4 — GET /portal/appointments?bucket=: MyAppointmentsUiState adds pending/cancelled lists + ViewModel loads 4 buckets in parallel (coroutineScope + async) + ScrollableTabRow with 4 tabs (Upcoming/Pending/Completed/Cancelled)
✅ G5 — GET /portal/health: CheckSmsAvailabilityUseCase + isSmsAvailable() in AuthRepository + LoginViewModel checks on init + dismissible amber banner on LoginScreen
✅ BuildConfig fix — `loginTenantId` now reads from `BuildConfig.TENANT_ID` (int=1) instead of DataStore slug resolution; fixes 401 for accounts on tenant 1 (e.g. mohitgupta@fivermail.com)

---

## Backend Gap Endpoints

| Endpoint | Status | Blocks |
|----------|--------|--------|
| POST /portal/register | ✅ Built + tested | Screen 4 |
| GET /portal/me/permissions | ✅ Live on backend | G2 — android calls it ✅ |
| GET /portal/health | ✅ Live on backend | G5 — android calls it ✅ |
| PATCH /portal/appointments/:id/reschedule | ✅ Live on backend | G3 — android UI + domain ✅ |
| GET /portal/appointments?bucket= | ✅ Live on backend | G4 — android 4-tab UI ✅ |
| GET /portal/me/dashboard | 🔴 Not yet on CRM backend | Screen 5 (mocked) |
| GET /portal/slots | 🔴 Not yet on CRM backend | Screen 6 Step 3 (mocked) |
| GET /portal/me/wallet | 🔴 Not yet on CRM backend | Screen 12 (mocked) |
| GET /portal/me/memberships | 🔴 Not yet on CRM backend | Screen 17 (mocked) |
| PUT /portal/me | 🔴 Not yet on CRM backend | Screen 15 edit (view-only) |
| POST /portal/me/fcm-token | 🔴 Not yet on CRM backend | Push registration |
| DELETE /portal/me/fcm-token | 🔴 Not yet on CRM backend | Push deregistration |
| GET /portal/me/treatment-plans | 🔴 Not yet on CRM backend | Screen 10 |
| GET /portal/me/consents | 🔴 Not yet on CRM backend | Screen 11 |
| GET /portal/me/consents/:id/pdf | 🔴 Not yet on CRM backend | Screen 11 |
| GET /portal/me/loyalty | 🔴 Not yet on CRM backend | Screen 14 |

---

## Full Git History (reference)

| Hash | Date | Commit message |
|------|------|----------------|
| `70027fc` | 2026-06-05 | feat: portal dashboard menu, Poppins font, section header upgrade + bug fixes |
| `3d6a804` | 2026-06-05 | feat: Wellness Lumina UI — warm palette, gradient heroes, fluid components |
| `6433348` | 2026-06-04 | feat: Enhanced Wellness Edition UI migration — design system overhaul |
| `fc30f19` | — | fix: display bugs + Phase 2 screens (Treatment Plans, Consent Forms, Loyalty) |
| `b73be3a` | — | chore: Phase 11 release prep — ProGuard, cert pins, Room migration, signing config |
| `3dcf655` | — | test: Phase 10 complete — Compose UI tests for BookAppointment, Memberships, Prescriptions |
| `f20f067` | — | feat: Phases 9–10 complete — FCM push notifications + full test suite |
| `2702d94` | — | feat: Phases 4–8 complete + live device test fixes |
| `f869b90` | — | feat: live API audit + Phase 3 Dashboard — all endpoints confirmed against staging |
| `2258156` | — | feat: Phase 2 auth — email+password login, register, splash branding |
| `de41dbd` | — | config: set staging and production BASE_URL per build type |
