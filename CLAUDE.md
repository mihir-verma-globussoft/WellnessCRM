# WellnessCRM Patient App — Claude Session Context

> Place this file at the **root of the Android repo** as `CLAUDE.md`.
> Claude reads it automatically at the start of every session.

---

## What This Project Is

A **white-label Android application** (Kotlin + Jetpack Compose) that serves as the patient self-service portal for clinics running WellnessCRM — a multi-tenant clinic management SaaS built by Globussoft Technologies. Patients use this app to book appointments, view prescriptions, manage their wallet and memberships, receive push notifications, and track their clinical journey.

This app is a **separate Android repo** consuming the WellnessCRM backend (`/api/wellness`). The backend lives at `https://github.com/Globussoft-Technologies/globussoft-crm`. The full specifications live in that repo under `docs/`:
- `docs/PATIENT_APP_PRD.md` — all screens, user flows, API contracts, backend gap spec
- `docs/PATIENT_APP_ARCHITECTURE.md` — package structure, code patterns, DI, Room, Navigation
- `docs/WELLNESS_CRM_ANALYSIS.md` — full CRM system analysis for context

---

## App Identity

| Field | Value |
|-------|-------|
| Package name | `com.crm.enhance_wellness` |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Min SDK | 26 (Android 8.0) · compileSdk/targetSdk 36 |
| Architecture | Feature-based Clean Architecture (MVVM per feature) |
| DI | Hilt |
| HTTP | Retrofit 2 + OkHttp 4 |
| Local DB | Room |
| Auth | Email + password → 7-day JWT (`POST /api/auth/login`) |
| Payments | Razorpay Android SDK |
| Push | Socket.IO (foreground only) — **not yet functional, see Real-time Notifications** |

---

## Backend Connection

| Environment | Base URL |
|-------------|----------|
| Production | `https://crm.globusdemos.com/api/wellness/` |
| Local emulator (AVD) | `http://10.0.2.2:5000/api/wellness/` |
| Local device (USB) | `http://192.168.x.x:5000/api/wellness/` |

**Auth header:** `Authorization: Bearer <portal_jwt>`
**All error responses:** `{ "error": "message", "code": "OPTIONAL_CODE" }`

### Auth is email + password, not phone OTP

The phone-OTP flow described in older revisions of this file was replaced in session 4.
`PhoneEntry` and `OtpVerify` no longer exist; `LoginScreen` is the single entry point.

```
POST /api/auth/login   { "email": "...", "password": "..." }
  → { "token": "<jwt>", "user": { id, email, name, userType }, "tenant": { id, name, slug, brandColor, logoUrl } }
```

Two things about this response matter:

- **`userType` must be `CUSTOMER`.** The whole `portal/*` surface sits behind
  `verifyPatientToken`, which resolves a Patient row from the user. A STAFF or ADMIN login
  returns `403 NO_PATIENT_PROFILE` on every portal call and looks like "forbidden
  everywhere". Non-portal endpoints still return 200, which makes the symptom confusing.
- **`tenant` is authoritative.** It is the clinic the account belongs to, which is often
  *not* the clinic `BuildConfig.TENANT_SLUG` names. `AuthRepositoryImpl` writes it to
  DataStore on login, and Splash deliberately does not overwrite it for a signed-in user.

The JWT carries `{ userId, tenantId, userType, role }` and lasts **7 days**. `patientId`
(the Patient row id, ≠ `userId`) comes from `GET /portal/me` right after login and is
cached in `EncryptedPrefsManager` — several endpoints are keyed on it.

---

## Architecture Rules (enforce these in every file you touch)

1. **Feature-first package structure** — code lives in `feature/<name>/data|domain|presentation`, never in a global `data/` or `presentation/` folder
2. **Features never import from each other** — cross-feature navigation only through `core/navigation/Screen`
3. **No business logic in ViewModels or Composables** — all logic lives in UseCases
4. **Repositories own data-source decisions** — remote vs. cache is transparent to UseCases
5. **No Android imports in `domain/`** — pure Kotlin; fully unit-testable without instrumentation
6. **Composables are stateless** — accept `UiState` + `onEvent` lambda only; no ViewModel reference inside sub-composables
7. **One-shot navigation events via `Channel<NavEvent>`** — never navigate from inside a Composable; consume navigation events in NavGraph
8. **`Result<T>` is the universal return type** — every UseCase returns `Result.Success`, `Result.Error`, or `Result.Loading`

---

## Package Structure

```
com.crm.enhance_wellness/
├── app/                            ← Application class (Hilt), MainActivity
├── core/
│   ├── network/                    ← WellnessApiService, AuthInterceptor, TokenManager
│   ├── storage/                    ← DataStoreManager, EncryptedPrefsManager
│   ├── navigation/                 ← Screen sealed class, NavGraph, DeepLinkHandler
│   ├── theme/                      ← WellnessTheme, Color, Typography, Shape
│   ├── util/                       ← Result<T>, DateUtil, CurrencyUtil, PhoneUtil
│   ├── websocket/                  ← WellnessSocketManager (see Real-time Notifications)
│   ├── ui/                         ← WellnessCard, BackendImage, shared components
│   └── di/                         ← AppModule, NetworkModule, DatabaseModule, RepositoryModule
└── feature/
    ├── auth/                       ← Splash, PhoneEntry, OtpVerify, Register
    ├── dashboard/                  ← Home Dashboard
    ├── booking/                    ← BookAppointment (4-step), MyAppointments, VisitHistory
    ├── health/                     ← Prescriptions, TreatmentPlans*, ConsentForms*
    ├── membership/                 ← MyMemberships, plan browse
    ├── wallet/                     ← Wallet, GiftCards (Razorpay)
    ├── loyalty/                    ← Loyalty & Referrals*
    ├── profile/                    ← Profile, avatar upload, DSAR export, delete account
    ├── notifications/              ← Notification Inbox + device notification preferences
    ├── catalog/                    ← Services / Categories / Memberships browse tab
    ├── finance/                    ← Payments / Gift Cards / Transactions tab host
    └── treatmentanalysis/          ← CameraX before/after capture tied to a prescription
```
All features listed above are implemented. `catalog`, `finance` and `treatmentanalysis`
postdate the original 17-screen plan.

### Inside every feature

```
feature/<name>/
├── data/
│   ├── remote/dto/                 ← Moshi @JsonClass DTOs
│   ├── local/entity/               ← Room @Entity
│   ├── local/dao/                  ← Room @Dao
│   ├── mapper/                     ← Dto.toDomain(), Entity.toDomain(), Domain.toEntity()
│   └── repository/                 ← implements domain interface
├── domain/
│   ├── model/                      ← pure Kotlin data classes
│   ├── repository/                 ← interface
│   └── usecase/                    ← one class, one suspend invoke()
└── presentation/
    ├── screen/                     ← @Composable screens
    ├── viewmodel/                  ← @HiltViewModel
    └── state/                      ← UiState data class, UiEvent sealed class
```

---

## Core Code Patterns (use these consistently)

### Result<T>
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val code: String, val message: String, val httpStatus: Int? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### UiState + UiEvent per feature
```kotlin
data class <Feature>UiState(val isLoading: Boolean = true, val error: String? = null, ...)
sealed class <Feature>UiEvent { ... }
```

### ViewModel structure
```kotlin
@HiltViewModel
class <Feature>ViewModel @Inject constructor(private val useCase: <Action>UseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(<Feature>UiState())
    val uiState: StateFlow<<Feature>UiState> = _uiState.asStateFlow()
    private val _navigationEvent = Channel<NavEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onEvent(event: <Feature>UiEvent) { ... }
}
```

### UseCase structure
```kotlin
class <Action>UseCase @Inject constructor(private val repository: <Feature>Repository) {
    suspend operator fun invoke(): Result<T> = try {
        Result.Success(repository.getData())
    } catch (e: HttpException) {
        if (e.code() == 401) Result.Error("UNAUTHORIZED", "Session expired", 401)
        else Result.Error("HTTP_${e.code()}", e.message(), e.code())
    } catch (e: IOException) {
        val cached = repository.getCached()
        if (cached.isNotEmpty()) Result.Success(cached) else Result.Error("NETWORK_ERROR", "No connection")
    }
}
```

### NavGraph wiring pattern
```kotlin
composable(Screen.Feature.route) {
    val vm: FeatureViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.navigationEvent.collect { event -> /* navController.navigate(...) */ } }
    FeatureScreen(state, vm::onEvent)
}
```

### Mapper naming convention
```kotlin
fun FeatureDto.toDomain(): Feature = ...      // remote → domain
fun FeatureEntity.toDomain(): Feature = ...   // cache → domain
fun Feature.toEntity(): FeatureEntity = ...   // domain → cache
// DTOs and Entities never leave the data layer
```

---

## All Screens

23 navigation routes are wired in `core/navigation/NavGraph.kt` — the original 17 plus
Waitlist, Catalog, Finance, Notification Settings, Consent Form PDF, Treatment Analysis and
Register. Every screen below is implemented and reachable from the dashboard hub; nothing is
deep-link-only any more.

| # | Screen | Feature package | Phase | Key API |
|---|--------|----------------|-------|---------|
| 1 | Splash / Branding | auth | 1 | `GET /public/tenant/:slug` |
| 2 | Phone Entry | auth | 1 | `POST /portal/login/request-otp` |
| 3 | OTP Verify | auth | 1 | `POST /portal/login/verify-otp` |
| 4 | Registration | auth | 1 | `POST /portal/register` ★ |
| 5 | Home Dashboard | dashboard | 1 | `GET /portal/me/dashboard` ★ |
| 6 | Book Appointment (4 steps) | booking | 1 | `GET /services`, `GET /locations`, `GET /portal/slots` ★, `POST /appointments/book` |
| 7 | My Appointments | booking | 1 | `GET /portal/visits?upcoming=true`, `POST /appointments/:id/cancel` |
| 8 | Visit History | booking | 1 | `GET /portal/visits` |
| 9 | Prescriptions | health | 1 | `GET /portal/prescriptions`, `GET /portal/prescriptions/:id/pdf` |
| 10 | Treatment Plans | health | 2★ | `GET /portal/me/treatment-plans` ★ |
| 11 | Consent Forms | health | 2★ | `GET /portal/me/consents` ★ |
| 12 | Wallet | wallet | 1 | `GET /portal/me/wallet` ★ |
| 13 | Gift Cards | wallet | 1 | `GET /giftcards/storefront`, `POST /giftcards/:id/purchase/order+confirm` |
| 14 | Loyalty & Referrals | loyalty | 2★ | `GET /portal/me/loyalty` ★ |
| 15 | Profile | profile | 1 | `GET /portal/me`, `PUT /portal/me` ★, `POST /portal/export` |
| 16 | Notification Inbox | notifications | 1 | `GET /portal/me/notifications` + Room cache |
| 17 | My Memberships | membership | 1 | `GET /portal/me/memberships` ★, `GET /membership-plans` |

★ marked new backend endpoints in the original plan. All are now live — see **Backend Gap Endpoints — resolved**.

---

## Backend Gap Endpoints — resolved

The gap list in earlier revisions is obsolete. Every endpoint on it was either built or
replaced by an existing route, and all were verified returning 200 against a live CUSTOMER
account on 2026-08-26. The app's real endpoint list is `core/network/WellnessApiService.kt`.

Two things that were once "gaps" resolved differently than planned:

| Original gap | Reality |
|--------------|---------|
| `GET /portal/me/dashboard` | Never built. The dashboard composes visits + wallet + memberships + loyalty client-side in `DashboardRepositoryImpl`. |
| `POST/DELETE /portal/me/fcm-token` | Never built, and no longer needed — FCM was dropped. See **Real-time Notifications**. |

### Known backend issues

| Issue | Detail |
|-------|--------|
| `GET /loyalty/{patientId}` not ownership-scoped | The backend does not verify the caller owns the id. Only ever call it with `EncryptedPrefsManager.getPatientId()`; never with user input. |
| `products.read` denied to CUSTOMER | `portal/products` and `portal/product-categories` return `403 PORTAL_RBAC_DENIED`. Both were removed from the app; the public `services` / `service-categories` routes carry the same catalogue. |
| No Socket.IO gateway | `/socket.io/` serves the SPA's HTML. See **Real-time Notifications**. |
| No notification-preference endpoint | Preferences are device-local in DataStore. |

---

## Exact API Contracts (key endpoints)

### Auth
```
POST /api/auth/login              (absolute path — not under /api/wellness/)
Body:     { "email": "...", "password": "..." }
Response: { "token": "jwt", "user": { "id", "email", "name", "userType" },
            "tenant": { "id", "name", "slug", "brandColor", "logoUrl" } }
Errors:   401 { "error": "Invalid credentials" }

POST /api/auth/customer/register
Body:     { "email", "password", "name", "registrationTenantId" }
Response: same shape as login
Errors:   409 → ALREADY_REGISTERED | 400 → INVALID_INPUT | 422 → VALIDATION_ERROR
Note:     backend may require email OTP (REQUIRE_EMAIL_OTP=1), which blocks API-only signup.

GET /portal/me
Response: { "id" (patientId), "name", "phone", "email", "dob", "gender" }
```

### Appointments
```
POST /appointments/book
Body:     { "appointmentDate": "ISO8601", "appointmentTime": "10:30", "reason": "string",
            "doctorId": int?, "serviceId": int?, "membershipId": int? }
Response: { "success": true, "appointment": { "id", "patientName", "doctorName",
            "appointmentDate", "status": "booked", "reason" } }
Errors:   400 MISSING_FIELDS | 400 Reason required | 409 { "code": "DOCTOR_UNAVAILABLE" }

GET /appointments/my
Response: [{ "id", "doctorName", "serviceName", "appointmentDate", "status", "reason", "doctorAssigned" }]
Status values: booked | arrived | in-treatment | checked-in

POST /appointments/:id/cancel
Response: { "success": true, "appointment": { "id", "status": "cancelled" } }
Errors:   403 { "error": "Can only cancel your own appointments" } | 404 not found
```

### Visits (portal-scoped)
```
GET /portal/visits
GET /portal/visits?upcoming=true
Response: [{ "id", "visitDate", "status", "service": { "id", "name" }, "doctor": { "id", "name" } }]
Limit: 50 rows
```

### Profile
```
GET /portal/me
Response: { "id", "name", "phone", "email", "dob", "gender" }
Note: tenantId is stripped — never in response
```

### Gift cards
```
GET /giftcards/storefront
Response: { "giftCards": [{ "id", "name", "amount", "price", "color", "validityDays", "currency", "expiresAt" }] }

POST /giftcards/:id/purchase/order
Body:     { "patientId"?: int }
Response: { "orderId", "paymentId", "key" (Razorpay key), "amount" (paise), "currency",
            "giftCardId", "patientId", "patientName" }

POST /giftcards/:id/purchase/confirm
Body:     { "paymentId", "razorpay_order_id", "razorpay_payment_id", "razorpay_signature" }
Response: { "giftCard": {...}, "transaction": {...} }
```

---

## Room DB Entities (offline cache)

```kotlin
// feature/booking/data/local/entity/CachedVisit.kt
@Entity(tableName = "cached_visits")
data class CachedVisit(id, visitDate: Long, status, serviceName?, doctorName?,
                       locationName?, bookingType, videoCallUrl?, amountCharged?, cachedAt)

// feature/health/data/local/entity/CachedPrescription.kt
@Entity(tableName = "cached_prescriptions")
data class CachedPrescription(id, visitId, visitDate: Long, doctorName?, serviceName?,
                               drugCount, pdfBytes: ByteArray?, pdfCachedAt: Long?, cachedAt)

// feature/membership/data/local/entity/CachedMembership.kt
@Entity(tableName = "cached_memberships")
data class CachedMembership(id, status, startDate: Long, endDate: Long, daysLeft,
                             planName, planPrice, planCurrency, creditsJson, historyJson, cachedAt)

// feature/notifications/data/local/entity/CachedNotification.kt
@Entity(tableName = "cached_notifications")
data class CachedNotification(id: String, type, title, body, screen?, entityId?,
                               isRead: Boolean = false, receivedAt: Long)
```

Cache eviction on app start:
- Prescription PDFs: `evictStalePdfs(now - 7.days)` (7-day TTL for PDF bytes)
- Notifications: `deleteOlderThan(now - 90.days)`
- Everything else: cleared on logout

---

## Navigation Routes

```kotlin
sealed class Screen(val route: String) {
    object Splash          : Screen("splash")
    object PhoneEntry      : Screen("phone_entry")
    object OtpVerify       : Screen("otp_verify/{phone}") { fun createRoute(phone: String) = "otp_verify/$phone" }
    object Register        : Screen("register/{phone}")   { fun createRoute(phone: String) = "register/$phone" }
    object Dashboard       : Screen("dashboard")
    object BookAppointment : Screen("book_appointment?serviceId={serviceId}&membershipId={membershipId}") {
        fun createRoute(serviceId: Int? = null, membershipId: Int? = null) =
            "book_appointment?serviceId=${serviceId ?: ""}&membershipId=${membershipId ?: ""}"
    }
    object MyAppointments  : Screen("my_appointments")
    object VisitHistory    : Screen("visit_history")
    object Prescriptions   : Screen("prescriptions")
    object PrescriptionPdf : Screen("prescription_pdf/{id}") { fun createRoute(id: Int) = "prescription_pdf/$id" }
    object TreatmentPlans  : Screen("treatment_plans")   // Phase 2
    object ConsentForms    : Screen("consent_forms")     // Phase 2
    object Memberships     : Screen("memberships")
    object Wallet          : Screen("wallet")
    object GiftCards       : Screen("gift_cards")
    object Loyalty         : Screen("loyalty")           // Phase 2
    object Profile         : Screen("profile")
    object Notifications   : Screen("notifications")
}
// Deep-link scheme: globuscrm://screen/{screenName}?id={entityId}
```

---

## Hilt DI Modules (all in `core/di/`)

| Module | Provides |
|--------|----------|
| `AppModule` | DataStore, EncryptedSharedPreferences |
| `NetworkModule` | OkHttpClient (cert pinned), Retrofit, WellnessApiService |
| `DatabaseModule` | AppDatabase, VisitDao, PrescriptionDao, MembershipDao, NotificationDao |
| `RepositoryModule` | `@Binds` all Repository interfaces → implementations |

---

## Real-time Notifications

> **Current state (2026-08-26): there is no working push channel.** FCM was never
> integrated — there is no Firebase dependency and no `google-services.json`. The
> replacement, Socket.IO, cannot connect either: `https://globuscrm.globussoft.com/socket.io/`
> serves the web app's HTML rather than an Engine.IO handshake, so no gateway is reachable.
>
> Notifications therefore arrive **only** by REST: `GET /portal/me/notifications`, pulled on
> sign-in (`MainViewModel`) and whenever the inbox opens. A patient with the app closed
> receives nothing. Resolving this needs a backend decision — mount the Socket.IO gateway,
> or add FCM — and is the single largest functional gap in the app.

### Socket.IO client

`core/websocket/WellnessSocketManager` connects when a token appears and disconnects on
logout. It is configured by two BuildConfig fields:

| Field | Default | Meaning |
|-------|---------|---------|
| `SOCKET_URL` | `""` (empty) | Gateway origin. **Empty disables the socket entirely.** |
| `SOCKET_PATH` | `/socket.io/` | Engine.IO path on that origin. |

Set `SOCKET_URL` in `build.gradle.kts` once a gateway exists. Connection successes and
failures are logged under the `WellnessSocket` tag — previously failures were swallowed,
which made an unreachable gateway indistinguishable from "no notifications yet".

### Channels (created in MainActivity.onCreate)
| Channel ID | Name | Importance |
|-----------|------|-----------|
| `wellness_reminders` | Appointment Reminders | HIGH + vibration |
| `wellness_health` | Health Updates | DEFAULT |
| `wellness_wallet` | Wallet & Payments | DEFAULT |
| `wellness_offers` | Offers & Surveys | LOW |

### Socket payload `entityType` → channel + preference category
| `entityType` | Channel | Preference category |
|--------------|---------|--------------------|
| `Appointment` | `wellness_reminders` | `appointment_reminders` |
| `Prescription` | `wellness_health` | `prescription_ready` |
| `Wallet` / `Payment` / `Transaction` | `wellness_wallet` | `payment_receipts` |
| `Membership` | `wellness_health` | `membership_updates` |
| `GiftCard` | `wellness_health` | `gift_card_activity` |

### `link` → deep-link screen
`/appointments` → `appointments` · `/prescriptions` → `prescriptions` · `/wallet` → `wallet`
· `/memberships` → `memberships` · `/book` → `book`

### Notification preferences

Device-local, stored in DataStore via `NotificationPreferencesRepository` (there is no
backend preference endpoint). `WellnessSocketManager` honours them before raising anything:
a muted category is dropped entirely, the `in_app` channel gates Room persistence, `push`
gates the system tray, and quiet hours suppress the tray alert only. Preferences survive
logout — they belong to the device, not the patient.

---

## Theme

| Token | Value |
|-------|-------|
| Primary color | `#8A6D23` (antique bronze-gold) |
| Accent color | `#6E6656` (warm silver-taupe) |
| Background | `#F5F1E8` (warm cream) |
| Primary button text | White |
| Body font | Roboto |
| Heading font | Playfair Display |
| Card radius | 12dp |
| Button radius | 24dp |

Apply clinic's `Tenant.brandColor` at runtime as the Material 3 seed color (fallback: `#8A6D23`).

---

## Security Rules (enforce always)

- JWT stored in `DataStore` (app-private) — not SharedPreferences
- Patient name and phone stored in `EncryptedSharedPreferences` (AES-256-GCM, Android Keystore)
- Prescription PDFs in Room BLOB — served via `FileProvider`, never auto-saved to Downloads
- **No PHI in Logcat, Sentry breadcrumbs, or Firebase Analytics params** — use `patientId` (int), never name/phone
- `POST_NOTIFICATIONS` permission requested at runtime before any notification is raised
- Cert pinning in **both** `network_security_config.xml` and `NetworkModule.CERT_PINS` — keep
  the two lists in sync. Pins target the **Google Trust Services intermediate + root**, not
  the leaf: GTS rotates leaves ~every 90 days and a leaf pin would break the app on renewal.
  Debug builds ship `src/debug/res/xml/network_security_config.xml` with no pin-set so a
  proxy can be used; the OkHttp pinner is likewise release-only.
- OkHttp logging is `HEADERS`, never `BODY` — response bodies carry PHI. `Authorization`,
  `Cookie` and `Set-Cookie` are redacted.
- The HTTP disk cache is restricted to a **non-PHI catalogue allowlist**
  (`CatalogueCacheInterceptor`) and is evicted on logout. Never add a patient-scoped route
  to it — OkHttp writes response bodies to disk in plaintext.
- R8 minification enabled in release builds

---

## Implementation Phases

### Phase 0 — Bootstrap
Android Studio project, `libs.versions.toml`, `build.gradle.kts` (`minSdk 26`, `compileSdk 36`), `network_security_config.xml`, `BuildConfig.BASE_URL` / `TENANT_SLUG` / `TENANT_ID` / `SOCKET_URL`.

### Phase 1 — Core module
`core/network/` → `core/storage/` → `core/util/Result.kt` → `core/di/` (all 4 modules) → `core/theme/` → `core/navigation/Screen.kt` → `AppDatabase` with 4 entities.

### Phase 2 — Auth feature
`SplashScreen` (branding load + token check) → `PhoneEntryScreen` → `OtpVerifyScreen` (SMS Retriever API, 4-box input, 60s resend timer) → `RegisterScreen`. Token saved to DataStore on success. FCM token registered immediately after login.

### Phase 3 — Dashboard feature
`GET /portal/me/dashboard` → single-call aggregated response → greeting, next-appointment card (with cancel), 3 tappable stat chips (wallet, loyalty, memberships), quick-action row.

### Phase 4 — Booking feature
4-step `BookAppointmentScreen`: services grid → location + booking type → slot grid (30-min intervals, holiday-aware) → reason + membership picker. Plus `MyAppointmentsScreen` (upcoming/past tabs, cancel, Join Video Call for VIDEO type) and `VisitHistoryScreen` (grouped by month, bottom sheet detail).

### Phase 5 — Health feature
`PrescriptionsScreen` (permission-gated on `my_prescriptions.read`) → in-app PDF viewer via Android `PdfRenderer` → cached in Room `pdfBytes` (7-day eviction). Phase 2: Treatment Plans + Consent Forms screens.

### Phase 6 — Membership feature
`MembershipsScreen`: Active/Expired tabs, per-service `LinearProgressIndicator` (remaining/total), redemption history `ModalBottomSheet`, plan catalog accordion (GET /membership-plans), "Book with this membership" CTA.

### Phase 7 — Wallet + Gift cards
`WalletScreen`: balance + transaction timeline with type icons. `GiftCardsScreen`: 2-column storefront grid → review modal → Razorpay Android SDK checkout → wallet credit confirmation.

### Phase 8 — Profile + Notifications
`ProfileScreen`: view/edit name/email/DOB/gender, DSAR export (`POST /portal/export`), logout (clear all storage + FCM deregister). `NotificationInboxScreen`: Room-backed list, mark-read on tap, deep-link on tap, 90-day eviction.

### Phase 9 — Notification delivery
`WellnessSocketManager` persists incoming events to Room and raises a system notification on the right channel, subject to the device notification preferences. `MainActivity` creates the 4 channels in `onCreate()` and requests `POST_NOTIFICATIONS` at runtime. **The socket gateway is not reachable — see Real-time Notifications.**

### Phase 10 — Testing
UseCase unit tests (JUnit 5 + MockK), ViewModel tests (Turbine), Room DAO integration tests (in-memory), key UI tests (Compose + Hilt). Target: 100% UseCase coverage, ≥90% ViewModel coverage.

### Phase 11 — Release prep
R8 full minification, ProGuard rules, cert pin update, Room migrations (replace `fallbackToDestructiveMigration`), Play Store internal testing track upload.

---

## Key Library Versions

```toml
kotlin              = "2.0.21"
compose-bom         = "2024.12.01"
hilt                = "2.52"
retrofit            = "2.11.0"
okhttp              = "4.12.0"
moshi               = "1.15.1"
room                = "2.6.1"
datastore           = "1.1.1"
razorpay            = "1.6.40"
coil                = "2.7.0"
camerax             = "1.4.1"
socket-io-client    = "2.1.0"
turbine             = "1.2.0"
compose-navigation  = "2.8.5"
hilt-navigation     = "1.2.0"
```

> No Firebase dependency and no `google-services.json` — FCM is not integrated. Sentry is
> declared in `libs.versions.toml` but not applied; the manifest DSN is empty.

The authoritative list is `gradle/libs.versions.toml` in this repo.

---

## status.md — Live Progress Tracker

`status.md` lives at the **repo root** alongside `CLAUDE.md`. (A duplicate `STATUS.md` was
removed on 2026-08-26 — two files differing only in case break checkouts on macOS and
Windows. Do not recreate it.) It is the single source of truth for what is done and what is left. Claude must read it at session start and update it after every completed task.

### Rules for status.md

- **Read it first** — before writing any code, read `status.md` to know exact current state
- **Update it immediately** — mark a task `✅` the moment a file is written and compiles; do not batch updates
- **Never delete entries** — completed items stay in the file so progress is always visible
- **Add blockers inline** — if a backend gap endpoint is missing, note it with `🔴 BLOCKED: <reason>` on that task row
- **One source of truth** — if `status.md` says something is done, it is done; if it is not in `status.md`, it has not been started

### status.md format

```markdown
# WellnessCRM Patient App — Implementation Status

Last updated: YYYY-MM-DD HH:MM
Current phase: Phase X — <name>

## Legend
✅ Done  🔄 In Progress  ⬜ Not started  🔴 Blocked

---

## Phase 0 — Bootstrap
✅ Android project created
✅ libs.versions.toml configured
...

## Phase 1 — Core Module
✅ core/network/WellnessApiService.kt
🔄 core/network/AuthInterceptor.kt
⬜ core/network/TokenManager.kt
...

## Backend Gap Endpoints
✅ POST /portal/register — built + tested
🔴 GET /portal/me/dashboard — NOT YET BUILT on CRM backend
⬜ GET /portal/slots
...

## Phase 2 — Auth Feature
⬜ feature/auth/data/remote/dto/AuthTokenDto.kt
...
```

### When to update status.md

| Trigger | Action |
|---------|--------|
| Session start | Read status.md, confirm current phase, resume from first `⬜` or `🔄` |
| File written + compiles | Mark `✅` immediately |
| File started but not finished | Mark `🔄` |
| Backend endpoint confirmed working | Mark `✅` in Backend Gap section |
| Backend endpoint missing | Mark `🔴 BLOCKED` with note |
| Phase fully complete | Add a `✅ Phase X complete — YYYY-MM-DD` line at top of that section |

---

## Session Start Checklist

At the start of every Claude session in this repo, you should:

1. **Read `status.md`** — find the current phase and the first `⬜` or `🔄` task; that is where you start
2. **Check backend gap endpoints** — for the current feature's required endpoints, confirm they exist on the CRM backend before proceeding; update status.md with `✅` or `🔴`
3. **Follow the architecture rules** — re-read the 8 rules in "Architecture Rules" before writing any new class
4. **One feature at a time** — complete all 3 layers (data → domain → presentation) for a feature before starting the next
5. **Test as you go** — write the UseCase test alongside the UseCase, not after
6. **Update `status.md` after every file** — never let it fall behind actual progress

## What NOT to do

- Do not put any business logic in a Composable or ViewModel
- Do not let a feature import from another feature package
- Do not use `runBlocking` outside of `AuthInterceptor`
- Do not log patient name, phone, or clinical details to Logcat or analytics
- Do not hardcode the tenant slug in Composables — always read from `BuildConfig.TENANT_SLUG`
- Do not skip the `Result<T>` wrapper — every UseCase must return it
- Do not use `LiveData` — use `StateFlow` and `Flow` only
- Do not use XML layouts — Jetpack Compose only
