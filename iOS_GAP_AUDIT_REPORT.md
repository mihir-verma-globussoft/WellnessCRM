# WellnessCRM Patient App — iOS Gap Audit Report
# Android → iOS Data Flow & Parity Analysis

**Date:** 2026-06-10  
**Android Source:** `com.globussoft.wellness.patient` — Kotlin + Jetpack Compose (Build: SUCCESSFUL)  
**iOS Target:** Swift + SwiftUI + iOS 16+ + MVVM + Clean Architecture  
**Basis:** Full Android codebase audit + iOS Screen Migration Report (2026-06-09)  
**iOS Repo Status:** Not yet on local machine — all iOS screens are **MISSING** (baseline = 0%)

---

## Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Complete — fully implemented |
| 🔶 | Partial — exists but incomplete |
| ❌ | Missing — not yet built |
| ⚠️ | Different — implemented differently from Android |
| 🔴 | Blocked — depends on unbuilt backend endpoint |
| 🔑 | Security-critical — requires specific iOS handling |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Screen Parity Matrix](#2-screen-parity-matrix)
3. [API Parity Matrix](#3-api-parity-matrix)
4. [Model Parity Matrix](#4-model-parity-matrix)
5. [Screen-by-Screen Data Flow Audit](#5-screen-by-screen-data-flow-audit)
6. [Business Logic Checklist](#6-business-logic-checklist)
7. [Realtime & Push Systems Audit](#7-realtime--push-systems-audit)
8. [Navigation & Deep Link Audit](#8-navigation--deep-link-audit)
9. [Security & Storage Audit](#9-security--storage-audit)
10. [Implementation Priority Order](#10-implementation-priority-order)

---

## 1. Executive Summary

| Category | Android | iOS | Gap |
|----------|---------|-----|-----|
| Screens | 22 | 0 | 22 missing |
| Overlays / Sheets / Dialogs | 16 | 0 | 16 missing |
| Reusable Components | 7 core | 0 | 7 missing |
| API Endpoints | 37 | 0 | 37 missing |
| Domain Models | 24 | 0 | 24 missing |
| DTOs | 31 | 0 | 31 missing |
| ViewModels | 16 | 0 | 16 missing |
| UseCases | 22 | 0 | 22 missing |
| Repository Interfaces | 11 | 0 | 11 missing |
| Local Cache Entities | 4 | 0 | 4 missing |

**Overall parity: 0% — full implementation required.**

### Critical path items (must exist before any screen works)
1. `AppRouter` + `SessionManager` (auth state drives root view)
2. `WellnessAPIClient` (Keychain-backed JWT, base URL switch, global 401 handler)
3. `KeychainManager` (JWT + patient PII storage)
4. `WellnessTheme` (design tokens — colour, typography, shape)
5. `CoreDataStack` (4 local cache entities)
6. `AppError` sealed type (universal error representation)

### Known Android gaps that must NOT be replicated in iOS
| Gap | Android behaviour | iOS must do |
|-----|------------------|-------------|
| No global 401 handler | Each screen handles independently — inconsistent UX | Global `URLProtocol` or `URLSession` delegate intercepts 401, clears Keychain, routes to Login |
| FCM token registration | Backend is WebPush/VAPID only — `POST /portal/me/fcm-token` silently fails | APNs via Firebase; flag backend gap; fallback gracefully |
| Loyalty patientId | Backend does NOT verify ownership | iOS must read patientId from Keychain only — never from user input |

---

## 2. Screen Parity Matrix

| # | Screen | Android Route | iOS Status | Priority |
|---|--------|--------------|------------|---------|
| 1 | SplashScreen | `splash` | ❌ Missing | P0 |
| 2 | LoginScreen | `login` | ❌ Missing | P0 |
| 3 | RegisterScreen | `register` | ❌ Missing | P0 |
| 4 | DashboardScreen | `tab_home` | ❌ Missing | P1 |
| 5 | BookAppointmentScreen | `book_appointment` | ❌ Missing | P1 |
| 6 | MyAppointmentsScreen | `tab_bookings` | ❌ Missing | P1 |
| 7 | VisitHistoryScreen | `visit_history` | ❌ Missing | P2 |
| 8 | WaitlistScreen | `waitlist` | ❌ Missing | P2 |
| 9 | CatalogTabScreen | `tab_catalog` | ❌ Missing | P1 |
| 10 | FinanceTabScreen | `tab_finance` | ❌ Missing | P2 |
| 11 | PrescriptionsScreen | `prescriptions` | ❌ Missing | P1 |
| 12 | PrescriptionPdfScreen | `prescription_pdf/{id}` | ❌ Missing | P2 |
| 13 | TreatmentPlansScreen | `treatment_plans` | ❌ Missing | P2 |
| 14 | ConsentFormsScreen | `consent_forms` | ❌ Missing | P2 |
| 15 | ConsentFormPdfScreen | `consent_form_pdf/{id}` | ❌ Missing | P3 |
| 16 | MembershipsScreen | `memberships` | ❌ Missing | P2 |
| 17 | WalletScreen | `wallet` | ❌ Missing | P2 |
| 18 | GiftCardsScreen | `gift_cards` | ❌ Missing | P2 |
| 19 | LoyaltyScreen | `loyalty` | ❌ Missing | P3 |
| 20 | ProfileScreen | `tab_profile` | ❌ Missing | P1 |
| 21 | NotificationInboxScreen | `notifications` | ❌ Missing | P2 |
| 22 | NotificationSettingsScreen | `notification_settings` | ❌ Missing | P2 |

### Overlay / Sheet / Dialog Parity

| # | Overlay | Parent Screen | iOS Status |
|---|---------|--------------|------------|
| A | VisitDetailSheet | VisitHistoryScreen | ❌ Missing |
| B | AddWaitlistSheet | WaitlistScreen | ❌ Missing |
| C | ServiceDetailSheet | CatalogTabScreen | ❌ Missing |
| D | AppointmentActionSheet | MyAppointmentsScreen | ❌ Missing |
| E | RescheduleSheet | MyAppointmentsScreen | ❌ Missing |
| F | CancelConfirmDialog | MyAppointmentsScreen | ❌ Missing |
| G | PlanDetailSheet | MembershipsScreen | ❌ Missing |
| H | JoinConfirmDialog | MembershipsScreen | ❌ Missing |
| I | TransactionReceiptSheet | WalletScreen | ❌ Missing |
| J | GiftCardBuySheet | GiftCardsScreen | ❌ Missing |
| K | PaymentActionSheet | FinanceTabScreen | ❌ Missing |
| L | RefundConfirmDialog | FinanceTabScreen | ❌ Missing |
| M | EditProfileSheet | ProfileScreen | ❌ Missing |
| N | ChangePasswordSection | ProfileScreen | ❌ Missing |
| O | WellnessTopAppBar | App-wide | ❌ Missing |
| P | WellnessBottomNavBar | App-wide | ❌ Missing |

---

## 3. API Parity Matrix

### Auth APIs

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `/api/auth/login` | POST | `LoginRequestDto` → `LoginResponseDto` | ❌ Missing | Absolute path — not under `/api/wellness/` |
| `/api/auth/customer/register` | POST | `RegisterRequestDto` → `RegisterResponseDto` | ❌ Missing | Absolute path |
| `/api/auth/me` | GET | → `AuthProfileResponseDto` | ❌ Missing | User-layer profile |
| `/api/auth/me` | PUT | `UpdateAuthProfileDto` → `AuthProfileResponseDto` | ❌ Missing | name/email/password only |
| `/api/auth/me/profile-picture` | POST (multipart) | `MultipartBody.Part` → `AuthProfileResponseDto` | ❌ Missing | `multipart/form-data` |
| `/api/auth/me/profile-picture` | DELETE | → `AuthProfileResponseDto` | ❌ Missing | |

### Portal Auth APIs

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `portal/health` | GET | → `PortalHealthDto` | ❌ Missing | SMS availability check |
| `portal/me/permissions` | GET | → `PatientPermissionsDto` | ❌ Missing | Guards prescriptions feature |
| `portal/me` | GET | → `ProfileDto` | ❌ Missing | Patient-layer profile |
| `portal/export` | POST | → `DsarExportResponseDto` | ❌ Missing | DSAR / data export |

### Tenant Branding

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `public/tenant/{slug}` | GET | → `TenantBrandingResponseDto` | ❌ Missing | Called on Splash; drives brand colour |

### Appointments & Visits

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `portal/appointments` | GET + `?bucket=` | → `AppointmentListResponseDto` | ❌ Missing | bucket = upcoming\|past\|pending\|cancelled |
| `portal/appointments/book` | POST | `BookAppointmentDto` → `BookAppointmentResponseDto` | ❌ Missing | Date must be `YYYY-MM-DD` not ISO8601 |
| `portal/appointments/{id}/cancel` | POST | → `CancelAppointmentResponseDto` | ❌ Missing | |
| `portal/appointments/{id}/reschedule` | PATCH | `RescheduleAppointmentDto` → `RescheduleAppointmentResponseDto` | ❌ Missing | |
| `portal/visits` | GET + `?upcoming=` | → `List<VisitDto>` | ❌ Missing | Limit 50 rows |
| `doctors/availability` | GET + `?date=` | → `List<DoctorAvailabilityDto>` | ❌ Missing | date = `YYYY-MM-DD` |

### Waitlist

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `waitlist` | GET | → `List<WaitlistEntryDto>` | ❌ Missing | No `portal/` prefix |
| `waitlist` | POST | `AddWaitlistDto` → `WaitlistEntryDto` | ❌ Missing | `patientId` MANDATORY in body |

### Catalog

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `services` | GET + `?public=true` | → `List<ProductDto>` | ❌ Missing | CUSTOMER role — `portal/products` returns 403 |
| `portal/product-categories` | GET | → `List<ProductCategoryDto>` | ❌ Missing | |
| `service-categories` | GET + `?public=true` | → `List<CatalogServiceCategoryDto>` | ❌ Missing | `_count` field in response |

### Health

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `portal/prescriptions` | GET | → `List<PrescriptionDto>` | ❌ Missing | `drugs` is JSON-encoded String, NOT array |
| `portal/prescriptions/{id}/pdf` | GET | → `ResponseBody` (binary) | ❌ Missing | PDF bytes — cache 7 days |
| `patients/{patientId}/treatment-plans` | GET | → `List<TreatmentPlanDto>` | ❌ Missing | patientId from Keychain |
| `patients/{patientId}/consents` | GET | → `List<ConsentFormDto>` | ❌ Missing | |
| `consents/{id}/pdf` | GET | → `ResponseBody` (binary) | ❌ Missing | |

### Memberships

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `appointments/my-memberships` | GET | → `List<MembershipDto>` | ❌ Missing | `balance` always `[]` — backend gap |
| `membership-plans` | GET | → `List<MembershipPlanDto>` | ❌ Missing | `entitlements` is raw JSON string |

### Wallet & Finance

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `patients/{patientId}/wallet` | GET | → `PatientWalletResponseDto` | ❌ Missing | |
| `my-transactions` | GET + `?from=&to=` | → `MyTransactionsResponseDto` | ❌ Missing | Also used by Dashboard for wallet balance |
| `giftcards/storefront` | GET | → `GiftCardStorefrontResponseDto` | ❌ Missing | |
| `giftcards/{id}/purchase/order` | POST | `GiftCardOrderDto` → `GiftCardOrderResponseDto` | ❌ Missing | Returns Razorpay key + amount in paise |
| `giftcards/{id}/purchase/confirm` | POST | `GiftCardConfirmDto` → `GiftCardConfirmResponseDto` | ❌ Missing | Called after Razorpay SDK success |

### Loyalty

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `loyalty/{patientId}` | GET | → `LoyaltyResponseDto` | ❌ Missing | 🔑 Backend does NOT verify patientId ownership |

### Payments

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `/api/payments` | GET | → `List<PaymentDto>` | ❌ Missing | Absolute path |
| `/api/payments/config` | GET | → `PaymentConfigDto` | ❌ Missing | Absolute path |
| `/api/payments/{id}/refund` | POST | → `PaymentDto` | ❌ Missing | Absolute path |

### Notifications (FCM)

| Endpoint | Method | Android DTO | iOS Status | Notes |
|----------|--------|-------------|------------|-------|
| `portal/me/fcm-token` | POST | `FcmTokenDto` → `Unit` | ❌ Missing | 🔴 Backend is WebPush/VAPID — silently fails for mobile |
| `portal/me/fcm-token` | DELETE | → `Unit` | ❌ Missing | Called on logout |

---

## 4. Model Parity Matrix

### Auth Models

| Android Model | Fields | iOS Swift Model | Status | Missing / Notes |
|--------------|--------|----------------|--------|----------------|
| `LoginRequestDto` | `email: String, password: String` | `LoginRequest` | ❌ Missing | — |
| `LoginResponseDto` | `token: String, user: UserDto, tenant: TenantDto?` | `LoginResponse` | ❌ Missing | — |
| `RegisterRequestDto` | `email, password, name, registrationTenantId: Int` | `RegisterRequest` | ❌ Missing | tenantId injected from `BuildConfig` equivalent |
| `RegisterResponseDto` | `token: String, user: UserDto` | `RegisterResponse` | ❌ Missing | — |
| `TenantBrandingResponseDto` | `id, name, slug, logo?, brandColor?, phone?` | `TenantBranding` | ❌ Missing | — |
| `PatientPermissionsDto` | `permissions: List<String>` | `PatientPermissions` | ❌ Missing | `has(_ permission: String) -> Bool` method |
| `PortalHealthDto` | `smsConfigured: Boolean` | `PortalHealth` | ❌ Missing | — |
| `AuthProfileResponseDto` | `id, name, email, role, profilePicture?` | `AuthProfile` | ❌ Missing | User-layer; different from ProfileDto |
| `UpdateAuthProfileDto` | `name?, email?, currentPassword?, newPassword?` | `UpdateAuthProfileRequest` | ❌ Missing | All fields optional |

### Profile Models

| Android Model | Fields | iOS Swift Model | Status | Missing / Notes |
|--------------|--------|----------------|--------|----------------|
| `ProfileDto` | `id: Int, name, phone, email, dob?, gender?` | `PatientProfile` | ❌ Missing | Patient-layer; phone/dob/gender NOT editable |
| `DsarExportResponseDto` | `message: String, exportedAt?` | `DsarExportResponse` | ❌ Missing | — |

### Booking Models

| Android Model | Fields | iOS Swift Model | Status | Missing / Notes |
|--------------|--------|----------------|--------|----------------|
| `BookAppointmentDto` | `appointmentDate: String (YYYY-MM-DD), appointmentTime, reason, doctorId?, serviceId?, membershipId?` | `BookAppointmentRequest` | ❌ Missing | ⚠️ Date MUST be `YYYY-MM-DD` not ISO8601 |
| `BookAppointmentResponseDto` | `success: Boolean, appointment: AppointmentDto` | `BookAppointmentResponse` | ❌ Missing | — |
| `AppointmentListResponseDto` | `bucket: String, count: Int, appointments: List<AppointmentDto>` | `AppointmentListResponse` | ❌ Missing | — |
| `AppointmentDto` (inner) | `id, patientName, doctorName, serviceName, appointmentDate, appointmentTime, status, reason, doctorAssigned: Boolean, bookingType?, videoCallUrl?` | `Appointment` | ❌ Missing | status values: booked\|pending\|arrived\|checked-in\|in-treatment\|completed\|cancelled |
| `CancelAppointmentResponseDto` | `success: Boolean, appointment: AppointmentDto` | `CancelAppointmentResponse` | ❌ Missing | — |
| `RescheduleAppointmentDto` | `appointmentDate: String, appointmentTime: String` | `RescheduleRequest` | ❌ Missing | — |
| `RescheduleAppointmentResponseDto` | `success: Boolean, appointment: AppointmentDto` | `RescheduleResponse` | ❌ Missing | — |
| `VisitDto` | `id, visitDate: String, status, service: ServiceRef?, doctor: DoctorRef?, locationName?, bookingType?, videoCallUrl?, amountCharged?` | `Visit` | ❌ Missing | — |
| `DoctorAvailabilityDto` | `id, name, specialty?, availableSlots: List<String>` | `DoctorAvailability` | ❌ Missing | — |
| `WaitlistEntryDto` | `id, serviceId, serviceName?, status, notes?, createdAt: String, patientId` | `WaitlistEntry` | ❌ Missing | — |
| `AddWaitlistDto` | `serviceId: Int, patientId: Int, notes?` | `AddWaitlistRequest` | ❌ Missing | ⚠️ patientId MANDATORY |
| `ProductDto` | `id, name, category: String, price?, description?, duration?` | `Service` | ❌ Missing | ⚠️ `category` is String not Object |
| `ProductCategoryDto` | `id, name, serviceCount?` | `ServiceCategory` | ❌ Missing | — |

### Health Models

| Android Model | Fields | iOS Swift Model | Status | Missing / Notes |
|--------------|--------|----------------|--------|----------------|
| `PrescriptionDto` | `id, visitId?, visitDate, doctorName?, serviceName?, drugs: String` | `Prescription` | ❌ Missing | ⚠️ `drugs` is JSON-encoded String — must be decoded twice |
| `DrugDto` (inner, within JSON) | `name, dosage?, frequency?, duration?` | `Drug` | ❌ Missing | Second-level JSON decode from `drugs` string |
| `TreatmentPlanDto` | `id, name, status, totalSessions, completedSessions, startDate?, endDate?` | `TreatmentPlan` | ❌ Missing | — |
| `ConsentFormDto` | `id, name, signedAt, status, patientId` | `ConsentForm` | ❌ Missing | — |

### Membership Models

| Android Model | Fields | iOS Swift Model | Status | Missing / Notes |
|--------------|--------|----------------|--------|----------------|
| `MembershipDto` | `id, planId, planName, status, startDate, endDate, daysLeft, creditsJson: String, historyJson: String, balance: List (always empty)` | `Membership` | ❌ Missing | `balance` always `[]` — backend gap |
| `MembershipPlanDto` | `id, name, price, currency, durationDays, entitlements: String (raw JSON)` | `MembershipPlan` | ❌ Missing | `entitlements` raw JSON — parse client-side |

### Wallet & Finance Models

| Android Model | Fields | iOS Swift Model | Status | Missing / Notes |
|--------------|--------|----------------|--------|----------------|
| `PatientWalletResponseDto` | `walletBalance, giftCardBalance, currency` | `PatientWallet` | ❌ Missing | — |
| `MyTransactionsResponseDto` | `currency, summary: SummaryDto, transactions: List<TransactionDto>` | `TransactionsResponse` | ❌ Missing | — |
| `TransactionDto` | `id: String, type, category, title, amount: Double, direction: "credit"\|"debit", status, date: String` | `Transaction` | ❌ Missing | `direction` drives colour (green/red) |
| `GiftCardStorefrontResponseDto` | `giftCards: List<GiftCardDto>` | `GiftCardStorefront` | ❌ Missing | — |
| `GiftCardDto` | `id, name, amount: Double, price: Double, color?, validityDays, currency, expiresAt?` | `GiftCard` | ❌ Missing | — |
| `GiftCardOrderDto` | `patientId?: Int` | `GiftCardOrderRequest` | ❌ Missing | — |
| `GiftCardOrderResponseDto` | `orderId, paymentId, key (Razorpay), amount (paise), currency, giftCardId, patientId, patientName` | `GiftCardOrderResponse` | ❌ Missing | `amount` in paise → ÷100 for display |
| `GiftCardConfirmDto` | `paymentId, razorpay_order_id, razorpay_payment_id, razorpay_signature` | `GiftCardConfirmRequest` | ❌ Missing | — |
| `GiftCardConfirmResponseDto` | `giftCard, transaction` | `GiftCardConfirmResponse` | ❌ Missing | — |
| `PaymentDto` | `id: Int, amount: Long?, currency?, status?, gateway?, description?, createdAt?` | `Payment` | ❌ Missing | All fields optional except `id` |
| `PaymentConfigDto` | `razorpayKey, currency?` | `PaymentConfig` | ❌ Missing | — |

### Loyalty Models

| Android Model | Fields | iOS Swift Model | Status | Missing / Notes |
|--------------|--------|----------------|--------|----------------|
| `LoyaltyResponseDto` | `patientId, points, tier?, referralCode?, referralCount, history: List<LoyaltyHistoryDto>` | `LoyaltyData` | ❌ Missing | 🔑 never pass patientId from user input |
| `FcmTokenDto` | `token: String, platform: "android"` | `FcmTokenRequest` | ❌ Missing | platform = `"ios"` in iOS implementation |

### Local Cache Entities (CoreData / SwiftData)

| Android Room Entity | Fields | iOS Entity | Status | TTL |
|--------------------|--------|-----------|--------|-----|
| `CachedVisit` | `id, visitDate: Long, status, serviceName?, doctorName?, locationName?, bookingType, videoCallUrl?, amountCharged?, cachedAt` | `CachedVisit` | ❌ Missing | Cleared on logout |
| `CachedPrescription` | `id, visitId, visitDate: Long, doctorName?, serviceName?, drugCount, pdfBytes: ByteArray?, pdfCachedAt: Long?, cachedAt` | `CachedPrescription` | ❌ Missing | PDF bytes: 7-day TTL |
| `CachedMembership` | `id, status, startDate: Long, endDate: Long, daysLeft, planName, planPrice, planCurrency, creditsJson, historyJson, cachedAt` | `CachedMembership` | ❌ Missing | Cleared on logout |
| `CachedNotification` | `id: String (UUID), type, title, body, screen?, entityId?, isRead: Boolean, receivedAt: Long` | `CachedNotification` | ❌ Missing | 90-day TTL |

---

## 5. Screen-by-Screen Data Flow Audit

Each screen shows:
- **Android data flow** (confirmed from source code)
- **iOS required data flow** (must match exactly)
- **Critical rules** (deviations that caused bugs in Android and must not be repeated)

---

### 5.1 SplashScreen

**Status:** ❌ Missing

**Android data flow:**
```
SplashScreen
  → SplashViewModel.init()
    → CheckAuthStatusUseCase.invoke()
      → TokenManager.getToken() [DataStore]
      → if token != null → GetTenantBrandingUseCase.invoke()
        → AuthRepository.getTenantBranding(slug: BuildConfig.TENANT_SLUG)
          → GET public/tenant/{slug}
          → TenantBrandingResponseDto.toDomain() → TenantBranding
          → DataStoreManager.saveBrandColor(hex)
          → DataStoreManager.saveClinicName(name)
      → if valid → Channel.send(NavEvent.NavigateToDashboard)
      → if null/expired → Channel.send(NavEvent.NavigateToLogin)
```

**iOS required data flow:**
```swift
SplashView.onAppear
  → SplashViewModel.checkAuth()
    → CheckAuthStatusUseCase.invoke()
      → KeychainManager.getJWT()          // replaces DataStore
      → if jwt != nil:
          → GetTenantBrandingUseCase.invoke(slug: AppConfig.tenantSlug)
            → AuthRepository.fetchTenantBranding()
              → GET public/tenant/{slug}
              → map TenantBrandingResponse → TenantBranding domain model
              → AppStorage.brandColorHex = branding.brandColor
              → AppStorage.clinicName = branding.name
          → AppRouter.route = .main
      → if nil: AppRouter.route = .auth(.login)
```

**Critical rules:**
- Minimum 0.5s display before routing (avoids flash)
- Background colour must be `#133F3E` (hardcoded) — matches logo border
- Logo must be 70% of screen width, 1:1 aspect ratio
- Error text shown if branding fetch fails — still auto-routes to Login

---

### 5.2 LoginScreen

**Status:** ❌ Missing

**Android data flow:**
```
LoginScreen
  → LoginViewModel.init()
    → CheckSmsAvailabilityUseCase.invoke()
      → AuthRepository.getPortalHealth()
        → GET portal/health
        → if !smsConfigured → _uiState.smsUnavailable = true

  → onEvent(Submit)
    → LoginUseCase.invoke(email, password)
      → AuthRepository.login(LoginRequestDto(email, password))
        → POST /api/auth/login              ← ABSOLUTE PATH (not under /api/wellness/)
        → on 200: LoginResponseDto
          → TokenManager.saveToken(token)   [DataStore]
          → EncryptedPrefsManager.saveUserId(user.id)
          → EncryptedPrefsManager.saveUserName(user.name)
          → EncryptedPrefsManager.saveUserEmail(user.email)
          → EncryptedPrefsManager.savePatientId(user.patientId)
          → FcmHelper.registerToken()       [POST portal/me/fcm-token — silently fails]
        → on 400: Result.Error("INVALID_INPUT", message)
        → on 401: Result.Error("INVALID_CREDENTIALS", "Invalid email or password")
      → Channel.send(NavEvent.NavigateToDashboard)
```

**iOS required data flow:**
```swift
LoginView
  → LoginViewModel.onAppear()
    → CheckSmsAvailabilityUseCase.invoke()
      → AuthRepository.getPortalHealth()
        → GET portal/health → PortalHealth
        → viewModel.smsUnavailable = !health.smsConfigured

  → loginViewModel.submit()
    → LoginUseCase.invoke(email: email, password: password)
      → AuthRepository.login(request: LoginRequest(email, password))
        → POST /api/auth/login              ← absolute path; base URL override required
        → on success: LoginResponse
          → KeychainManager.saveJWT(response.token)
          → KeychainManager.saveUserId(response.user.id)
          → KeychainManager.saveUserName(response.user.name)
          → KeychainManager.saveUserEmail(response.user.email)
          → KeychainManager.savePatientId(response.user.patientId)
          → FcmTokenService.register()      [async, fire-and-forget]
        → AppRouter.route = .main
      → on AppError.invalidCredentials: viewModel.error = "Invalid email or password"
      → on AppError.network: viewModel.error = "No internet connection"
```

**Critical rules:**
- `POST /api/auth/login` is an ABSOLUTE path — the iOS URL session must bypass the `/api/wellness/` base URL for this call. Use a separate `URLSession` or `URLRequest` with the full URL for auth endpoints.
- Error body parsing: server returns `{ "error": "message" }` — extract the `error` field, not the HTTP status text.
- Credentials MUST go to Keychain — never `UserDefaults` or `AppStorage`.
- FCM registration is fire-and-forget — failure must NOT block login flow.

---

### 5.3 RegisterScreen

**Status:** ❌ Missing

**Android data flow:**
```
RegisterScreen
  → onEvent(Submit)
    → validate: password == confirmPassword (client-side before API)
    → RegisterPatientUseCase.invoke(name, email, password)
      → AuthRepository.registerCustomer(
          RegisterRequestDto(email, password, name, BuildConfig.TENANT_ID))
        → POST /api/auth/customer/register   ← ABSOLUTE PATH
        → on 200: RegisterResponseDto
          → [same token storage as LoginScreen]
        → on 400: parse error body
      → Channel.send(NavEvent.NavigateToDashboard)
```

**iOS required data flow:**
```swift
RegisterViewModel.submit()
  → guard password == confirmPassword else { error = "Passwords do not match"; return }
  → RegisterPatientUseCase.invoke(name: name, email: email, password: password)
    → AuthRepository.register(
        request: RegisterRequest(email, password, name, registrationTenantId: AppConfig.tenantId))
      → POST /api/auth/customer/register    ← absolute path
      → on success: [same Keychain storage as LoginScreen]
      → AppRouter.route = .main
```

**Critical rules:**
- `registrationTenantId` must come from `AppConfig` (equivalent of `BuildConfig.TENANT_ID`) — never hardcoded in a view.
- Confirm password check is client-side only — do not send `confirmPassword` to API.
- Absolute path — same URL override needed as Login.

---

### 5.4 DashboardScreen

**Status:** ❌ Missing

**Android data flow:**
```
DashboardScreen
  → DashboardViewModel.init()
    → GetDashboardUseCase.invoke()
      [3 parallel API calls]
      → DashboardRepository.getWalletSummary()
        → GET my-transactions
        → extracts summary.walletBalance
      → DashboardRepository.getNextAppointment()
        → GET portal/appointments?bucket=upcoming
        → first item in appointments list
      → DashboardRepository.getMembershipStatus()
        → GET appointments/my-memberships
        → first active membership or null

  → EncryptedPrefsManager.getUserName() → greeting
  → time-of-day greeting logic:
      hour < 12  → "Good morning"
      hour < 17  → "Good afternoon"
      else       → "Good evening"
```

**iOS required data flow:**
```swift
DashboardViewModel.loadDashboard()
  → async let walletResult   = WalletRepository.getTransactionSummary()    // GET my-transactions
  → async let apptResult     = AppointmentRepository.getUpcoming()          // GET portal/appointments?bucket=upcoming
  → async let membershipResult = MembershipRepository.getMyMemberships()    // GET appointments/my-memberships

  → (wallet, appt, memberships) = try await (walletResult, apptResult, membershipResult)

  → customerName = KeychainManager.getUserName()
  → greeting = greetingForHour(Calendar.current.component(.hour, from: Date()))
  → walletBalance = wallet.summary.walletBalance
  → nextAppointment = appt.appointments.first
  → membershipStatus = memberships.first(where: { $0.status == "active" })?.planName ?? "—"
```

**Critical rules:**
- All 3 API calls must be parallel (`async let`) — not sequential.
- Greeting logic is time-of-day based — must use device local time (not UTC).
- `membershipStatus` displays "Active" if any active membership exists, "—" if none — not a count.
- Stats row: Wallet shows formatted rupees (₹ + 2 decimal places), Membership shows text, Loyalty shows points integer.

---

### 5.5 BookAppointmentScreen (4-step wizard)

**Status:** ❌ Missing

**Android data flow:**
```
STEP 1 — Service Selection
  → BookAppointmentViewModel.init()
    → GetPortalProductsUseCase.invoke()
      → AppointmentRepository.getProducts()
        → GET services?public=true
        → List<ProductDto> → List<Product>

STEP 2 — Doctor Selection
  → onEvent(DateSelected(date))
    → DoctorAvailabilityUseCase.invoke(date)
      → AppointmentRepository.getDoctorAvailability(date: "YYYY-MM-DD")
        → GET doctors/availability?date=YYYY-MM-DD
        → List<DoctorAvailabilityDto> → List<DoctorOption>
      → prepend DoctorOption(id=null, name="No preference")

STEP 3 — Date & Time (fixed from Step 2 date selection; time picked here)

STEP 4 — Confirm & Book
  → onEvent(BookAppointment)
    → BookAppointmentUseCase.invoke(...)
      → AppointmentRepository.bookAppointment(
          BookAppointmentDto(
            appointmentDate = "YYYY-MM-DD",   ← CRITICAL: not ISO8601
            appointmentTime = "HH:mm",
            reason = reason,
            doctorId = selectedDoctorId,      ← null if "No preference"
            serviceId = selectedServiceId,
            membershipId = selectedMembershipId
          ))
        → POST portal/appointments/book
        → BookAppointmentResponseDto → BookAppointmentResponse
      → _uiState.bookingSuccess = response.appointment
```

**iOS required data flow:**
```swift
// Step 1
BookAppointmentViewModel.loadServices()
  → GetPortalProductsUseCase.invoke()
    → AppointmentRepository.getServices()
      → GET services?public=true → [Service]

// Step 2
BookAppointmentViewModel.loadDoctors(for date: Date)
  → let dateStr = ISO8601DateFormatter().string(from: date).prefix(10)  // "YYYY-MM-DD"
  → GetDoctorAvailabilityUseCase.invoke(date: dateStr)
    → AppointmentRepository.getDoctorAvailability(date: dateStr)
      → GET doctors/availability?date=YYYY-MM-DD → [DoctorAvailability]
  → doctors = [DoctorAvailability(id: nil, name: "No preference")] + result

// Step 4 — Book
BookAppointmentViewModel.bookAppointment()
  → let dateStr = formatDate(selectedDate, format: "yyyy-MM-dd")  // "YYYY-MM-DD" — NOT ISO8601
  → BookAppointmentUseCase.invoke(
      date: dateStr,
      time: selectedTime,        // "HH:mm"
      reason: reason,
      doctorId: selectedDoctorId,
      serviceId: selectedServiceId,
      membershipId: selectedMembershipId)
    → AppointmentRepository.book(request: BookAppointmentRequest(...))
      → POST portal/appointments/book
      → BookAppointmentResponse
  → viewModel.bookingSuccess = response.appointment
```

**Critical rules:**
- `appointmentDate` MUST be `"YYYY-MM-DD"` — the Android app had a production bug where ISO8601 with time component (`"2026-06-10T00:00:00Z"`) was rejected by the backend. Use `DateFormatter` with format `"yyyy-MM-dd"`.
- `doctorId = null` is valid and means "No preference" — the API accepts it.
- `appointmentTime` is `"HH:mm"` 24-hour string (e.g., `"14:30"`).
- Step 2 date picker: only today and future dates allowed. `DatePicker(in: Date()...)`.
- Services load on init (once). Doctor availability reloads every time the date changes.
- On booking success: show inline confirmation card (do NOT auto-navigate) — display appointment ID, patient name, doctor, date, status.

---

### 5.6 MyAppointmentsScreen

**Status:** ❌ Missing

**Android data flow:**
```
MyAppointmentsViewModel.init()
  → LoadAppointmentsUseCase.invoke()
    [4 parallel calls]
    → GET portal/appointments?bucket=upcoming  → upcoming list
    → GET portal/appointments?bucket=past      → completed list
    → GET portal/appointments?bucket=pending   → pending list
    → GET portal/appointments?bucket=cancelled → cancelled list

onEvent(CancelAppointment(id))
  → CancelAppointmentUseCase.invoke(id)
    → AppointmentRepository.cancelAppointment(id)
      → POST portal/appointments/{id}/cancel
      → reload all 4 buckets

onEvent(RescheduleAppointment(id, date, time))
  → RescheduleAppointmentUseCase.invoke(id, date, time)
    → AppointmentRepository.rescheduleAppointment(
        id, RescheduleAppointmentDto(appointmentDate, appointmentTime))
      → PATCH portal/appointments/{id}/reschedule
      → reload all 4 buckets
```

**iOS required data flow:**
```swift
MyAppointmentsViewModel.loadAll()
  → async let upcoming   = repo.getAppointments(bucket: "upcoming")
  → async let past       = repo.getAppointments(bucket: "past")
  → async let pending    = repo.getAppointments(bucket: "pending")
  → async let cancelled  = repo.getAppointments(bucket: "cancelled")
  → (a, b, c, d) = try await (upcoming, past, pending, cancelled)

MyAppointmentsViewModel.cancelAppointment(_ id: Int)
  → isLoading = true
  → CancelAppointmentUseCase.invoke(id)
    → POST portal/appointments/{id}/cancel
  → await loadAll()    // reload all 4 buckets

MyAppointmentsViewModel.rescheduleAppointment(id: Int, date: Date, time: String)
  → let dateStr = formatDate(date, format: "yyyy-MM-dd")
  → RescheduleAppointmentUseCase.invoke(id: id, date: dateStr, time: time)
    → PATCH portal/appointments/{id}/reschedule
  → await loadAll()
```

**Critical rules:**
- 4 bucket API calls must be parallel.
- After cancel or reschedule: reload all 4 buckets (not just the affected one).
- KPI chip counts: each chip shows the `count` field from `AppointmentListResponseDto` — not `appointments.count` (the list may be paginated in future).
- LazyColumn/List bottom padding: 96pt to avoid FAB overlap.
- Reschedule date format: same `"yyyy-MM-dd"` rule as BookAppointment.

---

### 5.7 VisitHistoryScreen

**Status:** ❌ Missing

**Android data flow:**
```
VisitHistoryViewModel.init()
  → GetVisitHistoryUseCase.invoke()
    → AppointmentRepository.getVisits(upcoming = null)
      → GET portal/visits          ← no ?upcoming param = all visits
      → List<VisitDto> → List<Visit>
      → cache in Room (CachedVisit)
    → on IOException: return cached visits from Room

  groupBy: visits.groupBy { DateUtil.toDisplayMonthYear(it.visitDate) }
  → sections sorted reverse-chronologically
```

**iOS required data flow:**
```swift
VisitHistoryViewModel.load()
  → GetVisitHistoryUseCase.invoke()
    → VisitRepository.getVisits()
      → GET portal/visits → [Visit]
      → CoreData.save(visits)         // cache for offline
    → on URLError: load from CoreData
  → groupedVisits = Dictionary(grouping: visits) { formatMonthYear($0.visitDate) }
  → sortedKeys = groupedVisits.keys.sorted(by: >)   // newest month first
```

**Critical rules:**
- Offline fallback: if network unavailable, show cached visits (Room/CoreData) — show cached banner.
- Month grouping key format: `"June 2026"` (full month name + year).
- VisitDetailSheet: tapping a card shows all available fields — omit rows where value is nil.

---

### 5.8 WaitlistScreen

**Status:** ❌ Missing

**Android data flow:**
```
WaitlistViewModel.init()
  → LoadWaitlistUseCase.invoke()
    → AppointmentRepository.getWaitlist()
      → GET waitlist              ← NOT "portal/waitlist"
      → List<WaitlistEntryDto> → List<WaitlistEntry>
    → GetPortalProductsUseCase.invoke()
      → GET services?public=true  ← for the service dropdown in AddWaitlistSheet

onEvent(SubmitWaitlist)
  → AddToWaitlistUseCase.invoke(serviceId, patientId, notes)
    → AppointmentRepository.addToWaitlist(
        AddWaitlistDto(
          serviceId = selectedServiceId,
          patientId = EncryptedPrefsManager.getPatientId(),   ← MANDATORY
          notes = formNotes.ifEmpty { null }
        ))
      → POST waitlist
      → reload list
```

**iOS required data flow:**
```swift
WaitlistViewModel.load() async
  → async let entries  = WaitlistRepository.getWaitlist()      // GET waitlist
  → async let services = ServiceRepository.getServices()       // GET services?public=true
  → (e, s) = try await (entries, services)

WaitlistViewModel.submitWaitlist() async
  → guard let serviceId = selectedServiceId else { return }
  → let patientId = KeychainManager.getPatientId()            // MANDATORY — from Keychain
  → AddToWaitlistUseCase.invoke(
        serviceId: serviceId,
        patientId: patientId,
        notes: formNotes.isEmpty ? nil : formNotes)
    → POST waitlist body: { serviceId, patientId, notes? }
  → showAddSheet = false
  → await load()
```

**Critical rules:**
- Endpoint is `waitlist` NOT `portal/waitlist` — the `portal/waitlist` path 404s on the backend.
- `patientId` is MANDATORY in the POST body — the backend returns HTTP 400 without it.
- `patientId` must come from Keychain — never from a form field or user input.
- Load services in parallel with waitlist entries on init.

---

### 5.9 CatalogTabScreen

**Status:** ❌ Missing

**Android data flow:**
```
CatalogViewModel.init()
  → GetServicesUseCase.invoke()
    → CatalogRepository.getServices()
      → GET services?public=true
      → List<CatalogServiceDto> → List<CatalogService>
  → GetCategoriesUseCase.invoke()
    → CatalogRepository.getCategories()
      → GET service-categories?public=true
      → List<CatalogServiceCategoryDto> → List<CatalogServiceCategory>
      [note: CatalogServiceCategoryDto has @Json(name="_count") val count: CategoryCountDto?]

MembershipsViewModel (injected separately for inline tab)
  → [see MembershipsScreen data flow]

Categories tab → tap → switches to Services tab, sets activeCategory filter
Services tab → tap card → ServiceDetailSheet
ServiceDetailSheet → "Book this service" CTA → navController.navigate(Screen.BookAppointment)
```

**iOS required data flow:**
```swift
CatalogViewModel.load() async
  → async let services    = CatalogRepository.getServices()      // GET services?public=true
  → async let categories  = CatalogRepository.getCategories()    // GET service-categories?public=true
  → (s, c) = try await (services, categories)

// Cross-filter: category tap switches to services tab with active filter
CatalogViewModel.filterByCategory(_ category: CatalogServiceCategory)
  → selectedTab = 0                    // switch to Services tab
  → activeCategory = category
  → filteredServices = services.filter { $0.category == category.name }

// Search: real-time filter (no debounce needed for local list)
var displayedServices: [CatalogService] {
    let base = activeCategory == nil ? services : filteredServices
    guard !searchQuery.isEmpty else { return base }
    return base.filter { $0.name.localizedCaseInsensitiveContains(searchQuery) }
}
```

**Critical rules:**
- `CatalogServiceCategoryDto._count` field uses `@Json(name="_count")` on Android — on iOS, use `CodingKeys` mapping `_count → count`.
- Services and categories loaded in parallel.
- Category tap cross-filter: switches the `Picker` to tab 0 (Services) and applies filter — do NOT navigate away.
- The Memberships sub-tab reuses `MembershipsViewModel` — inject separately, do not share with booking flow.

---

### 5.10 FinanceTabScreen

**Status:** ❌ Missing

**Android data flow:**
```
FinanceViewModel.init()
  → GetPaymentsUseCase.invoke()
    → FinanceRepository.getPayments()
      → GET /api/payments          ← ABSOLUTE PATH
      → List<PaymentDto> → List<Payment>

  KPI computation (client-side from payment list):
    collected = payments.filter { it.status == "paid" }.sumOf { it.amount ?: 0 }
    pending   = payments.filter { it.status == "pending" }.sumOf { it.amount ?: 0 }
    failed    = payments.filter { it.status == "failed" }.sumOf { it.amount ?: 0 }

onEvent(RequestRefund(paymentId))
  → after AlertDialog confirm:
    → FinanceRepository.refundPayment(paymentId)
      → POST /api/payments/{id}/refund    ← ABSOLUTE PATH
      → reload payments list
```

**iOS required data flow:**
```swift
FinanceViewModel.load() async
  → GetPaymentsUseCase.invoke()
    → FinanceRepository.getPayments()
      → GET /api/payments     ← absolute URL; bypass /api/wellness/ base
      → [Payment]

// KPI computed property
var kpis: FinanceKPIs {
    FinanceKPIs(
        collected: payments.filter { $0.status == "paid" }.compactMap(\.amount).reduce(0, +),
        pending:   payments.filter { $0.status == "pending" }.compactMap(\.amount).reduce(0, +),
        failed:    payments.filter { $0.status == "failed" }.compactMap(\.amount).reduce(0, +)
    )
}

FinanceViewModel.requestRefund(paymentId: String) async
  → showRefundConfirm = false
  → FinanceRepository.refundPayment(id: paymentId)
    → POST /api/payments/{id}/refund    ← absolute URL
  → await load()
```

**Critical rules:**
- Both `/api/payments` and `/api/payments/{id}/refund` are absolute paths — they bypass `/api/wellness/`.
- KPI calculation is client-side from the payment list — there is no separate summary endpoint.
- `PaymentDto.amount` is `Long?` (all fields optional) — handle nil gracefully.
- Gift Cards and Transactions sub-tabs render `GiftCardsView` and `WalletView` inline — not pushed to navigation stack.

---

### 5.11 PrescriptionsScreen

**Status:** ❌ Missing

**Android data flow:**
```
PrescriptionsViewModel.init()
  → GetPatientPermissionsUseCase.invoke()
    → AuthRepository.getPermissions()
      → GET portal/me/permissions
      → PatientPermissionsDto → PatientPermissions
    → if !permissions.has("my_prescriptions.read"):
        → show "Access restricted" empty state, return

  → GetPrescriptionsUseCase.invoke()
    → PrescriptionRepository.getPrescriptions()
      → GET portal/prescriptions
      → List<PrescriptionDto>
      [CRITICAL: dto.drugs is a JSON-encoded String, not an array]
      → drugs = Moshi.fromJson<List<DrugDto>>(dto.drugs) ← second parse
      → cache in Room (CachedPrescription)
```

**iOS required data flow:**
```swift
PrescriptionsViewModel.load() async
  → let permissions = try await GetPatientPermissionsUseCase.invoke()
  → guard permissions.has("my_prescriptions.read") else {
        viewModel.accessDenied = true; return
    }
  → GetPrescriptionsUseCase.invoke()
    → PrescriptionRepository.getPrescriptions()
      → GET portal/prescriptions
      → [PrescriptionResponse]   // drugs is a String here
      → map: drugs = JSONDecoder().decode([Drug].self, from: dto.drugs.data(using: .utf8)!)
      → CoreData.save(prescriptions)   // cache
      → return [Prescription]    // drugs is [Drug] in domain model
```

**Critical rules:**
- `drugs` field is a JSON-encoded String (e.g., `"[{\"name\":\"Amoxicillin\"}]"`) — NOT a JSON array. Requires a second `JSONDecoder` call inside the mapper. This is a known backend quirk.
- Permission gate: check `my_prescriptions.read` before any API call — show access-denied state if missing.
- PDF confirm alert before downloading — never auto-download.
- PDF bytes cached in CoreData `CachedPrescription.pdfData` — 7-day TTL. Evict on app start.

---

### 5.12 PrescriptionPdfScreen

**Status:** ❌ Missing

**Android data flow:**
```
PrescriptionPdfViewModel.init(prescriptionId)
  → GetPrescriptionPdfUseCase.invoke(prescriptionId)
    → PrescriptionRepository.getPdf(prescriptionId)
      → check Room cache: CachedPrescription.pdfBytes + pdfCachedAt
        → if cached and pdfCachedAt > (now - 7.days): return pdfBytes
      → GET portal/prescriptions/{id}/pdf → ResponseBody (binary)
      → save to Room: CachedPrescription.pdfBytes + pdfCachedAt = now
      → serve via FileProvider ({appId}.wellness_pdfs authority)
```

**iOS required data flow:**
```swift
PrescriptionPdfViewModel.loadPdf(id: Int) async
  → GetPrescriptionPdfUseCase.invoke(prescriptionId: id)
    → PrescriptionRepository.getPdf(id: id)
      → check CoreData: CachedPrescription.pdfData + pdfCachedAt
        → if pdfCachedAt != nil && Date().timeIntervalSince(pdfCachedAt!) < 7*86400 {
              let url = writePdfToDocuments(data: cachedData)
              return url
          }
      → GET portal/prescriptions/{id}/pdf → Data (binary)
      → let url = writePdfToDocuments(data: pdfData, filename: "rx-\(id).pdf")
      → CoreData.update(id: id, pdfData: pdfData, pdfCachedAt: Date())
      → return url

// writePdfToDocuments: write to FileManager.default.urls(.documentDirectory)
// Use UIActivityViewController with excludedActivityTypes: [.saveToCameraRoll]
```

**Critical rules:**
- PDF bytes stored in app's private Documents directory — NOT Photos, NOT shared storage.
- 7-day TTL: check `pdfCachedAt` on load; if stale, re-fetch.
- Render using `PDFKit.PDFView` wrapped in `UIViewRepresentable`.
- Share sheet must exclude `.saveToCameraRoll` — prescription PDFs should never auto-save to the camera roll.

---

### 5.13 TreatmentPlansScreen

**Status:** ❌ Missing

**Android data flow:**
```
TreatmentPlanViewModel.init()
  → GetTreatmentPlansUseCase.invoke()
    → TreatmentPlanRepository.getPlans()
      → val patientId = EncryptedPrefsManager.getPatientId()
        → GET patients/{patientId}/treatment-plans
        → List<TreatmentPlanDto> → List<TreatmentPlan>
```

**iOS required data flow:**
```swift
TreatmentPlansViewModel.load() async
  → let patientId = KeychainManager.getPatientId()
  → GetTreatmentPlansUseCase.invoke(patientId: patientId)
    → TreatmentPlanRepository.getPlans(patientId: patientId)
      → GET patients/{patientId}/treatment-plans
      → [TreatmentPlan]
```

---

### 5.14 ConsentFormsScreen

**Status:** ❌ Missing

**Android data flow:**
```
ConsentFormViewModel.init()
  → GetConsentFormsUseCase.invoke()
    → ConsentFormRepository.getForms()
      → val patientId = EncryptedPrefsManager.getPatientId()
        → GET patients/{patientId}/consents
        → List<ConsentFormDto> → List<ConsentForm>
```

**iOS required data flow:**
```swift
ConsentFormsViewModel.load() async
  → let patientId = KeychainManager.getPatientId()
  → GetConsentFormsUseCase.invoke(patientId: patientId)
    → ConsentFormRepository.getForms(patientId: patientId)
      → GET patients/{patientId}/consents → [ConsentForm]
```

---

### 5.15 ConsentFormPdfScreen

**Status:** ❌ Missing

**Android data flow:**
```
ConsentFormPdfViewModel.init(consentId)
  → GetConsentFormPdfUseCase.invoke(consentId)
    → ConsentFormRepository.getPdf(consentId)
      → GET consents/{id}/pdf → ResponseBody (binary)
```

**iOS required data flow:** Identical pattern to PrescriptionPdfScreen — different endpoint only.

---

### 5.16 MembershipsScreen

**Status:** ❌ Missing

**Android data flow:**
```
MembershipsViewModel.init()
  → async parallel:
    → GetMyMembershipsUseCase.invoke()
      → MembershipRepository.getMyMemberships()
        → GET appointments/my-memberships
        → List<MembershipDto>
        [NOTE: MembershipDto.balance is always [] — backend gap]
        → cache in Room (CachedMembership)
    → GetMembershipPlansUseCase.invoke()
      → MembershipRepository.getMembershipPlans()
        → GET membership-plans
        → List<MembershipPlanDto>
        [NOTE: MembershipPlanDto.entitlements is raw JSON string]
        → parse: entitlements = Moshi.fromJson<List<String>>(dto.entitlements)
```

**iOS required data flow:**
```swift
MembershipsViewModel.load() async
  → async let myMemberships = MembershipRepository.getMyMemberships()  // GET appointments/my-memberships
  → async let plans         = MembershipRepository.getPlans()           // GET membership-plans
  → (m, p) = try await (myMemberships, plans)

  // entitlements parsing
  → availablePlans = p.map { dto in
      let entitlements = (try? JSONDecoder().decode([String].self,
          from: dto.entitlements.data(using: .utf8)!)) ?? []
      return MembershipPlan(..., entitlements: entitlements)
  }
```

**Critical rules:**
- `MembershipDto.balance` is always `[]` — do not surface "balance" UI until backend fixes this.
- `MembershipPlanDto.entitlements` is a raw JSON string — second decode required in mapper.
- My memberships and available plans loaded in parallel.
- Plan tier colours: Diamond = `Color(hex: "#1A237E")`, Gold = `Color(hex: "#FFB300")`, Platinum = `Color(hex: "#37474F")`.
- "Join Now" requires a confirm dialog (`alert`) before calling any API.

---

### 5.17 WalletScreen

**Status:** ❌ Missing

**Android data flow:**
```
WalletViewModel.init()
  → async parallel:
    → GetPatientWalletUseCase.invoke()
      → WalletRepository.getWallet()
        → val patientId = EncryptedPrefsManager.getPatientId()
          → GET patients/{patientId}/wallet
          → PatientWalletResponseDto → PatientWallet
    → GetMyTransactionsUseCase.invoke()
      → WalletRepository.getTransactions()
        → GET my-transactions
        → MyTransactionsResponseDto → List<Transaction>

  filter logic (client-side):
    "All"         → all transactions
    "Wallet"      → transactions.filter { it.category == "wallet" }
    "GiftCards"   → transactions.filter { it.category == "gift_card" }
    "Memberships" → transactions.filter { it.category == "membership" }
    "Treatments"  → transactions.filter { it.category == "treatment" }

  Transaction.direction: "credit" → green amount, "debit" → red amount
  Transaction.amount: Double (already in rupees — NOT paise)
```

**iOS required data flow:**
```swift
WalletViewModel.load() async
  → let patientId = KeychainManager.getPatientId()
  → async let wallet       = WalletRepository.getWallet(patientId: patientId) // GET patients/{patientId}/wallet
  → async let transactions = WalletRepository.getTransactions()               // GET my-transactions
  → (w, t) = try await (wallet, transactions)

var filteredTransactions: [Transaction] {
    switch activeFilter {
    case .all:         return transactions
    case .wallet:      return transactions.filter { $0.category == "wallet" }
    case .giftCards:   return transactions.filter { $0.category == "gift_card" }
    case .memberships: return transactions.filter { $0.category == "membership" }
    case .treatments:  return transactions.filter { $0.category == "treatment" }
    }
}
```

**Critical rules:**
- `Transaction.amount` is already in rupees (Double) — do NOT divide by 100. The Android codebase had a bug where amounts were divided by 100; this was fixed.
- `direction: "credit"` → green text, `direction: "debit"` → red text.
- Wallet balance and transaction summary loaded in parallel.
- KPI row wraps to 2×2 grid on narrow screens — use `LazyVGrid` with 2 flexible columns.

---

### 5.18 GiftCardsScreen

**Status:** ❌ Missing

**Android data flow:**
```
GiftCardsViewModel.init()
  → GetGiftCardStorefrontUseCase.invoke()
    → WalletRepository.getStorefront()
      → GET giftcards/storefront
      → GiftCardStorefrontResponseDto → List<GiftCard>
  [Current build overrides with DEMO_DENOMINATIONS — ₹500/1000/2000/5000/10000]

onEvent(PurchaseGiftCard(card))
  → Step 1: InitiateGiftCardPurchaseUseCase.invoke(giftCardId, patientId)
    → WalletRepository.initiateOrder(
        giftCardId, GiftCardOrderDto(patientId = EncryptedPrefsManager.getPatientId()))
      → POST giftcards/{id}/purchase/order
      → GiftCardOrderResponseDto:
          { orderId, paymentId, key (Razorpay key), amount (paise), currency, ... }
      [amount is in PAISE — divide by 100 for display, pass raw to Razorpay]

  → Step 2: Launch Razorpay SDK
      val options = JSONObject().apply {
          put("name", clinicName)
          put("description", card.name)
          put("order_id", orderId)
          put("amount", amount)           ← in paise
          put("currency", currency)
          put("key", razorpayKey)
      }
      Checkout().open(activity, options)

  → Step 3 (Razorpay callback): onPaymentSuccess(paymentId)
    → ConfirmGiftCardPurchaseUseCase.invoke(...)
      → WalletRepository.confirmPurchase(
          giftCardId, GiftCardConfirmDto(
            paymentId = paymentId,
            razorpay_order_id = ...,
            razorpay_payment_id = ...,
            razorpay_signature = ...))
        → POST giftcards/{id}/purchase/confirm
        → GiftCardConfirmResponseDto → success state
```

**iOS required data flow:**
```swift
GiftCardsViewModel.purchase(card: GiftCard) async
  → let patientId = KeychainManager.getPatientId()

  // Step 1: Create order
  → let order = try await GiftCardRepository.initiateOrder(
      giftCardId: card.id,
      request: GiftCardOrderRequest(patientId: patientId))
    → POST giftcards/{card.id}/purchase/order
    → GiftCardOrderResponse: { orderId, key, amount (paise), currency, ... }

  // Step 2: Launch Razorpay
  → let options: [String: Any] = [
      "name": appState.clinicName,
      "description": card.name,
      "order_id": order.orderId,
      "amount": order.amount,           // paise — pass as-is to Razorpay
      "currency": order.currency,
      "key": order.key
  ]
  → RazorpayController.open(options: options, delegate: self)

  // Step 3: Razorpay delegate callback
  → func onPaymentSuccess(paymentId: String, andData response: [AnyHashable: Any])
    → let confirmRequest = GiftCardConfirmRequest(
          paymentId: paymentId,
          razorpayOrderId:   response["razorpay_order_id"],
          razorpayPaymentId: response["razorpay_payment_id"],
          razorpaySignature: response["razorpay_signature"])
    → let result = try await GiftCardRepository.confirmPurchase(
          giftCardId: card.id, request: confirmRequest)
      → POST giftcards/{card.id}/purchase/confirm
    → viewModel.purchaseSuccess = result
```

**Critical rules:**
- `GiftCardOrderResponseDto.amount` is in **paise** (1/100 rupee) — pass raw to Razorpay SDK; divide by 100 only for display.
- Razorpay iOS SDK requires a `UIViewController` — wrap in `UIViewControllerRepresentable`.
- Razorpay delegate: implement `RazorpayPaymentCompletionProtocol` — `onPaymentSuccess` and `onPaymentError`.
- Confirm call is mandatory after Razorpay success — the gift card is only credited after server verification.
- On payment failure/cancellation: show error, do not call confirm.

---

### 5.19 LoyaltyScreen

**Status:** ❌ Missing

**Android data flow:**
```
LoyaltyViewModel.init()
  → GetLoyaltyUseCase.invoke()
    → LoyaltyRepository.getLoyalty()
      → val patientId = EncryptedPrefsManager.getPatientId()
        → GET loyalty/{patientId}       ← backend does NOT verify ownership
        → LoyaltyResponseDto → LoyaltyData
```

**iOS required data flow:**
```swift
LoyaltyViewModel.load() async
  → let patientId = KeychainManager.getPatientId()  // 🔑 ONLY from Keychain
  → GetLoyaltyUseCase.invoke(patientId: patientId)
    → LoyaltyRepository.getLoyalty(patientId: patientId)
      → GET loyalty/{patientId}
      → LoyaltyData
```

**Critical rules:**
- patientId must come from Keychain only. Backend does not verify ownership — if a user could pass an arbitrary patientId they could read another patient's loyalty data.
- Use `ShareLink(item:)` for referral code sharing — native iOS share sheet.

---

### 5.20 ProfileScreen

**Status:** ❌ Missing

**Android data flow:**
```
ProfileViewModel.init()
  → async parallel:
    → GetProfileUseCase.invoke()
      → ProfileRepository.getPatientProfile()
        → GET portal/me               ← patient-layer
        → ProfileDto { id, name, phone, email, dob?, gender? }
    → GetAuthProfileUseCase.invoke()
      → ProfileRepository.getAuthProfile()
        → GET /api/auth/me            ← user-layer (absolute path)
        → AuthProfileResponseDto { id, name, email, role, profilePicture? }

onEvent(UpdateProfile(name, email))
  → UpdateAuthProfileUseCase.invoke(name, email, null, null)
    → ProfileRepository.updateAuthProfile(
        UpdateAuthProfileDto(name = name, email = email))
      → PUT /api/auth/me              ← absolute path
      → AuthProfileResponseDto

onEvent(ChangePassword(current, new))
  → UpdateAuthProfileUseCase.invoke(null, null, current, new)
    → PUT /api/auth/me with currentPassword + newPassword

onEvent(UploadProfilePicture(bytes))
  → UploadProfilePictureUseCase.invoke(bytes, mimeType)
    → ProfileRepository.uploadProfilePicture(MultipartBody.Part)
      → POST /api/auth/me/profile-picture   ← multipart, absolute path

onEvent(Logout)
  → LogoutUseCase.invoke()
    → TokenManager.clearToken()         [DataStore]
    → EncryptedPrefsManager.clearAll()
    → AppDatabase.clearAll()            [Room — all 4 tables]
    → FcmHelper.deregisterToken()       [DELETE portal/me/fcm-token]
    → Channel.send(NavEvent.NavigateToLogin) [popUpTo(0)]
```

**iOS required data flow:**
```swift
ProfileViewModel.load() async
  → async let patientProfile = ProfileRepository.getPatientProfile() // GET portal/me
  → async let authProfile    = ProfileRepository.getAuthProfile()    // GET /api/auth/me (absolute)
  → (patient, auth) = try await (patientProfile, authProfile)
  → viewModel.mergedProfile = MergedProfile(patient: patient, auth: auth)

ProfileViewModel.updateProfile(name: String, email: String) async
  → UpdateAuthProfileUseCase.invoke(name: name, email: email)
    → PUT /api/auth/me (absolute)

ProfileViewModel.changePassword(current: String, new: String, confirm: String) async
  → guard new == confirm else { error = "Passwords do not match"; return }
  → guard new.count >= 8 else { error = "Password too short"; return }
  → UpdateAuthProfileUseCase.invoke(currentPassword: current, newPassword: new)
    → PUT /api/auth/me (absolute)

ProfileViewModel.uploadPhoto(data: Data) async
  → ProfileRepository.uploadProfilePicture(data: data, mimeType: "image/jpeg")
    → POST /api/auth/me/profile-picture (multipart, absolute)

ProfileViewModel.logout() async
  → FcmTokenRepository.deregister()          // DELETE portal/me/fcm-token
  → KeychainManager.clearAll()
  → CoreDataStack.deleteAll()
  → AppRouter.route = .auth(.login)
```

**Critical rules:**
- Profile data comes from TWO different APIs — patient-layer (`GET portal/me`) and user-layer (`GET /api/auth/me`). They must be merged into a single domain model.
- **Editable fields:** name, email, currentPassword, newPassword — from `PUT /api/auth/me`.
- **Non-editable fields:** phone, dob, gender — from `GET portal/me`. These are display-only (no backend endpoint to update them).
- Logout must: clear Keychain + CoreData + attempt FCM deregister + replace navigation root with Login screen.
- Profile picture upload: use `PhotosPicker` (SwiftUI, iOS 16+) — no UIKit needed.
- Change password: local validation first (`new == confirm`, length check) before API call.

---

### 5.21 NotificationInboxScreen

**Status:** ❌ Missing

**Android data flow:**
```
NotificationInboxViewModel.init()
  → notificationDao.getAllNotificationsAsFlow()   ← Room Flow, NO network call
    → StateFlow<List<CachedNotification>>
  → sorted by receivedAt desc

onTapNotification(notification)
  → notificationDao.markAsRead(notification.id)
  → if notification.screen != null:
      → DeepLinkHandler.navigate(navController, notification.screen, notification.entityId)

onMarkAllRead()
  → notificationDao.markAllAsRead()
```

**iOS required data flow:**
```swift
NotificationInboxViewModel.init()
  → @Published notifications: [CachedNotification] = []
  → fetchResultsController.delegate = self    // NSFetchedResultsController on CachedNotification
  → sort: receivedAt descending

NotificationInboxViewModel.tap(_ notification: CachedNotification)
  → CoreData.update(notification.id, isRead: true)
  → if let screen = notification.screen {
      AppRouter.deepLink(to: screen, entityId: notification.entityId)
  }

NotificationInboxViewModel.markAllRead()
  → CoreData.updateAll(isRead: true)
```

**Critical rules:**
- Data source is CoreData ONLY — no network call on this screen.
- Notifications are written by `UNUserNotificationCenterDelegate` on FCM receipt.
- 90-day TTL: run `CoreData.deleteOlderThan(Date().addingTimeInterval(-90*86400))` on app start.
- Badge count: set via `UNUserNotificationCenter.current().setBadgeCount(unreadCount)` (iOS 16+).

---

### 5.22 NotificationSettingsScreen

**Status:** ❌ Missing

**Android data flow:**
```
NotificationSettingsViewModel.init()
  → DataStoreManager.getNotificationSettings()
    → local persistence only — 5 category flags + 3 channel flags + quiet hours

onSave()
  → DataStoreManager.saveNotificationSettings(settings)
  → no API call — local preferences only
```

**iOS required data flow:**
```swift
NotificationSettingsViewModel.load()
  → load from UserDefaults (or AppStorage @AppStorage wrappers)
  // 5 categories: appointmentReminders, healthUpdates, walletPayments, offersSurveys, treatmentUpdates
  // 3 channels: pushEnabled, smsEnabled (disabled if !smsAvailable), emailEnabled
  // Quiet hours: quietHoursEnabled, quietStart: Date, quietEnd: Date

NotificationSettingsViewModel.save()
  → write all flags to UserDefaults
  → if !pushEnabled: UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
```

**Critical rules:**
- No API call — settings are local only.
- SMS channel toggle: disabled (grayed out) if `smsUnavailable == true` (from portal health check).
- Quiet hours: times stored as `Date` components (hour + minute) — not full Date objects.

---

## 6. Business Logic Checklist

### Auth & Session

| Rule | Android implementation | iOS requirement | Status |
|------|----------------------|-----------------|--------|
| JWT in secure storage | DataStore (file-encrypted) | Keychain (`kSecClassGenericPassword`) | ❌ |
| Patient name/phone in encrypted storage | `EncryptedSharedPreferences` (AES-256-GCM) | Keychain | ❌ |
| Global 401 handling | NOT implemented — each screen independent | URLProtocol/URLSession interceptor — clears Keychain + routes to Login | ❌ |
| Token passed as `Authorization: Bearer {jwt}` | `AuthInterceptor` (OkHttp) | `URLRequest` header in `URLSession` delegate | ❌ |
| Logout clears all storage | DataStore + EncryptedPrefs + Room + FCM | Keychain + CoreData + FCM deregister | ❌ |
| FCM token registration on login | `FcmHelper.registerToken()` | `FcmTokenService.register()` — fire-and-forget | ❌ |

### Appointment Booking

| Rule | Android implementation | iOS requirement | Status |
|------|----------------------|-----------------|--------|
| Date format = `YYYY-MM-DD` | `DateUtil.toApiDate(millis)` | `DateFormatter(format: "yyyy-MM-dd")` | ❌ |
| Time format = `HH:mm` | String directly from TimePicker | `DateFormatter(format: "HH:mm")` | ❌ |
| "No preference" doctor option | Prepended `DoctorOption(id=null, name="No preference")` | `.id = nil` prepended to doctor list | ❌ |
| Doctor availability loads on date change | `LaunchedEffect(selectedDate)` | `.onChange(of: selectedDate)` | ❌ |

### Prescriptions

| Rule | Android implementation | iOS requirement | Status |
|------|----------------------|-----------------|--------|
| `drugs` is double-decoded | Moshi parses `PrescriptionDto.drugs: String` as JSON | `JSONDecoder().decode([Drug].self, from: dto.drugs.data(using: .utf8)!)` | ❌ |
| Permission gate | `PatientPermissions.has("my_prescriptions.read")` | Same check before API call | ❌ |
| PDF 7-day TTL | `pdfCachedAt` Room field | CoreData `pdfCachedAt: Date?` | ❌ |
| PDF confirm dialog | AlertDialog before download | `.alert("View PDF?", isPresented:)` | ❌ |

### Waitlist

| Rule | Android implementation | iOS requirement | Status |
|------|----------------------|-----------------|--------|
| `patientId` mandatory in POST | `AddWaitlistDto.patientId` from EncryptedPrefsManager | From Keychain — never from form | ❌ |
| Endpoint = `waitlist` not `portal/waitlist` | Fixed after 404 bug | `waitlist` — no portal/ prefix | ❌ |

### Finance & Wallet

| Rule | Android implementation | iOS requirement | Status |
|------|----------------------|-----------------|--------|
| Transaction amount in rupees (not paise) | `TransactionDto.amount: Double` | Do NOT divide by 100 | ❌ |
| Gift card order amount in paise | `GiftCardOrderResponseDto.amount` paise | Pass raw to Razorpay; ÷100 for display | ❌ |
| KPI calculation client-side | Computed from payment list | Same — no summary endpoint | ❌ |

### Memberships

| Rule | Android implementation | iOS requirement | Status |
|------|----------------------|-----------------|--------|
| `entitlements` double-decoded | Moshi parses `MembershipPlanDto.entitlements: String` as JSON | `JSONDecoder().decode([String].self, ...)` in mapper | ❌ |
| `balance` always `[]` | Known backend gap — not surfaced in UI | Do not show balance UI | ❌ |

### Loyalty

| Rule | Android implementation | iOS requirement | Status |
|------|----------------------|-----------------|--------|
| patientId from EncryptedPrefsManager only | `EncryptedPrefsManager.getPatientId()` | Keychain only — never user input | ❌ |

### Profile

| Rule | Android implementation | iOS requirement | Status |
|------|----------------------|-----------------|--------|
| Dual-API merge | GET portal/me + GET /api/auth/me | Both calls, merged domain model | ❌ |
| Editable: name, email, password | PUT /api/auth/me | Same endpoint (absolute path) | ❌ |
| Non-editable: phone, dob, gender | Display-only from portal/me | Display-only — no edit UI | ❌ |
| DSAR export | POST portal/export | Same | ❌ |

---

## 7. Realtime & Push Systems Audit

### 7.1 Socket.IO (WellnessSocketManager)

**Android implementation:** `WellnessSocketManager.kt` — Socket.IO client connected after login.

| Aspect | Android | iOS status |
|--------|---------|------------|
| Socket connection | `socket.connect()` on login | ❌ Missing |
| Auth header | JWT passed in socket options | ❌ Missing |
| Disconnect on logout | `socket.disconnect()` | ❌ Missing |
| Event listeners | `on("notification")` → save to Room | ❌ Missing |
| Reconnection | Handled by Socket.IO client library | ❌ Missing |

**iOS requirement:** Use [socket.io-client-swift](https://github.com/socketio/socket.io-client-swift) (SPM). Connect with JWT in `extraHeaders`. On `"notification"` event: parse payload, write to CoreData `CachedNotification`, post `UNUserNotificationCenter` local notification if app is in background.

### 7.2 FCM Push Notifications

| Aspect | Android | iOS status |
|--------|---------|------------|
| FCM SDK | `firebase-messaging` | ❌ Missing — add Firebase iOS SDK |
| Token registration | `POST portal/me/fcm-token` (silently fails — backend is WebPush) | ❌ Missing — same endpoint; accept silent failure |
| Token deregistration | `DELETE portal/me/fcm-token` on logout | ❌ Missing |
| Notification channels | 4 channels in `NotificationManager` | ❌ Missing — map to 4 UNNotificationCategory |
| `onMessageReceived` | Persist to Room + show system notification | ❌ Missing — `UNUserNotificationCenterDelegate.didReceiveRemoteNotification` |

**iOS notification channels → UNNotificationCategory mapping:**

| Android Channel | Channel ID | iOS UNNotificationCategory | Importance → iOS options |
|----------------|-----------|---------------------------|--------------------------|
| Appointment Reminders | `wellness_reminders` | `wellness.reminders` | HIGH + vibration → `.alert + .sound + .badge` |
| Health Updates | `wellness_health` | `wellness.health` | DEFAULT → `.alert + .sound` |
| Wallet & Payments | `wellness_wallet` | `wellness.wallet` | DEFAULT → `.alert + .sound` |
| Offers & Surveys | `wellness_offers` | `wellness.offers` | LOW → `.badge` only |

**FCM type → deep link mapping:**

| `type` field | iOS deep link action |
|-------------|---------------------|
| `APPOINTMENT_REMINDER_24H` / `APPOINTMENT_REMINDER_1H` | `AppRouter.deepLink(to: "appointments")` |
| `BOOKING_CONFIRMED` / `BOOKING_CANCELLED` | `AppRouter.deepLink(to: "appointments")` |
| `PRESCRIPTION_READY` | `AppRouter.deepLink(to: "prescriptions")` |
| `MEMBERSHIP_EXPIRY` | `AppRouter.deepLink(to: "memberships")` |
| `WALLET_CREDITED` | `AppRouter.deepLink(to: "wallet")` |
| `NPS_SURVEY` | `UIApplication.shared.open(externalUrl)` |
| `NO_SHOW_REENGAGEMENT` | `AppRouter.deepLink(to: "book")` |

### 7.3 Background Badge Count

**Android:** `MainViewModel.unreadNotificationCount = notificationDao.getUnreadCountAsFlow()`  
**iOS:** `NSFetchedResultsController` on `CachedNotification` where `isRead == false` → count → `UNUserNotificationCenter.current().setBadgeCount(count)` (iOS 16+)

---

## 8. Navigation & Deep Link Audit

### 8.1 Navigation Parity

| Android destination | iOS equivalent | Status |
|--------------------|---------------|--------|
| 5-tab BottomNav | `TabView(selection:)` with 5 tabs | ❌ Missing |
| `WellnessTopAppBar` | Custom toolbar overlay above TabView | ❌ Missing |
| Back arrow on sub-screens | `NavigationStack` native back | ❌ Missing |
| `popUpTo(0)` on login/logout | `AppRouter.route = .auth` / `.main` (replaces root) | ❌ Missing |
| Tab back-stack preservation | Per-tab `NavigationPath` in `TabNavigator` | ❌ Missing |
| Tab root back suppression | TabView handles natively | ✅ Automatic |

### 8.2 Deep Link Parity

**Scheme:** `wellnesspatient://screen/{name}?id={entityId}`

| Screen name | Target | Android | iOS status |
|------------|--------|---------|------------|
| `appointments` | MyAppointmentsScreen | ✅ | ❌ Missing |
| `prescriptions` | PrescriptionsScreen | ✅ | ❌ Missing |
| `memberships` | MembershipsScreen | ✅ | ❌ Missing |
| `wallet` | WalletScreen | ✅ | ❌ Missing |
| `book` | BookAppointmentScreen | ✅ | ❌ Missing |
| `waitlist` | WaitlistScreen | ✅ | ❌ Missing |
| `notifications` | NotificationInboxScreen | ✅ | ❌ Missing |
| `loyalty` | LoyaltyScreen | ✅ | ❌ Missing |
| `gift_cards` | GiftCardsScreen | ✅ | ❌ Missing |

**iOS Info.plist URL scheme registration:**
```xml
<key>CFBundleURLTypes</key>
<array>
  <dict>
    <key>CFBundleURLSchemes</key>
    <array><string>wellnesspatient</string></array>
  </dict>
</array>
```

---

## 9. Security & Storage Audit

| Security concern | Android | iOS requirement | Status |
|-----------------|---------|----------------|--------|
| JWT storage | DataStore (file-encrypted) | Keychain `kSecClassGenericPassword`, `kSecAttrAccessibleAfterFirstUnlock` | ❌ Missing |
| Patient name + phone storage | EncryptedSharedPreferences AES-256-GCM | Keychain (same item group as JWT) | ❌ Missing |
| patientId storage | EncryptedSharedPreferences | Keychain | ❌ Missing |
| PHI in logs | patientId (int) only — never name/phone | `os_log` with `%{private}` — no PHI in any log call | ❌ Missing |
| PHI in crash reports | Sentry config strips PII | Sentry iOS SDK — same config | ❌ Missing |
| Prescription PDF storage | Room BLOB, FileProvider | Documents directory, exclude from backup | ❌ Missing |
| PDF backup exclusion | Room / FileProvider handles | `URLResourceValues.isExcludedFromBackup = true` | ❌ Missing |
| Cert pinning | `network_security_config.xml` | `URLSession` challenge handler — pin SHA256 of production cert | ❌ Missing |
| Global 401 | NOT in Android — fix in iOS | `URLProtocol` subclass or `URLSession.delegate` | ❌ Missing |
| Loyalty patientId | EncryptedPrefsManager only | Keychain only — never from URL params or user input | ❌ Missing |
| Razorpay key storage | `GiftCardOrderResponseDto.key` from server | Same — key comes from API response, never hardcoded | ❌ Missing |
| POST_NOTIFICATIONS | Requested at runtime (Android 13+) | `UNUserNotificationCenter.requestAuthorization(options:)` | ❌ Missing |

---

## 10. Implementation Priority Order

### P0 — Foundation (nothing works without these)

| Task | Depends on | Est. |
|------|-----------|------|
| `AppConfig.swift` — tenant slug, base URLs, build env | — | 0.5d |
| `AppError.swift` — sealed error enum | — | 0.5d |
| `KeychainManager.swift` — JWT + PII read/write/clear | — | 1d |
| `WellnessAPIClient.swift` — URLSession, base URL, auth header, 401 handler | KeychainManager | 2d |
| `AppRouter.swift` — auth state, tab selection, deep link routing | — | 1d |
| `WellnessTheme.swift` — colours, typography, shapes, brand override | — | 1d |
| `CoreDataStack.swift` — 4 entities, migrations | — | 1d |
| `SplashView` + `SplashViewModel` | All above | 1d |
| `LoginView` + `LoginViewModel` + `LoginUseCase` + `AuthRepository` | APIClient, Keychain | 1.5d |
| `RegisterView` + `RegisterViewModel` + `RegisterUseCase` | AuthRepository | 1d |
| **P0 total** | | **~10.5d** |

### P1 — Core patient flows

| Task | Depends on | Est. |
|------|-----------|------|
| `DashboardView` + 3-API parallel load | P0 | 3d |
| `BookAppointmentView` (4-step wizard) | P0 | 5d |
| `MyAppointmentsView` (4-bucket + sheets) | P0 | 3.5d |
| `CatalogView` (3-tab + service detail sheet) | P0 | 3.5d |
| `PrescriptionsView` + permission gate + PDF | P0 | 2.5d |
| `ProfileView` (dual API + photo + password) | P0 | 4d |
| Reusable Components (`WellnessCard`, `StatusChip`, `EmptyState`, `ErrorState`, `PrimaryButton`) | WellnessTheme | 2d |
| Global chrome (`WellnessTopBar`, `TabBar`) | AppRouter | 2d |
| **P1 total** | | **~25.5d** |

### P2 — Secondary flows

| Task | Est. |
|------|------|
| `VisitHistoryView` + detail sheet + Room cache | 2d |
| `WaitlistView` + add sheet + FAB | 2d |
| `FinanceView` (3-tab + inline sub-screens) | 3d |
| `MembershipsView` (toggle + plan cards + detail sheet) | 3d |
| `WalletView` (4-KPI grid + filter chips + receipt sheet) | 3d |
| `GiftCardsView` + Razorpay iOS SDK | 4d |
| `TreatmentPlansView` | 1d |
| `ConsentFormsView` | 1d |
| `PrescriptionPdfView` (PDFKit) | 1.5d |
| `NotificationInboxView` (CoreData + deep link) | 1.5d |
| `NotificationSettingsView` (Form + UserDefaults) | 1.5d |
| FCM + APNs + Socket.IO + badge count | 3d |
| Deep link handler (URL scheme routing) | 1d |
| **P2 total** | | **~27.5d** |

### P3 — Polish & platform

| Task | Est. |
|------|------|
| `LoyaltyView` | 1.5d |
| `ConsentFormPdfView` | 0.5d |
| Cert pinning (production) | 1d |
| Accessibility (VoiceOver labels, Dynamic Type) | 2d |
| Dark mode verification pass | 1d |
| iPad layout pass | 2d |
| QA pass (all screens) | 5d |
| **P3 total** | | **~13d** |

### Total estimated effort

| Phase | Est. |
|-------|------|
| P0 — Foundation | ~10.5d |
| P1 — Core patient flows | ~25.5d |
| P2 — Secondary flows | ~27.5d |
| P3 — Polish & platform | ~13d |
| **Grand total** | **~76.5 developer-days** |

---

## Appendix — Known Android Bugs Fixed (do not replicate in iOS)

| Bug | Android fix | iOS note |
|-----|------------|---------|
| `appointmentDate` sent as `"YYYY-MM-DDT00:00:00Z"` (ISO8601 with time) | Fixed to `"YYYY-MM-DD"` | Use `DateFormatter` with `"yyyy-MM-dd"` format |
| `portal/waitlist` 404s | Fixed to `waitlist` | Use `waitlist` — no portal/ prefix |
| `portal/products` 403 for CUSTOMER role | Fixed to `services?public=true` | Use `services?public=true` |
| `ProductDto.category` was Object type | Fixed to String | Decode as `String` |
| `AddWaitlistDto` missing `patientId` | Backend returns 400 without it | Always include `patientId` from Keychain |
| Transaction amounts divided by 100 | Fixed — amounts already in rupees | `TransactionDto.amount: Double` is already rupees |
| No global 401 handler | Not fixed in Android | **Fix from day 1 in iOS** |

---

*End of iOS Gap Audit Report*  
*Android source: Kotlin + Jetpack Compose, Build SUCCESSFUL (2026-06-09)*  
*iOS baseline: 0% — full implementation required*  
*Priority order: P0 (Foundation) → P1 (Core flows) → P2 (Secondary) → P3 (Polish)*
