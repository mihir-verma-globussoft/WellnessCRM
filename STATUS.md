# WellnessCRM Patient App — Implementation Status

Last updated: 2026-06-03 (session 1 complete)
Current phase: Phase 2 — Auth Feature (next session)

## Legend
✅ Done &nbsp; 🔄 In Progress &nbsp; ⬜ Not started &nbsp; 🔴 Blocked (reason inline)

---

## Backend Gap Endpoints
*Verified against CRM backend at `/home/glb-blr-214/StudioProjects/globussoft-crm` on 2026-06-03*

| Endpoint | Status | Blocks |
|----------|--------|--------|
| `POST /portal/register` | 🔴 BLOCKED — not yet built on CRM backend | Screen 4 (Register) |
| `GET /portal/me/dashboard` | 🔴 BLOCKED — not yet built on CRM backend | Screen 5 (Dashboard) |
| `GET /portal/slots` | 🔴 BLOCKED — not yet built on CRM backend | Screen 6 Step 3 (Slot picker) |
| `PUT /portal/me` | 🔴 BLOCKED — not yet built on CRM backend | Screen 15 (Profile edit) |
| `GET /portal/me/wallet` | 🔴 BLOCKED — not yet built on CRM backend | Screen 12 (Wallet) |
| `GET /portal/me/memberships` | 🔴 BLOCKED — not yet built on CRM backend | Screen 17 (Memberships) |
| `POST /portal/me/fcm-token` | 🔴 BLOCKED — not yet built on CRM backend | Push notifications |
| `DELETE /portal/me/fcm-token` | 🔴 BLOCKED — not yet built on CRM backend | Push notifications |
| `GET /portal/me/treatment-plans` | 🔴 BLOCKED — not yet built on CRM backend | Screen 10 Phase 2 |
| `GET /portal/me/consents` | 🔴 BLOCKED — not yet built on CRM backend | Screen 11 Phase 2 |
| `GET /portal/me/consents/:id/pdf` | 🔴 BLOCKED — not yet built on CRM backend | Screen 11 Phase 2 |
| `GET /portal/me/loyalty` | 🔴 BLOCKED — not yet built on CRM backend | Screen 14 Phase 2 |

Endpoints confirmed ✅ on backend: `POST /portal/login/verify-otp`, `GET /portal/me`, `GET /portal/visits`, `GET /portal/prescriptions`, `POST /portal/export`

⚠️ NOTE: Backend has `POST /portal/login` — need to verify if this is the OTP-request endpoint (CLAUDE.md specifies `/portal/login/request-otp`). Check when wiring auth feature.

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
| auth | `AuthDtos.kt` (OtpRequest, OtpVerify, OtpVerifyResponse, Register, TenantBranding) | ✅ |
| booking | `BookingDtos.kt` (Visit, Service, Location, Slot, Appointment, Book, Cancel) | ✅ |
| health | `HealthDtos.kt` (Prescription, Drug) | ✅ |
| membership | `MembershipDtos.kt` (Membership, MembershipPlan, Credits, History) | ✅ |
| profile | `ProfileDtos.kt` (Profile, UpdateProfile, DsarExport) | ✅ |
| wallet | `WalletDtos.kt` (Wallet, Transaction, GiftCard, GiftCardOrder, GiftCardConfirm, FcmToken) | ✅ |

---

## Phase 2 — Auth Feature

### Data layer
| File | Status |
|------|--------|
| `TenantBrandingDto.kt` + mapper | ⬜ (DTO created in Phase 1, mapper needed) |
| `AuthTokenDto.kt` + mapper | ⬜ (DTO created in Phase 1, mapper needed) |
| `AuthRepositoryImpl.kt` | ⬜ |

### Domain layer
| File | Status |
|------|--------|
| `AuthRepository.kt` (interface) | ⬜ |
| `RequestOtpUseCase.kt` + test | ⬜ |
| `VerifyOtpUseCase.kt` + test | ⬜ |
| `RegisterPatientUseCase.kt` + test | 🔴 BLOCKED — `POST /portal/register` not on backend (stub) |
| `LogoutUseCase.kt` + test | ⬜ |

### Presentation layer
| File | Status |
|------|--------|
| `SplashScreen.kt` + `SplashViewModel.kt` | ⬜ |
| `PhoneEntryScreen.kt` + `PhoneEntryViewModel.kt` | ⬜ |
| `OtpVerifyScreen.kt` + `OtpVerifyViewModel.kt` | ⬜ |
| `RegisterScreen.kt` + `RegisterViewModel.kt` | 🔴 BLOCKED — `POST /portal/register` not on backend (stub UI) |

---

## Phase 3 — Dashboard Feature

| File | Status |
|------|--------|
| `DashboardDto.kt` + mapper | 🔴 BLOCKED — `GET /portal/me/dashboard` not on backend |
| `DashboardRepository.kt` + impl | 🔴 BLOCKED |
| `GetDashboardUseCase.kt` + test | 🔴 BLOCKED |
| `DashboardScreen.kt` + `DashboardViewModel.kt` | 🔴 BLOCKED |

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
| `GetAvailableSlotsUseCase.kt` + test | 🔴 BLOCKED — `GET /portal/slots` not on backend |
| `BookAppointmentUseCase.kt` + test | ⬜ |
| `GetMyAppointmentsUseCase.kt` + test | ⬜ |
| `CancelAppointmentUseCase.kt` + test | ⬜ |
| `GetVisitHistoryUseCase.kt` + test | ⬜ |

### Presentation layer
| File | Status |
|------|--------|
| `BookAppointmentScreen.kt` (4-step) + `BookAppointmentViewModel.kt` | ⬜ |
| `MyAppointmentsScreen.kt` + `MyAppointmentsViewModel.kt` | ⬜ |
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
| `TreatmentPlanDto.kt` + mapper + Repository + UseCase | 🔴 BLOCKED — `GET /portal/me/treatment-plans` not on backend |
| `ConsentFormDto.kt` + mapper + Repository + UseCase | 🔴 BLOCKED — `GET /portal/me/consents` not on backend |

---

## Phase 6 — Membership Feature

### Data layer
| File | Status |
|------|--------|
| `MembershipDto.kt` + `MembershipPlanDto.kt` + mappers | ✅ (DTOs created in Phase 1, mappers ⬜) |
| `CachedMembership` entity + DAO | ✅ (created in Phase 1) |
| `MembershipRepositoryImpl.kt` | 🔴 BLOCKED — `GET /portal/me/memberships` not on backend |

### Domain layer
| File | Status |
|------|--------|
| `MembershipRepository.kt` (interface) | ⬜ |
| `GetMyMembershipsUseCase.kt` + test | 🔴 BLOCKED |
| `GetMembershipPlansUseCase.kt` + test | ⬜ |

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
| `WalletRepositoryImpl.kt` + `GiftCardRepositoryImpl.kt` | 🔴 BLOCKED — `GET /portal/me/wallet` not on backend |

### Domain layer
| File | Status |
|------|--------|
| `GetWalletUseCase.kt` + test | 🔴 BLOCKED |
| `GetGiftCardStorefrontUseCase.kt` + test | ⬜ |
| `InitiateGiftCardPurchaseUseCase.kt` + test | ⬜ |
| `ConfirmGiftCardPurchaseUseCase.kt` + test | ⬜ |

### Presentation layer
| File | Status |
|------|--------|
| `WalletScreen.kt` + `WalletViewModel.kt` | 🔴 BLOCKED |
| `GiftCardsScreen.kt` + `GiftCardsViewModel.kt` | ⬜ |
| `GiftCardPurchaseSheet.kt` (Razorpay flow) | ⬜ |

---

## Phase 8 — Profile & Notifications Feature

### Profile
| File | Status |
|------|--------|
| `ProfileRepositoryImpl.kt` | ⬜ |
| `GetProfileUseCase.kt` + test | ⬜ |
| `UpdateProfileUseCase.kt` + test | 🔴 BLOCKED — `PUT /portal/me` not on backend |
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
| `FcmHelper.kt` — register/deregister on login/logout (full) | 🔴 BLOCKED — `POST/DELETE /portal/me/fcm-token` not on backend |
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
| 2026-06-03 | Session 1 complete. Phase 0 + Phase 1 fully done. All 12 backend gap endpoints confirmed MISSING after checking `/home/glb-blr-214/StudioProjects/globussoft-crm`. |
| 2026-06-03 | `google-services.json` requires manual Firebase Console step — cannot auto-generate. App compiles without it. |
| 2026-06-03 | Backend has `POST /portal/login` (not `/request-otp`). Need to clarify endpoint name when wiring auth feature in Phase 2. |
| 2026-06-03 | Playfair Display loaded via Google Fonts downloadable fonts (XML-based, 5 weight/style variants). No binary .ttf needed. |
| 2026-06-03 | AGP version in libs.versions.toml set to 8.7.3 (stable) rather than 9.0.1 from original template — avoids unstable API. |
| 2026-06-03 | All DTO stubs created in Phase 1 to satisfy WellnessApiService Retrofit interface compilation (no circular dependencies). |
| 2026-06-03 | Next session: Phase 2 — Auth feature (data → domain → presentation). Start with AuthRepository interface + impl, then UseCases, then Splash/PhoneEntry/OtpVerify screens. |
