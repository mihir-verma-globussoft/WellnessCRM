# WellnessCRM Patient App — iOS Implementation Spec
## Scope: Prescriptions (+ Renewal Requests + Medication Reminders), Packages, Notifications

This document reverse-engineers three existing Android features from the WellnessCRM patient
app (`com.crm.enhance_wellness`, Kotlin + Jetpack Compose + Hilt + Retrofit + Room) so an iOS
agent can reproduce them exactly. Nothing here is redesigned, simplified, or invented — every
claim is traceable to a file path/class/method in the Android repo, cited inline as
`file.kt:line`. Where something could not be determined, it is listed under **Unknown /
Requires Verification** rather than guessed.

The three features share one architecture pattern and one dependency-injection mechanism, so
those are documented once (Section 0) rather than three times. Everything else is documented
per feature.

---

# 0. SHARED ARCHITECTURE (applies to all three features)

## 0.1 Layering

```
Composable (Screen)
    ↓ (UiState read, UiEvent sent via onEvent lambda)
ViewModel (@HiltViewModel)
    ↓ (calls UseCase(s), holds MutableStateFlow<UiState>, Channel<NavEvent>)
UseCase (one class, one suspend operator fun invoke(), wraps Result<T>)
    ↓
Repository interface (domain layer, pure Kotlin)
    ↓
RepositoryImpl (data layer)
    ↓
WellnessApiService (Retrofit interface) and/or Room DAO and/or DataStore
    ↓
Remote: Moshi DTO ← JSON. Local: Room @Entity / DataStore Preferences.
    ↓ (Mapper: `Dto.toDomain()`, `Entity.toDomain()`, `Domain.toEntity()`)
Domain model (pure Kotlin data class, no Android imports)
    ↑ (flows back up through the same chain)
ViewModel updates StateFlow<UiState>
    ↓
Composable recomposes
```

- **Composables are stateless**: `Screen(state: XUiState, onEvent: (XUiEvent) -> Unit)`. No
  ViewModel reference inside a Composable.
- **One-shot navigation**: ViewModels expose `Channel<NavEvent>` (buffered) → `receiveAsFlow()`.
  The NavGraph composable collects this in a `LaunchedEffect(Unit)` and calls
  `navController.navigate(...)`. Composables never navigate directly.
- **`Result<T>`** is the universal UseCase return type
  (`core/util/Result.kt`):
  ```kotlin
  sealed class Result<out T> {
      data class Success<T>(val data: T) : Result<T>()
      data class Error(val code: String, val message: String, val httpStatus: Int? = null) : Result<Nothing>()
      object Loading : Result<Nothing>()
  }
  ```
  In practice `Result.Loading` is declared but never emitted by any UseCase in this codebase —
  loading state is tracked separately as a `Boolean` field on each `UiState`. Every `when`
  over `Result` still has a `Result.Loading -> Unit` branch for exhaustiveness.
- **Mapper naming**: `Dto.toDomain()` (remote → domain), `Entity.toDomain()` (cache → domain),
  `Domain.toEntity()` (domain → cache). DTOs and Entities never leave the `data/` layer.

## 0.2 Dependency Injection

- **Framework**: Hilt (`@HiltViewModel`, `@Inject constructor`, `@Singleton`).
- **`core/di/RepositoryModule.kt`**: `@Binds abstract fun bindXRepository(impl: XRepositoryImpl): XRepository` for every repository interface in scope here — `PrescriptionRepository`, `PrescriptionRequestRepository`, `NotificationRepository`, `NotificationPreferencesRepository`, `CatalogRepository` (`core/di/RepositoryModule.kt:57,60-62,77,80-82,94`).
- **`core/di/NetworkModule.kt`**: provides the single Retrofit-backed `WellnessApiService` singleton used by every repository below.
- **`core/di/DatabaseModule.kt`**: provides `AppDatabase` and the DAOs used here (`PrescriptionDao`, `PrescriptionReminderDao`, `NotificationDao`).
- ViewModels receive UseCases (not Repositories) as constructor params; UseCases receive Repositories (and occasionally `WellnessApiService`/`Moshi` directly, noted per-case below).
- Scope: everything above is `@Singleton` except ViewModels (`@HiltViewModel`, scoped to the NavGraph's `hiltViewModel()` call site — i.e. to the back-stack entry).

## 0.3 Navigation host

Single-Activity Jetpack Compose Navigation graph, `core/navigation/NavGraph.kt`. Routes are
defined in `core/navigation/Screen.kt` as a sealed class. Deep links use the custom scheme
`globuscrm://screen/...` and are declared per-`composable()` via `navDeepLink { uriPattern = ... }`.

## 0.4 Backend base URL & auth

All endpoints below are relative to `WellnessApiService`'s configured base
(`https://<host>/api/wellness/` — see root `CLAUDE.md` for environment values), except where a
comment says "absolute path". Every call in scope here requires
`Authorization: Bearer <portal_jwt>` and is gated by `verifyPatientToken` (CUSTOMER `userType`
only) unless stated otherwise. All error bodies follow `{ "error": "message", "code": "OPTIONAL_CODE" }`.

---

# PART 1 — PRESCRIPTIONS (incl. Renewal Requests + Medication Reminders)

## 1. FEATURE OVERVIEW

**What it does.** Patients view their prescriptions (list → PDF), request a renewal/refill of a
prescription (full or partial, with optional duration and notes), track the status of past
renewal requests, and optionally turn on local dosage + "medicine running low" reminders per
prescription. A prescription card also links out to a "Before/After Scan" (CameraX treatment
analysis) — that sub-feature is out of scope for this document; only the navigation hook is
noted.

**Entry points:**
- Dashboard quick action `DashboardNavEvent.ToPrescriptions` → `Screen.Prescriptions.route` (`core/navigation/NavGraph.kt:238`).
- Dashboard quick action `DashboardNavEvent.ToPrescriptionRequests` → `Screen.PrescriptionRequests.route` directly (`core/navigation/NavGraph.kt:247`), bypassing the list.
- Deep link `globuscrm://screen/prescriptions` (`NavGraph.kt:316`).
- Deep link `globuscrm://screen/prescription_pdf?id={id}` (`NavGraph.kt:385`) — used by the medication-reminder notification's tap action.
- In-app: "My Prescription Requests" button atop the Prescriptions list → `PrescriptionRequestsScreen`.

**Screens (all in `feature/health/presentation/screen/`):**
1. `PrescriptionsScreen.kt` — list of prescriptions, main screen.
2. `PrescriptionPdfScreen.kt` — in-app PDF viewer for one prescription.
3. `PrescriptionRequestsScreen.kt` — read-only history of renewal requests ("My Prescription Requests").

**User actions on the list screen:**
- Pull-to-refresh (re-fetches both prescriptions and open-renewal status).
- Tap a card → confirmation dialog → "Open" → downloads/opens the PDF.
- Tap "Before/After Scan" → navigates to Treatment Analysis (CameraX), out of scope here.
- Toggle "Reminder" switch per card → enables/disables local dosage reminders for that prescription. Disabled (switch itself disabled, not hidden) when the prescription has zero parseable drugs.
- Tap "Request a renewal" → opens a dialog to submit a refill request. Replaced by a static "Renewal pending" chip once a request is open (status PENDING or ACCEPTED).
- Tap "My Prescription Requests" → navigates to the request-history screen.

**States:**
- Loading: `CircularProgressIndicator` centered, full-screen (`state.isLoading`).
- Permission-blocked: centered `ErrorState` with message "Prescription access is not enabled for your account. Contact your clinic." — shown when the patient's role lacks the `my_prescriptions.read` permission. Retry re-checks permission.
- Error: centered `ErrorState` with the backend/network error message and a Retry button.
- Empty: centered text "No prescriptions found" (only reachable after a successful load with zero items — there is no separate `Result` case for it).
- Success: `LazyColumn` of `PrescriptionCard`s, with the "My Prescription Requests" button as the first item.

**Permissions involved:**
- Backend RBAC: `my_prescriptions.read` (checked client-side via `GetPatientPermissionsUseCase` before the prescriptions call is even made — see 5.1).
- Android runtime: `POST_NOTIFICATIONS` (for both dosage reminders and the "renewal pending"/error toasts, though toasts don't need it — only the actual reminder notifications do).
- Android runtime (API 31+): `SCHEDULE_EXACT_ALARM` — optional; reminders still work approximately without it.

**Conditional behavior — important:**
- The reminder switch is only interactive if the prescription has ≥1 drug that parses to a valid `ScheduledMedication` (non-blank name, frequency > 0, duration > 0 after unit parsing — see 6.3).
- "Request a renewal" is replaced by a static pending-chip whenever `prescription.id` is in the currently-open (`PENDING`/`ACCEPTED`) renewal-request set. This set is refetched via `ON_RESUME` every time the screen becomes visible (not just on first load), because there is no push channel for renewal-status changes (`PrescriptionsScreen.kt:76-82`).
- Submitting a renewal for "the complete prescription" sends `medicines: null`; submitting for specific drugs sends the list of drug names checked in the dialog. At least one drug must be selected if not renewing the full prescription (client-side validation, no network round-trip).

## 2. ARCHITECTURE

Follows the shared pattern in Section 0 exactly. Three sub-domains inside `feature/health/`:
- **Prescriptions** (list + PDF): `PrescriptionRepository` / `PrescriptionRepositoryImpl`, with a Room cache (`PrescriptionDao`, `CachedPrescription`).
- **Renewal requests**: `PrescriptionRequestRepository` / `PrescriptionRequestRepositoryImpl`, **no local cache** — always live.
- **Medication reminders**: `feature/health/reminder/` — its own mini-package: `PrescriptionReminderRepository` (business logic + Room), `MedicationReminderScheduler` (AlarmManager), `MedicationReminderReceiver` / `MedicationReminderRescheduleReceiver` (BroadcastReceivers), `PrescriptionReminderDao` + `PrescriptionReminderEntity` (Room). This is Android-native functionality with no domain/UseCase layer of its own — `PrescriptionsViewModel` calls `PrescriptionReminderRepository` directly.

```
PrescriptionsScreen ↔ PrescriptionsViewModel
    ├─ GetPrescriptionsUseCase → PrescriptionRepository → WellnessApiService.getPrescriptions() / PrescriptionDao (cache)
    ├─ GetPatientPermissionsUseCase (auth feature; gates the whole screen)
    ├─ GetPrescriptionRequestsUseCase → PrescriptionRequestRepository → WellnessApiService.getPrescriptionRequests()
    ├─ SubmitPrescriptionRenewalUseCase → PrescriptionRequestRepository → WellnessApiService.submitPrescriptionRequest()
    └─ PrescriptionReminderRepository (direct, no UseCase) → PrescriptionReminderDao + MedicationReminderScheduler

PrescriptionPdfScreen ↔ PrescriptionPdfViewModel
    └─ GetPrescriptionPdfUseCase → PrescriptionRepository → WellnessApiService.getPrescriptionPdf() / PrescriptionDao (PDF byte cache)

PrescriptionRequestsScreen ↔ PrescriptionRequestsViewModel
    └─ GetPrescriptionRequestsUseCase → PrescriptionRequestRepository
```

## 3. COMPLETE FILE INVENTORY

| Layer | File | Responsibility | Depends on |
|---|---|---|---|
| Presentation | `feature/health/presentation/screen/PrescriptionsScreen.kt` | List screen, reminder toggle UI, renewal dialog | `PrescriptionsUiState/UiEvent` |
| Presentation | `feature/health/presentation/screen/PrescriptionPdfScreen.kt` | Renders PDF bytes via `android.graphics.pdf.PdfRenderer`, download-to-Downloads FAB | `PrescriptionPdfUiState/UiEvent` |
| Presentation | `feature/health/presentation/screen/PrescriptionRequestsScreen.kt` | Read-only renewal-request history list | `PrescriptionRequestsUiState/UiEvent` |
| Presentation | `feature/health/presentation/viewmodel/PrescriptionsViewModel.kt` | Orchestrates list load, permission gate, reminder toggle, renewal submit | 4 UseCases + `PrescriptionReminderRepository` |
| Presentation | `feature/health/presentation/viewmodel/PrescriptionPdfViewModel.kt` | Loads one PDF by `SavedStateHandle["id"]` | `GetPrescriptionPdfUseCase` |
| Presentation | `feature/health/presentation/viewmodel/PrescriptionRequestsViewModel.kt` | Loads request history | `GetPrescriptionRequestsUseCase` |
| Presentation | `feature/health/presentation/state/HealthState.kt` | All UiState/UiEvent classes for health feature (incl. Treatment Plans/Consent Forms, out of scope) | — |
| Domain | `feature/health/domain/model/Prescription.kt` | `Prescription`, `Drug` domain models | — |
| Domain | `feature/health/domain/model/PrescriptionRequest.kt` | `PrescriptionRequest` domain model + `isOpen`/`drugNames` derived properties, `toRefillStatusLabel()` | — |
| Domain | `feature/health/domain/repository/PrescriptionRepository.kt` | Interface: prescriptions + PDF cache contract | — |
| Domain | `feature/health/domain/repository/PrescriptionRequestRepository.kt` | Interface: get/submit renewal requests | — |
| Domain | `feature/health/domain/usecase/GetPrescriptionsUseCase.kt` | Fetch list, offline fallback to cache | `PrescriptionRepository` |
| Domain | `feature/health/domain/usecase/GetPrescriptionPdfUseCase.kt` | Fetch-or-cache one PDF | `PrescriptionRepository` |
| Domain | `feature/health/domain/usecase/GetPrescriptionRequestsUseCase.kt` | Fetch renewal-request history | `PrescriptionRequestRepository` |
| Domain | `feature/health/domain/usecase/SubmitPrescriptionRenewalUseCase.kt` | Submit a renewal request | `PrescriptionRequestRepository` |
| Data | `feature/health/data/remote/dto/HealthDtos.kt` | `PrescriptionDto`, `PrescriptionRequestDto`, `SubmitPrescriptionRequestDto`, `MyPrescriptionsResponseDto`, and their nested DTOs | Moshi |
| Data | `feature/health/data/remote/dto/FlexibleDrugs.kt` | Custom Moshi adapter: normalizes `drugs` field (string-encoded-JSON OR real JSON array) to a string | Moshi |
| Data | `feature/health/data/mapper/HealthMappers.kt` | All DTO/Entity ↔ Domain mappers for health feature, incl. the defensive `drugs` JSON parser | org.json |
| Data | `feature/health/data/repository/PrescriptionRepositoryImpl.kt` | Implements `PrescriptionRepository`; talks to API + `PrescriptionDao` | `WellnessApiService`, `PrescriptionDao` |
| Data | `feature/health/data/repository/PrescriptionRequestRepositoryImpl.kt` | Implements `PrescriptionRequestRepository`; defensive empty-body parsing | `WellnessApiService`, `Moshi` |
| Data | `feature/health/data/local/entity/CachedPrescription.kt` | Room entity: prescription list cache + PDF byte cache | Room |
| Data | `feature/health/data/local/dao/PrescriptionDao.kt` | Room DAO: getAll, getById, insert(All), evictStalePdfs, deleteAll | Room |
| Reminders | `feature/health/reminder/PrescriptionReminderRepository.kt` | Business logic: enable/disable/cleanup reminders, "Request Meds" (currently unwired — see §12) | `PrescriptionReminderDao`, `MedicationReminderScheduler`, `WellnessApiService` |
| Reminders | `feature/health/reminder/MedicationReminderScheduler.kt` | AlarmManager scheduling (dose alarms + one depletion alarm per drug) | `AlarmManager` |
| Reminders | `feature/health/reminder/MedicationReminderReceiver.kt` | `BroadcastReceiver`; builds and shows the actual system notification | `NotificationCompat`, `PrescriptionReminderRepository` |
| Reminders | `feature/health/reminder/MedicationReminderRescheduleReceiver.kt` | `BroadcastReceiver`; reschedules all active reminders on boot/app-update/timezone-change | `PrescriptionReminderRepository` |
| Reminders | `feature/health/reminder/MedicationReminderModels.kt` | `ScheduledMedication`, frequency/duration string parsers, JSON (de)serialization for `drugsJson` | org.json |
| Reminders | `feature/health/data/local/entity/PrescriptionReminderEntity.kt` | Room entity: one row per prescription with reminders enabled | Room |
| Reminders | `feature/health/data/local/dao/PrescriptionReminderDao.kt` | Room DAO: CRUD + `getAllAsFlow()` for the enabled-reminder-ids stream | Room |
| Shared | `core/network/WellnessApiService.kt` | Retrofit endpoint declarations (see §8) | Retrofit |
| Shared | `core/util/Result.kt`, `core/util/DateUtil.kt` | Universal Result wrapper; date parsing/formatting | — |
| Shared | `feature/auth/domain/usecase/GetPatientPermissionsUseCase.kt`, `feature/auth/domain/model/PatientPermissions.kt` | Gates the whole prescriptions screen on `my_prescriptions.read` | auth feature (cross-feature use is allowed only through this kind of shared domain util, not through screens) |

## 4. PRESENTATION / UI — PrescriptionsScreen

`feature/health/presentation/screen/PrescriptionsScreen.kt`

- Root: `PullToRefreshBox` over full-screen `Modifier.fillMaxSize().background(background)`.
- `LifecycleEventEffect(Lifecycle.Event.ON_RESUME)` re-sends `Refresh` every time the screen resumes (see conditional-behavior note above).
- Body `when`: Loading → centered spinner. `permissionBlocked` → `ErrorState`. `error != null` → `ErrorState`. Empty list → centered "No prescriptions found". Else → `LazyColumn`, 16dp content padding, 12dp vertical spacing.
- First list item: full-width `OutlinedButton` "My Prescription Requests" with a `History` icon, key `"request_history_button"`.
- Each item: `PrescriptionCard` (key = `prescription.id`).

**`PrescriptionCard` layout** (`WellnessCard`, 16dp padding, 10dp vertical spacing):
- Row: title column (service name or "Prescription" fallback, doctor name if present, visit date in primary color) + trailing `TextButton` "PDF" with `PictureAsPdf` icon → `RequestViewPdf`.
- Full-width `OutlinedButton` "Before/After Scan" with `CameraAlt` icon → `StartTreatmentAnalysis`.
- `ExpandableInstructions`: shows `instructions` text truncated to 2 lines with a "Show more"/"Show less" toggle that only appears if the text actually overflows (`onTextLayout { hasVisualOverflow }`). Hidden entirely if `instructions` is null/blank.
- Row: `Surface` chip "{n} medication(s)" (secondaryContainer color) on the left, "Reminder" label + `Switch` on the right. Switch `enabled = !reminderInProgress && prescription.drugs.isNotEmpty()`.
- Bottom: either a `Surface` chip "Renewal pending" (tertiaryContainer) if `renewalPending`, or a `TextButton` "Request a renewal" (Medication icon) `enabled = prescription.drugs.isNotEmpty()`.

**Dialogs:**
1. PDF-open confirmation (`AlertDialog`): title "Open prescription PDF?", body "This will download the document to view it in the app.", Confirm "Open" → `ConfirmViewPdf`, Cancel → `DismissPdfConfirm`. Shown when `state.showPdfConfirm`.
2. `RenewalRequestDialog` (`AlertDialog`, shown when `state.renewalDialogPrescription != null`):
   - Local (Compose-only, not in ViewModel state) fields keyed by `prescription.id`: `fullPrescription: Boolean` (default `true`), `selectedMedicines: Set<String>`, `durationText: String`, `notesText: String`, `validationError: String?`.
   - Header text: `"Prescription #{id}"` + optional `" · Dr {doctorName}"`.
   - Checkbox "Renew the complete prescription" — when unchecked, reveals a per-drug checkbox list ("Choose the medicines you need:").
   - `OutlinedTextField` "How long do you need it for? (optional)" — digits only, max 3 chars, numeric keyboard.
   - `OutlinedTextField` "Anything the clinic should know? (optional)" — free text, min 2 lines.
   - Client-side validation: if `!fullPrescription && selectedMedicines.isEmpty()`, blocks submit and shows "Pick at least one medicine, or ask for the whole prescription." — no network call made.
   - Confirm button "Send request" shows a small spinner + disables all fields while `inProgress`.
   - Cancel is disabled while `inProgress`; dialog cannot be dismissed by tapping outside while `inProgress` either (`onDismissRequest = { if (!inProgress) onDismiss() }`).

**Toasts** (Android `Toast.makeText(..., LENGTH_SHORT)`):
- `state.reminderMessage` (any reminder enable/disable result or error) — shown once via `LaunchedEffect(state.reminderMessage)` then dismissed via `DismissReminderMessage`.
- `state.renewalMessage` ("Renewal request sent." on success, or the backend error message on failure) — same one-shot pattern via `DismissRenewalMessage`.

**Exact-alarm permission prompt**: if `state.exactAlarmPermissionPromptNeeded` and API ≥ 31, launches `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` for this package, then immediately clears the flag (`ExactAlarmPermissionPromptShown`) — it does not wait for the user's answer, and does not re-check afterward.

## 4b. PRESENTATION / UI — PrescriptionPdfScreen

`feature/health/presentation/screen/PrescriptionPdfScreen.kt`

- Loading → centered spinner. Error → centered `CloudOff` icon (48dp, error color) + message text. Success (`pdfBytes != null`) → `PdfViewer`.
- `PdfViewer`: writes `pdfBytes` to a temp file in `context.cacheDir`, opens it with `android.graphics.pdf.PdfRenderer`, renders every page to a `Bitmap` at **2× native page resolution** (`page.width * 2, page.height * 2`, white background, `RENDER_MODE_FOR_DISPLAY`), then deletes the temp file. Pages are laid out in a vertically-scrolling `Column`, one `Image` per page, `ContentScale.FillWidth`, 8dp page spacing, 80dp bottom padding to clear the FAB.
- `FloatingActionButton` (bottom-end, `FileDownload` icon) → `savePdfToDownloads`:
  - API ≥ 29 (Q): `MediaStore.Downloads` insert with `DISPLAY_NAME`, `MIME_TYPE = "application/pdf"`, `RELATIVE_PATH = DIRECTORY_DOWNLOADS`. No extra runtime permission needed on this path (scoped storage).
  - API < 29: requires `WRITE_EXTERNAL_STORAGE` granted; writes directly to the public Downloads dir via legacy `File` API. Returns `false` (no exception) if not granted.
  - File name pattern: `"{filePrefix}_{System.currentTimeMillis()}.pdf"`, `filePrefix = "prescription"` here.
  - Result toast: "Saved to Downloads" or "Download failed — storage permission may be required".
- No top bar in this Composable — the caller (NavGraph / outer Scaffold, not shown here) supplies chrome and the back action.

## 4c. PRESENTATION / UI — PrescriptionRequestsScreen

`feature/health/presentation/screen/PrescriptionRequestsScreen.kt`

- `PullToRefreshBox` → Loading spinner / `ErrorState` / empty text "No medicine or refill requests found" / `LazyColumn` (16dp padding, 12dp spacing) of `PrescriptionRequestCard`s (key = `request.id`).
- **`PrescriptionRequestCard`** (`WellnessCard`, 16dp padding, 8dp spacing):
  - Row: title = comma-joined `request.drugNames` (falls back to `"Prescription #{prescriptionId}"` if empty) + doctor name below it; trailing `StatusChip(status = request.status.toRefillStatusLabel())`.
  - "Full prescription refill" or "Partial refill" label, based on `isFullPrescription`.
  - "Requested: {DateUtil.toDisplayDate(createdAt)}".
  - "Requested duration: {n} days" — only if `requestedDurationDays != null`.
  - "Note: {notes}" — only if notes present.
  - If `reviewedByName` present: `HorizontalDivider` then "Reviewed by {name}" + optional " · {date}", then `reviewNote` text if present.

## 5. VIEWMODEL / PRESENTATION LOGIC

### 5.1 `PrescriptionsViewModel` (`feature/health/presentation/viewmodel/PrescriptionsViewModel.kt`)

Constructor deps: `GetPrescriptionsUseCase`, `GetPatientPermissionsUseCase`, `PrescriptionReminderRepository`, `GetPrescriptionRequestsUseCase`, `SubmitPrescriptionRenewalUseCase`.

State: `MutableStateFlow<PrescriptionsUiState>`. Nav: `Channel<PrescriptionsNavEvent>` (`ToPdf(id)`, `ToAnalysis(prescriptionId, visitId)`, `Back`, `ToRequestHistory`).

`init {}` calls, in order: `observeReminderState()`, `load()`, `loadOpenRenewalRequests()` — all three run concurrently (no sequencing dependency).

| Method | Params | Called by | What it does | State changes | Errors |
|---|---|---|---|---|---|
| `observeReminderState()` | — | `init` | `reminderRepository.cleanupExpired()` once, then collects `reminderRepository.enabledReminderIds()` forever | `reminderEnabledIds` on every emission | — |
| `load()` | — | `init`, `Refresh` event | Sets loading; calls `GetPatientPermissionsUseCase()`; if `Result.Success` and permission missing → sets `permissionBlocked=true`, stops. Else calls `GetPrescriptionsUseCase()` | `isLoading`, `error`, `permissionBlocked`, `prescriptions` | Error message surfaced via `ErrorState` |
| `loadOpenRenewalRequests()` | — | `init`, `Refresh` event | Sets `renewalStatusLoading=true`; calls `GetPrescriptionRequestsUseCase()`; on success, filters `it.isOpen` and stores their `prescriptionId`s as a `Set<Int>` | `openRenewalPrescriptionIds`, `renewalStatusLoading` | Non-fatal on failure — old ids kept as-is, spinner just clears |
| `toggleReminder(prescription, enabled)` | `Prescription`, `Boolean` | `ToggleReminder` event | Sets `reminderActionInProgressId`; calls `reminderRepository.enable(prescription)` or `.disable(prescription.id)` | `reminderActionInProgressId=null`, `reminderMessage`, `exactAlarmPermissionPromptNeeded` | Result always "succeeds" at the ViewModel layer — the repo returns a message string either way |
| `submitRenewal(fullPrescription, selectedMedicines, durationDays, notes)` | — | `SubmitRenewal` event | Reads `renewalDialogPrescription` from state; sets `renewalSubmitInProgress=true`; calls `SubmitPrescriptionRenewalUseCase(prescriptionId, medicines = if(full) null else list, durationDays, notes)` | On success: closes dialog, sets `renewalMessage="Renewal request sent."`, re-runs `loadOpenRenewalRequests()`. On error: keeps dialog open, sets `renewalMessage=result.message` | Backend/network error text shown as toast, dialog stays open for retry |
| `openTreatmentAnalysis(prescriptionId)` | `Int` | `StartTreatmentAnalysis` event | Looks up `visitId` from the already-loaded `prescriptions` list, sends `ToAnalysis` nav event | — | — |

`onEvent()` is a flat `when` over `PrescriptionsUiEvent` — every case either mutates state directly (dialog show/hide, toast dismiss) or delegates to one of the methods above.

### 5.2 `PrescriptionPdfViewModel`

Reads `prescriptionId` once from `SavedStateHandle["id"]` (crashes via `checkNotNull` if absent — this is a required nav arg, not optional). `init` calls `loadPdf()`, which calls `GetPrescriptionPdfUseCase(prescriptionId)` and maps the `Result` straight onto `PrescriptionPdfUiState`. No refresh capability from this screen. `NavigateBack` event → `Back` nav event.

### 5.3 `PrescriptionRequestsViewModel`

`init` calls `load()`. `Refresh` event re-calls it. `load()` replaces the whole state object (not a `.copy()`) with `PrescriptionRequestsUiState(isLoading=true)` then the result — so every field resets on each load, unlike `PrescriptionsViewModel` which preserves unrelated fields via `.copy()`.

## 6. DOMAIN MODELS

### 6.1 `Prescription` (`feature/health/domain/model/Prescription.kt`)

| Property | Type | Optional | Meaning |
|---|---|---|---|
| id | Int | No | Prescription's backend id |
| visitId | Int? | Yes | The visit this prescription came from |
| visitDate | String? | Yes | ISO date string of the visit |
| createdAt | String? | Yes | ISO datetime the prescription record was created — used as the reminder schedule's start anchor |
| instructions | String? | Yes | Free-text instructions shown on the card |
| doctorName | String? | Yes | Prescribing doctor's display name |
| serviceName | String? | Yes | Name of the service/visit type; used as the card title fallback |
| drugs | List\<Drug\> | No (can be empty) | Parsed drug list — see 11.1 for the defensive parser |
| pdfBytes | ByteArray? | Yes, default null | Cached PDF bytes (only populated on cached rows, not on list-fetch rows) |
| pdfCachedAt | Long? | Yes, default null | Epoch ms the PDF was cached |

Custom `equals`/`hashCode`: compares only `id` and `visitId` (not the full object, including `drugs`/`pdfBytes`) — this affects Compose `LazyColumn` item diffing and any `Set`/`distinctBy` usage on this type.

`Drug`: `name: String`, `dosage: String?`, `frequency: String?`, `duration: String?`, `instructions: String?` — all raw strings, not typed/parsed at this layer (parsing happens later, only for the reminder feature — see 6.3).

### 6.2 `PrescriptionRequest` (`feature/health/domain/model/PrescriptionRequest.kt`)

| Property | Type | Optional | Meaning |
|---|---|---|---|
| id | Int | No | Request id |
| status | String | No | Raw backend status string: `PENDING` \| `ACCEPTED` \| `REJECTED` \| `COMPLETED` (case as sent by backend, uppercased for comparisons) |
| prescriptionId | Int | No | The prescription this request is against |
| doctorName | String? | Yes | — |
| isFullPrescription | Boolean | No | true = whole prescription renewal, false = partial |
| requestedDrugNames | List\<String\> | No (can be empty) | Names explicitly requested (partial refill only) |
| prescriptionDrugNames | List\<String\> | No (can be empty) | Names from the original full prescription (full-refill requests) |
| requestedDurationDays | Int? | Yes | — |
| notes | String? | Yes | Patient's free-text note |
| reviewedByName | String? | Yes | Staff member who reviewed it |
| reviewedAt | String? | Yes | ISO datetime |
| reviewNote | String? | Yes | Staff's note back to the patient |
| createdAt | String? | Yes | — |

Derived properties (not stored, computed):
- `drugNames: List<String>` = `requestedDrugNames.ifEmpty { prescriptionDrugNames }` — "a partial-refill request names its own drugs; a full-prescription request repeats the whole original prescription" (`PrescriptionRequest.kt:18-22`).
- `isOpen: Boolean` = `status.uppercase() in {"PENDING","ACCEPTED"}` — mirrors the web portal's own definition of an "open" request; gates the renewal-pending UI.

`String.toRefillStatusLabel()` (top-level extension in the same file): `PENDING`→"Pending", `ACCEPTED`→"Approved", `REJECTED`→"Rejected", `COMPLETED`→"Completed", else → first-char-uppercased raw value.

### 6.3 Reminder domain types (`feature/health/reminder/MedicationReminderModels.kt`)

`ScheduledMedication`: `name: String`, `dosage: String?`, `frequencyPerDay: Int`, `durationDays: Int`. This is a **derived** model, not persisted from the backend — it's computed from `Drug.frequency`/`Drug.duration` free-text strings via `Drug.toScheduledMedication()`, which returns `null` (drug excluded from reminders) if name is blank or frequency/duration can't be parsed to a positive int.

**Frequency-string parser** (`String.toFrequencyPerDay()`), applied in order:
1. Dose-pattern like `"1-0-1"` or `"1+1+1"` → sums the numbers (`1-0-1` → 2 doses/day).
2. Medical abbreviations (case-insensitive substring match): `qid`/`qds`/`"four times"`→4, `tds`/`tid`/`thrice`/`"three times"`→3, `bd`/`bid`/`twice`/`"two times"`→2, `od`/`once`/`daily`→1.
3. Number words `"one".."ten"` → 1–10.
4. First numeric substring; if the string also contains `"hour"`/`"hr"`, treats the number as an interval-in-hours and computes `round(24/number)` (e.g. "every 8 hours" → 3/day); otherwise the number is the count directly.
5. Anything else / non-positive result → `null` (drug is excluded, no reminder for it).

**Duration-string parser** (`String.toDurationDays()`): first numeric substring (or number-word) × 7 if the string contains `"week"`, × 30 if it contains `"month"`, else as-is (days). `null`/non-positive → `null`.

## 7. DTOs / API MODELS

### 7.1 `PrescriptionDto` (`feature/health/data/remote/dto/HealthDtos.kt:9-18`) — `GET portal/prescriptions`

```json
[
  {
    "id": 501,
    "visitId": 900,
    "drugs": "[{\"name\":\"Paracetamol\",\"dosage\":\"500mg\",\"frequency\":\"BD\",\"duration\":\"5 days\"}]",
    "instructions": "Take after food",
    "pdfUrl": null,
    "visit": { "id": 900, "visitDate": "2026-08-20", "service": { "name": "Consultation" } },
    "doctor": { "id": 12, "name": "Dr. Rao" },
    "createdAt": "2026-08-20T10:15:00.000Z"
  }
]
```
`drugs` is annotated `@FlexibleDrugs` — the backend inconsistently sends this field as **either** a JSON-encoded string **or** a real JSON array/object depending on environment; `FlexibleDrugsAdapter` (`FlexibleDrugs.kt`) normalizes both shapes to a plain Kotlin `String` at parse time, so the mapper (`parseDrugsJson`, §11.1) always receives a string. `pdfUrl` is declared but not used anywhere downstream — the app fetches the PDF via a dedicated endpoint, not this URL.

| JSON field | DTO property | Type | Nullable |
|---|---|---|---|
| id | id | Int | No |
| visitId | visitId | Int? | Yes |
| drugs | drugs | String? (normalized) | Yes |
| instructions | instructions | String? | Yes |
| pdfUrl | pdfUrl | String? | Yes (unused) |
| visit | visit → `PrescriptionVisitDto?` | object | Yes |
| doctor | doctor → `PrescriptionDoctorDto?` | object | Yes |
| createdAt | createdAt | String? | Yes |

`PrescriptionVisitDto { id: Int, visitDate: String?, service: PrescriptionServiceDto? }`, `PrescriptionServiceDto { name: String }`, `PrescriptionDoctorDto { id: Int, name: String? }`.

### 7.2 `MyPrescriptionsResponseDto` / `MyPrescriptionDto` — `GET my-prescriptions`

```json
{ "prescriptions": [ { "id": 501, "validUntil": "2026-09-10T00:00:00.000Z" } ] }
```
Deliberately minimal: the live response has many more, inconsistently-typed fields (e.g. `drugId` is sometimes `Int`, sometimes `""`) — this DTO only declares `id` and `validUntil` because that's all the reminder depletion-alarm feature needs; Moshi silently ignores the rest.

### 7.3 `PrescriptionRequestDto` — `GET portal/prescription-requests`

```json
[
  {
    "id": 77,
    "status": "PENDING",
    "prescriptionId": 501,
    "doctorName": "Dr. Rao",
    "isFullPrescription": false,
    "requestedDrugs": [ { "name": "Paracetamol" } ],
    "requestedDurationDays": 10,
    "notes": "Ran out early",
    "reviewedByName": null,
    "reviewedAt": null,
    "reviewNote": null,
    "createdAt": "2026-08-25T09:00:00.000Z",
    "prescription": { "id": 501, "drugs": [ { "name": "Paracetamol" }, { "name": "Ibuprofen" } ] }
  }
]
```
Two different shapes for "a drug" appear in this one response — `requestedDrugs[]` (partial-refill requests, string fields) vs. `prescription.drugs[]` (full-prescription requests) — hence two DTOs, `RequestedDrugDto { name: String }` and `PrescriptionRequestDrugDto { name: String }`, both name-only. `PrescriptionRequestPrescriptionDto { id: Int, drugs: List<PrescriptionRequestDrugDto> = emptyList() }`.

**Critical wire quirk**: when the patient has zero requests, the backend returns an **empty HTTP body**, not `[]`. A strict Moshi list adapter throws on an empty string, so the endpoint is declared to return raw `ResponseBody` and `PrescriptionRequestRepositoryImpl` reads `.string()`, checks for blank, and returns `emptyList()` before ever invoking the Moshi adapter (`PrescriptionRequestRepositoryImpl.kt:30-35`).

### 7.4 `SubmitPrescriptionRequestDto` — request body for `POST portal/prescription-requests`

```json
{ "prescriptionId": 501, "medicines": ["Paracetamol"], "durationDays": 10, "notes": "Ran out early" }
```
Reverse-engineered from the live web portal's bundle (undocumented backend route). For a **full**-prescription request, `medicines` is `null` and Moshi's default writer omits it from the JSON entirely (field absent, not `"medicines": null`) — this distinction may matter to the backend and should be preserved on iOS (omit the key, don't send JSON `null`).

| Field | Type | Sent when |
|---|---|---|
| prescriptionId | Int | Always |
| medicines | List\<String\>? | Only for partial refills; omitted (not null) for full-prescription requests |
| durationDays | Int? | Only if patient entered a value |
| notes | String? | Only if patient entered text |

**Response**: also raw `ResponseBody`, unparsed — the app checks only `response.isSuccessful` (`PrescriptionRequestRepositoryImpl.kt:51`) and ignores the response payload.

## 8. API / NETWORK LAYER

All declared in `core/network/WellnessApiService.kt`. All CUSTOMER-JWT, all under the wellness base path unless noted.

| Method | Path | Purpose | Request | Response | Auth/Notes |
|---|---|---|---|---|---|
| GET | `portal/prescriptions` | List prescriptions | — | `List<PrescriptionDto>` | verifyPatientToken |
| GET | `portal/prescriptions/{id}/pdf` | Download one PDF | path `id: Int` | raw `ResponseBody` (PDF bytes) | verifyPatientToken |
| GET | `my-prescriptions` | Richer per-prescription data incl. `validUntil`, used only by the (currently unwired) "Request Meds" reminder feature | — | `MyPrescriptionsResponseDto` | verifyPatientToken |
| GET | `portal/prescription-requests` | List renewal/refill requests | — | raw `ResponseBody` (parsed defensively, see 7.3) | verifyPatientToken. **Host-dependent**: confirmed absent (404 `API_ROUTE_NOT_FOUND`) on `globuscrm.globussoft.com` (this app's actual configured `BuildConfig.BASE_URL`), confirmed present (401 on bad token = route exists) on `crm.globusdemos.com` (CLAUDE.md's documented "Production" host, which the app is not actually pointed at) — see `WellnessApiService.kt:205-211`. **Verify which host the iOS app will point at before relying on this endpoint.** |
| POST | `portal/prescription-requests` | Submit a renewal/refill request | `SubmitPrescriptionRequestDto` | raw `ResponseBody` (ignored) | verifyPatientToken, same host caveat as above |

No pagination, sorting, or filtering on any of these — full lists every time. No client-side retry logic anywhere in this stack; a failed call surfaces its error once per user-initiated action (pull-to-refresh, submit, etc.).

## 9. REPOSITORY LAYER

### `PrescriptionRepository` / `PrescriptionRepositoryImpl`

Interface (`feature/health/domain/repository/PrescriptionRepository.kt`):
```kotlin
interface PrescriptionRepository {
    suspend fun getPrescriptions(): List<Prescription>
    suspend fun getCachedPrescriptions(): List<Prescription>
    suspend fun getPrescriptionPdf(prescriptionId: Int): ByteArray
    suspend fun savePdfToCache(prescriptionId: Int, pdfBytes: ByteArray)
    suspend fun getCachedPdf(prescriptionId: Int): ByteArray?
    suspend fun evictStalePdfs()
}
```
Impl (`PrescriptionRepositoryImpl.kt`):
- `getPrescriptions()`: calls the API; on success, maps every DTO to domain **and overwrites the entire Room cache** with `dao.insertAll(...)` (`REPLACE` conflict strategy — existing PDF bytes for unchanged rows are preserved because `toEntity()` carries `pdfBytes = this.pdfBytes`, which is `null` on freshly-fetched-from-network `Prescription` objects... **note**: since list-fetched `Prescription.pdfBytes` is always `null`, every `getPrescriptions()` call actually **wipes the PDF cache** for all rows via this insertAll — see §16/§22 for the implication). Throws `HttpException` on non-2xx.
- `getCachedPrescriptions()`: straight Room read, mapped to domain, `visitDate`/`createdAt` reconstructed from the cached epoch-ms via `DateUtil.epochMsToIso`.
- `getPrescriptionPdf(id)`: raw API call, throws on failure; returns raw bytes, does **not** cache them itself (caller's job).
- `savePdfToCache`/`getCachedPdf`: read-modify-write against `PrescriptionDao`, creating a stub row if none exists yet (only when opening a PDF the list-cache doesn't know about).
- `evictStalePdfs()`: nulls `pdfBytes`/`pdfCachedAt` for rows cached >7 days ago. **Not called anywhere in the code covered by this document** — verify where app startup wires this in (likely `Application.onCreate` or Splash, outside this scope).

### `PrescriptionRequestRepository` / `PrescriptionRequestRepositoryImpl`

Interface: `getPrescriptionRequests(): List<PrescriptionRequest>`, `submitRenewalRequest(prescriptionId, medicines, durationDays, notes)`. Impl talks directly to `WellnessApiService` + a manually-built Moshi list adapter (`Types.newParameterizedType(List::class.java, PrescriptionRequestDto::class.java)`) — no local cache, no offline fallback. Both methods throw `HttpException` on non-2xx.

## 10. USE CASES / BUSINESS LOGIC

| UseCase | Input | Output | Business rule / error mapping |
|---|---|---|---|
| `GetPrescriptionsUseCase` | — | `Result<List<Prescription>>` | `HttpException` → parses `{"error": "..."}` from the error body if present, else falls back to `e.message()`, else "Server error"; code = `HTTP_{status}`. `IOException` (offline) → falls back to `repository.getCachedPrescriptions()`; if cache is non-empty, returns it as a **Success** (not flagged as stale/offline in any way visible to the UI); if cache is empty, `Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")`. Any other `Exception` → `UNEXPECTED_ERROR`. |
| `GetPrescriptionPdfUseCase` | `prescriptionId: Int` | `Result<ByteArray>` | Cache-first: checks `getCachedPdf` before ever hitting the network; only calls the API (and then caches the result) on a cache miss. No 7-day-TTL check happens here — a stale-but-present cached PDF is served without a network call at all (see §16, §22). |
| `GetPrescriptionRequestsUseCase` | — | `Result<List<PrescriptionRequest>>` | `HttpException`: 401→"Session expired. Please log in again." (`UNAUTHORIZED`), 403→"You don't have access to this. Contact your clinic." (`FORBIDDEN`), else generic. `IOException`→`NETWORK_ERROR`, no offline cache (there isn't one for this data). |
| `SubmitPrescriptionRenewalUseCase` | `prescriptionId, medicines: List<String>?, durationDays: Int?, notes: String?` | `Result<Unit>` | Same error-code mapping as above. No client-side validation here — that lives in the dialog Composable (§4). |

## 11. MAPPERS / TRANSFORMATIONS

All in `feature/health/data/mapper/HealthMappers.kt`.

### 11.1 `PrescriptionDto.toDomain()` and the drug-JSON parser

| DTO field | Domain field | Transform |
|---|---|---|
| id | id | direct |
| visitId | visitId | direct |
| visit?.visitDate | visitDate | direct |
| createdAt | createdAt | direct |
| instructions | instructions | direct |
| doctor?.name | doctorName | direct |
| visit?.service?.name | serviceName | direct |
| drugs (string) | drugs (List\<Drug\>) | `parseDrugsJson(drugs)` — see below |

`parseDrugsJson(json: String?)`: wrapped in `runCatching { ... }.getOrDefault(emptyList())` — **any** parse exception silently yields an empty drug list rather than propagating an error. Logic:
1. Trim; blank or literal `"null"` → `emptyList()`.
2. Starts with `[` → parse as `JSONArray`, map each element through `toDrugOrNull()`.
3. Starts with `{` → parse as `JSONObject`; look for a nested array under `"drugs"`, `"medications"`, `"items"`, or `"data"` (first match wins) and recurse into that array; if none found, try to parse the object itself as a single drug.
4. Anything else → `emptyList()`.

`JSONObject.toDrugOrNull()`: extremely defensive key-hunting to cope with inconsistent backend field naming, including one level of nesting under `"drug"`/`"medicine"`/`"medication"`/`"product"`:
- **name**: first present of `name`, `drugName`, `medicineName`, `medicationName`, `title`, `label` (own object), else same keys on the nested object. `null` name → drug is dropped entirely (`return null`).
- **frequency**: first present of `frequency`, `frequencyPerDay`, `dailyFrequency`, `timesPerDay`, `times`, `perDay`, `noOfTimes`, `numberOfTimes` (own), else a narrower set on the nested object.
- **duration**: `duration`, `durationDays`, `days`, `noOfDays`, `numberOfDays`, `courseDuration` (own), else narrower set nested.
- **dosage**: `dosage`, `dose`, `dosageValue`, `quantity`, `strength` (own or nested), else falls back to a computed `"{strengthValue} {strengthUnit}"` string (own, then nested) if a strength value exists.
- **instructions**: `instructions`, `instruction`, `notes`, `remarks` (own or nested).
- A key counts as "present" only if it exists, is not JSON null, and its string form is non-blank and not the literal text `"null"`.

### 11.2 Other health mappers

- `CachedPrescription.toDomain()`: `instructions=null`, `drugs=emptyList()` always (the list cache doesn't store either) — **only the PDF cache round-trips drug data-free**; anything reading `drugs`/`instructions` off a cache-sourced `Prescription` gets nothing.
- `Prescription.toEntity()`: `drugCount = drugs.size` (count only, not the drugs themselves — corroborates the above), `cachedAt = System.currentTimeMillis()`.
- `PrescriptionRequestDto.toDomain()`: `requestedDrugNames = requestedDrugs?.map{it.name} ?: emptyList()`, `prescriptionDrugNames = prescription?.drugs?.map{it.name} ?: emptyList()`, everything else direct.

## 12. REMINDER SUBSYSTEM (Android-native — needs an iOS-native equivalent design, not a port)

This entire subsystem has **no backend counterpart** — it's 100% on-device scheduling built on `AlarmManager` + `BroadcastReceiver` + Room. An iOS implementation will need `UNUserNotificationCenter` + local (not push) notifications with equivalent trigger dates; there is no 1:1 API to port, only the business logic below.

### 12.1 `PrescriptionReminderRepository` (`feature/health/reminder/PrescriptionReminderRepository.kt`)

| Method | Logic |
|---|---|
| `enabledReminderIds(): Flow<Set<Int>>` | Streams `PrescriptionReminderDao.getAllAsFlow()`, filters to rows where `endAt > now`, maps to `prescriptionId` set. Drives the reminder-switch UI. |
| `enable(prescription)` | Maps `prescription.drugs` through `toScheduledMedication()`, dropping unparseable ones. If **all** drugs are unparseable → returns a failure message "This prescription does not include frequency and duration details." without writing anything. Else: `startAt = max(createdAt-as-epoch, now)`; `endAt = startAt + (max drug durationDays across all drugs) × 24h`; upserts a `PrescriptionReminderEntity`; calls `scheduler.schedule(reminder)`. Returns success message "Medication and refill reminders enabled." if exact alarms are allowed, else "Reminders enabled with approximate timing. Allow exact alarms for best accuracy." and sets `exactAlarmPermissionRequired=true`. |
| `disable(prescriptionId)` | Looks up the entity, cancels its alarms via the scheduler, deletes the Room row. Always "succeeds" (message "Medication reminders disabled."). |
| `cleanupExpired()` | Deletes (and cancels alarms for) every row where `endAt <= now`. Called once on `PrescriptionsViewModel` init before the reminder-ids flow is even collected, and also at the start of `rescheduleActiveReminders()`. |
| `rescheduleActiveReminders()` | `cleanupExpired()` then re-`schedule()`s every remaining row. Called only from `MedicationReminderRescheduleReceiver` (boot / app-update / timezone-change). |
| `requestMeds(prescriptions)` | **Declared but never called from any UI/ViewModel in this codebase** (confirmed via full-repo grep — see §22/23). Would call `GET my-prescriptions`, build a `validUntil`-per-id map, and for every prescription with a resolvable `validUntil`, upsert/update its reminder row's `validUntilAt` and reschedule. If wired up, this is meant to be the trusted source for the depletion alarm's trigger date instead of the locally-estimated `endAt`. |

### 12.2 `MedicationReminderScheduler` (`feature/health/reminder/MedicationReminderScheduler.kt`)

- `canScheduleExactAlarms()`: `true` below API 31, else `AlarmManager.canScheduleExactAlarms()`.
- `schedule(reminder)`: for each drug in `reminder.drugsJson` (parsed via `parseReminderDrugs`), computes `intervalMs = 24h / frequencyPerDay` and walks `triggerAt` from `reminder.startAt` in steps of `intervalMs` until it passes either `drugEndAt` (= `startAt + durationDays×24h`) or `reminder.endAt` (whichever is sooner), scheduling one alarm per step that's still in the future. Then calls `scheduleDepletionAlarm` once per drug (see below). Wrapped in `try/catch(SecurityException)` → returns `false` (treated as "exact alarms not allowed") if the OS refuses.
  - Alarm-setting strategy (`scheduleAlarm`), in priority order: API ≥ 31 and exact **not** allowed → `setAndAllowWhileIdle` (inexact, Doze-aware). API ≥ 21 (Lollipop) → `setAlarmClock(AlarmClockInfo(triggerAt, tapIntent), pendingIntent)` — this is the actual path taken on essentially all supported devices (minSdk 26), and it shows a persistent "alarm" indicator to the user and is Doze-exempt by OS design. The `AlarmClockInfo`'s associated "tap to view" intent deep-links to `prescription_pdf?id={prescriptionId}`. (Lower-API branches — `setExactAndAllowWhileIdle`, `setExact`, `setAndAllowWhileIdle`, plain `set` — exist for completeness but are unreachable given minSdk 26.)
  - **`BuildConfig.DEBUG`-only behavior**: schedules one extra test dose alarm 60s out and one extra test depletion alarm 90s out on every `schedule()` call, using reserved index/sequence constants (999/999 and 998/998) so they don't collide with real alarms. **Must not be ported to production iOS builds** — this is a debug-build convenience only.
- `cancel(reminder)`: re-derives and cancels every `PendingIntent` the same way `schedule()` built them (alarms are matched by their `PendingIntent` request code, not stored anywhere), plus the debug-alarm slots.
- `scheduleDepletionAlarm`: fires once, `DEPLETION_REMINDER_LEAD_DAYS = 3` days before `reminder.validUntilAt ?: reminder.endAt` (i.e. prefers the backend-confirmed date if present, else the locally-estimated course end). Skipped if that trigger time is already in the past.
- `requestCodeFor(prescriptionId, drugIndex, sequence) = prescriptionId*100_000 + drugIndex*1_000 + sequence` — the collision-avoidance scheme for `PendingIntent` request codes across potentially many alarms per prescription.

### 12.3 `MedicationReminderReceiver` (`feature/health/reminder/MedicationReminderReceiver.kt`)

- Dose alarms: if `System.currentTimeMillis() > endAt` (course already over — e.g. a very delayed device wake), silently calls `reminderRepository.disable(prescriptionId)` via `goAsync()` instead of showing anything. Otherwise builds and shows a notification: title "Time for your medication", short text `"Take {drug} ({dosage} unit/s)."`, expanded `BigTextStyle` with a fuller sentence, `PRIORITY_HIGH`, `CATEGORY_REMINDER`, auto-cancel, tap → deep link to `prescription_pdf?id={id}`. Notification id = `"medication-{prescriptionId}-{drugName}".hashCode()` (so re-firing the same drug/prescription combo replaces rather than stacks).
- Depletion alarms: title "Medicine running low", text `"Your supply of {drug} is running low."`, expanded text names the expected run-out date (`DateUtil.toDisplayDate(depletionAt)` or "soon" if unknown) and prompts arranging a refill. Notification id = `"depletion-{prescriptionId}-{drugName}".hashCode()`.
- Both check `POST_NOTIFICATIONS` (API ≥ 33) before calling `NotificationManagerCompat.notify` and silently no-op if not granted — **no in-app fallback UI when permission is denied**.
- Channel: `wellness_medication_reminders` ("Medication Reminders"), `IMPORTANCE_HIGH`, vibration on — created lazily by the receiver itself (`ensureMedicationReminderChannel`) in addition to `MainActivity`'s own channel-creation pass, so it's guaranteed to exist even if the receiver fires before `MainActivity` ever launched (e.g. after a reboot).

### 12.4 `MedicationReminderRescheduleReceiver`

Listens for `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `MY_TIMEZONE_CHANGED` (manifest-declared intent filter, `AndroidManifest.xml:69-76`) and calls `reminderRepository.rescheduleActiveReminders()` via `goAsync()`. Necessary because `AlarmManager` alarms do not survive a reboot and can be invalidated by a timezone change (all trigger times are absolute `RTC` epoch-ms, computed once at `enable()`-time in the device's then-current zone).

### 12.5 Room schema

`PrescriptionReminderEntity` (table `prescription_reminders`): `prescriptionId: Int` (PK), `enabledAt: Long`, `startAt: Long`, `endAt: Long`, `prescriptionLabel: String?`, `drugsJson: String` (JSON-serialized `List<ScheduledMedication>`), `validUntilAt: Long?` (null until "Request Meds" sets it — currently never, per §12.1).

`PrescriptionReminderDao`: `getAllAsFlow()`, `getAll()`, `getByPrescriptionId(id)`, `upsert` (REPLACE), `deleteByPrescriptionId`, `delete(entity)`, `deleteExpired(now)` (declared but — note — `PrescriptionReminderRepository.cleanupExpired()` actually does a `getAll().filter{}.forEach{ cancel; delete }` loop rather than calling this one-shot SQL method; both exist, only the loop form is used in the traced code path).

## 13. NAVIGATION

| From | To | Trigger | Params | Mechanism |
|---|---|---|---|---|
| Dashboard | Prescriptions list | Quick-action tap | — | `navController.navigate(Screen.Prescriptions.route)` |
| Dashboard | Prescription Requests | Quick-action tap | — | Direct navigate, bypasses the list |
| Prescriptions list | PDF viewer | Confirm dialog "Open" | `prescriptionId` (path segment) | `Screen.PrescriptionPdf.createRoute(id)` = `"prescription_pdf/{id}"` |
| Prescriptions list | Treatment Analysis | "Before/After Scan" tap | `prescriptionId`, `visitId` (query, may be absent) | `Screen.PrescriptionAnalysis.createRoute(...)` — out of scope |
| Prescriptions list | Request history | "My Prescription Requests" tap | — | `Screen.PrescriptionRequests.route` |
| Any screen (external) | Prescriptions list | Deep link | — | `globuscrm://screen/prescriptions` |
| Medication reminder notification tap | PDF viewer | System notification tap | `prescriptionId` | `globuscrm://screen/prescription_pdf?id={id}`, `FLAG_ACTIVITY_NEW_TASK/CLEAR_TOP/SINGLE_TOP` |
| Alarm-clock "tap to view" (the OS alarm-clock UI, not the notification itself) | PDF viewer | Same deep link, separate `PendingIntent` slot required by `setAlarmClock`'s API | `prescriptionId` | Same URI |
| PDF viewer / Request history | back | Back nav event | — | `navController.popBackStack()` |

## 14. DEPENDENCY INJECTION

Covered in Section 0.2. Feature-specific bindings: `PrescriptionRepository → PrescriptionRepositoryImpl`, `PrescriptionRequestRepository → PrescriptionRequestRepositoryImpl` (`core/di/RepositoryModule.kt:57,60-62`). `PrescriptionReminderRepository`, `MedicationReminderScheduler` are `@Singleton @Inject constructor` classes with no separate `@Provides`/`@Binds` needed (concrete classes, Hilt constructor-injects them directly). `PrescriptionReminderDao`/`PrescriptionDao` come from `DatabaseModule`'s `AppDatabase` provider.

## 15. STATE & BUSINESS FLOW

```
Open Prescriptions screen
    ↓
init: observeReminderState() + load() + loadOpenRenewalRequests() (concurrent)
    ↓
load(): check my_prescriptions.read permission
    ├─ missing → permissionBlocked=true → ErrorState (terminal until manual Refresh)
    └─ present → GetPrescriptionsUseCase()
                    ├─ Success → prescriptions shown
                    ├─ Error (HTTP) → ErrorState + Retry
                    └─ Error (offline, cache empty) → ErrorState "No internet..."
                       (offline + cache non-empty is NOT an error state — see UseCase table, §10)
    ↓
User toggles Reminder switch
    ↓
toggleReminder() → reminderRepository.enable/disable → Room write + AlarmManager schedule/cancel
    → enabledReminderIds Flow re-emits → switch UI updates reactively (not from the toggle call's own return value)
    ↓
User taps "Request a renewal" (only visible if prescription.id NOT in openRenewalPrescriptionIds)
    ↓
Dialog collects full/partial + medicines + duration + notes → client validation
    ↓
submitRenewal() → SubmitPrescriptionRenewalUseCase
    ├─ Success → dialog closes, toast "Renewal request sent.", loadOpenRenewalRequests() reruns
    │              → that prescription's card flips to "Renewal pending" once the new request
    │                comes back with status PENDING
    └─ Error → dialog stays open, toast shows the error, user can retry
    ↓
Screen resumes later (e.g. back from PDF viewer) → ON_RESUME → Refresh → loadOpenRenewalRequests() reruns
    (only way status changes are ever observed — no push)
```

## 16. ERROR HANDLING

| Origin | Propagation | UI result | Retry? |
|---|---|---|---|
| `GET portal/prescriptions` non-2xx | `HttpException` → `GetPrescriptionsUseCase` → `Result.Error` w/ backend `error` field if parseable | Full-screen `ErrorState` | Yes, `onRetry` re-sends `Refresh` |
| `GET portal/prescriptions` offline, cache hit | `IOException` caught, cache returned as `Result.Success` | List shows (silently stale — no "offline"/"cached" indicator in the UI) | N/A |
| `GET portal/prescriptions` offline, cache empty | `IOException` → `Result.Error("NETWORK_ERROR", ...)` | Full-screen `ErrorState` "No internet connection. Please try again." | Yes |
| `my_prescriptions.read` missing | Checked before any prescriptions call | Full-screen `ErrorState` "Prescription access is not enabled..." | Retry re-checks permission (won't self-resolve without a role change server-side) |
| `GET .../pdf` failure | `HttpException`/`IOException` → `Result.Error` | Centered `CloudOff` icon + message, no retry button on this screen | **No** — user must navigate back and try opening the PDF again |
| `GET/POST prescription-requests` failure | Same pattern, 401/403 get specific copy | `ErrorState` (list screen) or inline toast (submit) | Yes for list; retry = re-tap "Send request" for submit |
| Renewal submit validation failure (empty medicine selection) | Pure client-side, no network call | Inline red text under the fields | Immediate, no round-trip |
| Reminder enable on a drug-less prescription | `PrescriptionReminderRepository.enable` short-circuits | Toast "This prescription does not include frequency and duration details." | Switch reverts (state never flips to enabled) |
| Exact-alarm permission denied | Detected via `!scheduler.canScheduleExactAlarms()` after `schedule()` returns `false` | Toast "Reminders enabled with approximate timing..." + a one-shot Settings-app launch prompt | Reminders still function, just less precisely timed |

No logging/analytics of any of these errors is visible in the traced code (no Sentry/analytics calls in this feature's files).

## 17. LOCAL STORAGE / CACHE

- **Room, table `cached_prescriptions`** (`CachedPrescription`): full-list cache (offline fallback) **and** per-prescription PDF byte cache, same table/entity. `PrescriptionDao.insertAll` on every successful list fetch **replaces all rows**, which — because list-derived `Prescription` objects always have `pdfBytes=null` — has the side effect of nulling out every cached PDF's bytes on the next pull-to-refresh (`toEntity()` carries whatever `pdfBytes` the in-memory `Prescription` has, and list-fetched ones never have any). **This is a real, current behavior, not a hypothetical** — an iOS port should decide deliberately whether to preserve or intentionally fix this quirk; flagged again in §22.
- 7-day PDF eviction (`evictStalePdfs`) exists in the repository but its call site was not found in the files covered by this document — verify where it's invoked (likely app startup) before assuming it actually runs.
- **Room, table `prescription_reminders`** (`PrescriptionReminderEntity`): one row per prescription with an active reminder; source of truth for what's currently scheduled (alarms themselves are stateless OS registrations, not queryable — the Room table is the only place "what's on" is recorded).
- No DataStore/SharedPreferences usage in this feature.
- Offline behavior summary: prescriptions list falls back to cache; PDF viewing falls back to cache (and is in fact cache-**first**, network-second, unconditionally); renewal requests (list + submit) have **no offline support** at all.

## 18. PERMISSIONS / DEVICE FEATURES

- **`POST_NOTIFICATIONS`** (manifest-declared, `AndroidManifest.xml:6`): required (API 33+) for both the medication-reminder notifications and depletion notifications to actually display; requested app-wide by `MainActivity`, not by this feature specifically (see Section "Notifications" Part 3, §18 there). If denied, `MedicationReminderReceiver` silently no-ops — reminders are still scheduled and "fire" (the alarm triggers, the receiver runs) but nothing is shown.
- **`SCHEDULE_EXACT_ALARM`** (manifest-declared, `AndroidManifest.xml:13`) + **`RECEIVE_BOOT_COMPLETED`** (`AndroidManifest.xml:12`): exact-alarm permission is optional at runtime (checked via `AlarmManager.canScheduleExactAlarms()`); when absent the app falls back to inexact `setAndAllowWhileIdle` scheduling and prompts the user once via a one-shot Settings-app launch (not a repeating nag).
- No camera/location/bluetooth/biometric permissions in this feature (Treatment Analysis's CameraX permission is a separate, out-of-scope feature).

## 19. SIDE EFFECTS

- Local (non-push) system notifications via `AlarmManager` + `NotificationManagerCompat`, on channel `wellness_medication_reminders`.
- PDF file writes: a temp file per PDF-view (`context.cacheDir`, deleted immediately after rendering) and, on explicit user tap of the download FAB, a permanent file in the public Downloads folder (or via `MediaStore` on API 29+).
- No analytics/logging calls found in this feature.
- No background work beyond `AlarmManager` (no WorkManager usage in this feature).

## 20. COMPLETE END-TO-END DATA FLOW EXAMPLE

**User taps "Request a renewal", picks 1 medicine, submits.**

```
RenewalRequestDialog (Compose, local state: fullPrescription=false, selectedMedicines={"Paracetamol"})
    ↓ user taps "Send request" → client validation passes (set non-empty)
    ↓ onSubmit(false, {"Paracetamol"}, null, null)
PrescriptionsScreen: onEvent(PrescriptionsUiEvent.SubmitRenewal(false, {"Paracetamol"}, null, null))
    ↓
PrescriptionsViewModel.onEvent → submitRenewal(fullPrescription=false, {"Paracetamol"}, null, null)
    ↓ reads renewalDialogPrescription.id = 501 from current state
    ↓ sets renewalSubmitInProgress=true
    ↓
SubmitPrescriptionRenewalUseCase(prescriptionId=501, medicines=["Paracetamol"], durationDays=null, notes=null)
    ↓
PrescriptionRequestRepositoryImpl.submitRenewalRequest(501, ["Paracetamol"], null, null)
    ↓ builds SubmitPrescriptionRequestDto(prescriptionId=501, medicines=["Paracetamol"], durationDays=null, notes=null)
    ↓
WellnessApiService.submitPrescriptionRequest(dto)  →  POST portal/prescription-requests
    ↓ backend 200 OK (body ignored)
Repository returns Unit (no exception)
    ↓
UseCase returns Result.Success(Unit)
    ↓
ViewModel: renewalSubmitInProgress=false, renewalDialogPrescription=null (dialog closes),
           renewalMessage="Renewal request sent." (toast fires)
           → loadOpenRenewalRequests() re-runs
    ↓
GetPrescriptionRequestsUseCase → GET portal/prescription-requests → now includes the new PENDING request
    ↓
openRenewalPrescriptionIds now contains 501
    ↓
PrescriptionsScreen recomposes: prescription 501's card now shows the "Renewal pending" chip
    instead of the "Request a renewal" button
```

## 21. iOS IMPLEMENTATION REFERENCE (conceptual mapping only — see Part 4 for the shared table)

See the combined mapping table at the end of this document (Section "iOS Mapping — All Features"). Feature-specific note: the reminder subsystem has no Android-API-to-iOS-API 1:1 mapping (`AlarmManager`/`BroadcastReceiver` → `UNUserNotificationCenter` local notifications is a re-implementation of the *business logic* in §12, not a port of any class).

## 22. IMPLEMENTATION RULES / NON-OBVIOUS BEHAVIOR (Prescriptions)

- **List refresh silently wipes the PDF cache.** See §17 — every successful `getPrescriptions()` call replaces all Room rows via objects whose `pdfBytes` is always `null`. Decide deliberately whether iOS should replicate this or store the list cache and PDF cache as separate tables (the latter is very likely the *intended* behavior, given `evictStalePdfs` exists as if PDFs were meant to persist across list refreshes).
- **`GetPrescriptionPdfUseCase` is cache-first, unconditionally** — it never re-checks the network once any cached bytes exist for that id, regardless of age. The 7-day eviction is the only thing that would ever force a re-fetch, and its call site wasn't found in this feature's files (see §17) — verify before assuming PDFs ever actually refresh.
- **`requestMeds()` exists but is dead code** — confirmed by a full-repository grep of `requestMeds`/`getMyPrescriptions` call sites (only found inside `PrescriptionReminderRepository.kt` itself and the `WellnessApiService` declaration). This means the depletion-reminder alarm currently **always** uses the locally-estimated `endAt` (drug duration math), never the backend's authoritative `validUntil`, in the shipped app. If the iOS app is expected to have a working "Request Meds" action, that's new functionality relative to what Android currently ships, not a port.
- **Empty-body quirk on `GET portal/prescription-requests`**: must be parsed leniently (empty string ⇒ empty list), not assumed to always be a JSON array.
- **`medicines` field omission, not null, for full-prescription renewal submissions** — see §7.4. Matches the default behavior of most JSON encoders when a field is `nil`/absent in the request struct (e.g. Swift's `JSONEncoder` with `Encodable` `nil` for an `Optional` typically encodes `null`, not omits the key — this must be handled explicitly, e.g. via a custom `encode(to:)` or by using a dictionary-based request body).
- **`Prescription.equals()`/`hashCode()` only compare `id`+`visitId`**, not `drugs`/`instructions`/etc. — affects Compose list diffing on Android; on iOS, if using `Identifiable`/`Equatable`-driven diffing (e.g. `List` with `.id`), decide whether to replicate this narrow equality or use full structural equality — the Android choice means a changed `instructions` string on an otherwise-identical prescription would **not** trigger a `LazyColumn` item recomposition keyed only by `id` in some diffing paths, though in practice the screens here key by `.id` explicitly for `LazyColumn` `items(...)`, so this mostly matters for `Set`/`distinctBy` usage rather than list rendering.
- **Drug-JSON parsing is maximally defensive and silently degrades to "no drugs shown"** on any parse failure — there is no error surfaced to the user when drug parsing fails; the prescription just shows "0 medications" and the reminder switch becomes non-interactive. Preserve this fail-soft behavior; do not throw/crash on malformed drug JSON.
- **Renewal-pending status is polled, not pushed** — refetched on `ON_RESUME`, not via any real-time channel (there is none — see Part 3, §"Real-time delivery"). An iOS `onAppear`/scenePhase-active equivalent should replicate this poll-on-resume behavior.
- **Debug-build test alarms** (§12.2) must not ship in a release iOS build if ported at all — they exist purely for on-device QA of the Android build.
- **`setAlarmClock` behavior has no iOS analogue** — it's what makes the "tap to view" launch intent exist as a second `PendingIntent` per alarm; on iOS, a `UNNotificationRequest`'s own tap action is sufficient and there's no separate "alarm clock" OS surface to feed.

## 23. UNKNOWN / AMBIGUOUS AREAS (Prescriptions)

- **Which host `portal/prescription-requests` actually resolves against for this app's real users** — the code comment (`WellnessApiService.kt:205-211`) documents a discrepancy between the app's configured `BuildConfig.BASE_URL` (`globuscrm.globussoft.com`, where the route reportedly 404s) and `crm.globusdemos.com` (where it reportedly works) as of the comment's writing. **Verify live against whichever backend the iOS app will actually point at** before building against this contract.
- **Where `evictStalePdfs()` is invoked** — not found within `feature/health/`; likely `Application.onCreate()`, Splash, or a startup Worker. Verify in `app/` module root files before assuming the 7-day TTL is enforced anywhere.
- **`pdfUrl` field on `PrescriptionDto`** — declared, populated by the backend, never read by the app (PDF is always fetched via the dedicated endpoint). Unknown whether this is meant as a future direct-link mechanism or is simply unused; treat as informational only.
- **Behavior of `requestMeds()` if it were wired up** — since it's currently unreachable, its correctness (e.g. whether `validUntilByPrescriptionId` matching by raw prescription id is reliable) has not been exercised in production. Do not port it as "known-good" without separate verification.

---

# PART 2 — PACKAGES (Catalog tab)

## 1. FEATURE OVERVIEW

**What it does.** A read-only storefront listing service **packages** (bundles of N sessions of
one or more services, sold at a discount) — separate from the individually-priced Services tab
and from Memberships (a different backend concept covered elsewhere in the app). Patients can
browse packages, see what's included, and see their own ownership/progress if they already
purchased one. **Purchasing is not implemented** — tapping "Buy package" shows a
"Package purchase is coming soon." toast and nothing else happens.

**Entry point.** Packages is not a standalone route — it is **tab index 1** of a 4-tab
`ScrollableTabRow` inside `CatalogTabScreen` (route `Screen.CatalogTab.route = "tab_catalog"`),
alongside Services (0), Categories (2), and Memberships (3, a different feature's Composable
embedded here). There is no deep link or dashboard shortcut directly to the Packages tab found
in this codebase — reaching it always means opening the Catalog tab (bottom nav, presumably)
and then tapping "Packages".

**Screen.** `feature/catalog/presentation/screen/CatalogTabScreen.kt`, function
`ServicePackagesContent` (private, only reachable from `CatalogTabScreen`).

**User actions:**
- Pull-to-refresh (re-fetches packages only, independent of the Services/Categories tabs).
- Tap "Buy package" on an unowned package → shows the not-yet-implemented toast.
- (No tap target for an owned package's progress bar — it's display-only.)

**States:**
- Loading: `PullToRefreshBox`'s own spinner (`isRefreshing = state.packagesLoading`), no separate full-screen spinner.
- Error: centered `ErrorState` with message + Retry (re-fires `LoadPackages`).
- Empty: centered `EmptyState` "No packages found." with an `Inventory2` icon.
- Success: `LazyColumn` of `PackageCard`s, 16dp padding, 12dp spacing.

**Permissions involved:** None beyond the standard portal JWT — no client-side RBAC permission gate on this endpoint (unlike Prescriptions' `my_prescriptions.read` check). `GetPackagesUseCase` does not call `GetPatientPermissionsUseCase` at all.

**Conditional behavior:**
- A package shows either a `Button("Buy package")` (unowned) or a progress display (`ownedPlan != null`) — never both.
- The "Live"/"Inactive" status pill reflects `isActive`, independent of ownership.
- Discount/tax labels only render if their respective percent is `> 0`.
- The "Sell by / valid N days" row only renders if at least one of `sellByDate`/`validityDays` is present.
- The strikethrough original-price text only renders if `grossPrice != null && grossPrice > price`.

## 2. ARCHITECTURE

Standard shared pattern (Section 0). `CatalogViewModel` owns three independent load functions
(`loadServices`, `loadCategories`, `loadPackages`) against one `CatalogRepository`, all
triggered together in `init {}` and independently re-triggerable per-tab. Packages has its own
loading/error fields on the shared `CatalogUiState` (`packagesLoading`, `packagesError`,
`packages`) so a Packages-tab refresh does not disturb Services-tab state and vice versa.

```
CatalogTabScreen (tab index 1) ↔ CatalogViewModel
    └─ GetPackagesUseCase → CatalogRepository → WellnessApiService.getServicePackages()
```

## 3. COMPLETE FILE INVENTORY

| Layer | File | Responsibility | Depends on |
|---|---|---|---|
| Presentation | `feature/catalog/presentation/screen/CatalogTabScreen.kt` | All 4 tabs' UI, incl. `ServicePackagesContent`/`PackageCard` | `CatalogUiState/UiEvent` |
| Presentation | `feature/catalog/presentation/viewmodel/CatalogViewModel.kt` | Loads services/categories/**packages**, handles `BuyPackage` (stub) | `GetServicesUseCase`, `GetCategoriesUseCase`, `GetPackagesUseCase` |
| Presentation | `feature/catalog/presentation/state/CatalogState.kt` | `CatalogUiState`/`CatalogUiEvent` (shared across all 4 tabs) | — |
| Domain | `feature/catalog/domain/model/CatalogModels.kt` | `Service`, `ServiceCategory`, `ServicePackage`, `OwnedServicePlan` | — |
| Domain | `feature/catalog/domain/repository/CatalogRepository.kt` | Interface: `getServices/getCategories/getPackages` | — |
| Domain | `feature/catalog/domain/usecase/GetPackagesUseCase.kt` | Fetch packages, wrap errors | `CatalogRepository` |
| Data | `feature/catalog/data/remote/dto/CatalogDtos.kt` | `ServicePackagesResponseDto`, `ServicePackageDto`, `PackageServiceDto`, `OwnedServicePlanDto` (+ Service/Category DTOs, other tabs) | Moshi |
| Data | `feature/catalog/data/mapper/CatalogMappers.kt` | `ServicePackageDto.toDomain()`, `OwnedServicePlanDto.toDomain()` | — |
| Data | `feature/catalog/data/repository/CatalogRepositoryImpl.kt` | `getPackages()` → API call, unwraps `.packages`, maps to domain | `WellnessApiService` |
| Shared | `core/network/WellnessApiService.kt` | `getServicePackages()` declaration | Retrofit |
| Shared | `core/util/CurrencyUtil.kt` | `formatRupees()` — INR currency formatting for price display | — |

No local cache/Room entity for packages — every load is a live network call, and there is no
offline fallback (an `IOException` is a hard error, not a cache-fallback path, unlike
Prescriptions).

## 4. PRESENTATION / UI

`ServicePackagesContent` (`CatalogTabScreen.kt:280-328`): `PullToRefreshBox` wrapping the
loading/error/empty/list `when`, described above. Also hosts a `LaunchedEffect` that watches
`state.packageActionMessage` and fires a `Toast` then dismisses it — the same one-shot-toast
pattern used elsewhere in the app.

**`PackageCard`** (`CatalogTabScreen.kt:332-508`, `WellnessCard`, 16dp padding, 10dp vertical spacing):
1. Header row: package `name` (titleMedium, bold, weight 1f) + trailing status pill: `Surface` "Live" (primaryContainer) or "Inactive" (surfaceVariant), based on `isActive`.
2. `description` text (bodySmall, onSurfaceVariant) — only if non-blank.
3. `serviceNames` as a `FlowRow` of `SuggestionChip`s (one per included service name) — only if non-empty.
4. A `FlowRow` of small stat texts: `"{sessions} sessions"` always; `"· {discountPercent}% off"` in primary/semibold if `discountPercent > 0`; `"· +{taxPercent}% tax"` if `taxPercent > 0`.
5. Validity row (icon `CalendarMonth` + text) — only rendered if `sellByDate` non-blank or `validityDays != null`: builds `"Sell by {DateUtil.toDisplayDate(sellByDate)}"` and/or `"valid {validityDays} days after purchase"`, joined with `" · "` if both present.
6. Price row: `CurrencyUtil.formatRupees(price)` (titleLarge, bold, primary) +, if `grossPrice != null && grossPrice > price`, a strikethrough `CurrencyUtil.formatRupees(grossPrice)` next to it.
7. Footer — mutually exclusive:
   - **Owned** (`ownedPlan != null`): "You own this package" (semibold) + `"{completedSessions}/{totalSessions} sessions used"` on the same row, then a `LinearProgressIndicator` with `progress = completedSessions/totalSessions` (0 if `totalSessions <= 0`, coerced to `[0,1]`).
   - **Not owned**: full-width `Button("Buy package")` with a `ShoppingBag` icon → `onEvent(BuyPackage(id))`.

No package-detail screen/bottom-sheet exists — unlike Services (which has `ServiceDetailSheet`), tapping a `PackageCard` itself does nothing; only the "Buy package" button is interactive.

## 5. VIEWMODEL / PRESENTATION LOGIC

`CatalogViewModel` (`feature/catalog/presentation/viewmodel/CatalogViewModel.kt`), packages-relevant surface:

| Method | Params | Called by | Logic | State changes |
|---|---|---|---|---|
| `loadPackages()` | — | `init`, `LoadPackages` event | Sets `packagesLoading=true, packagesError=null`; calls `GetPackagesUseCase()` | `Success` → `packagesLoading=false, packages=data`. `Error` → `packagesLoading=false, packagesError=message` |
| `onEvent(BuyPackage(packageId))` | `Int` (unused beyond the event payload — no call made with it) | "Buy package" tap | Sets `packageActionMessage = "Package purchase is coming soon."` | Toast fires via the Composable's `LaunchedEffect`, then `DismissPackageMessage` clears it |
| `onEvent(DismissPackageMessage)` | — | Toast's `LaunchedEffect` | Clears `packageActionMessage` | — |

`init {}` calls `loadServices()`, `loadCategories()`, `loadPackages()` together — all three tabs' data loads eagerly when the Catalog tab screen is first composed, not lazily per-tab-selection.

## 6. DOMAIN MODELS

### `ServicePackage` (`feature/catalog/domain/model/CatalogModels.kt:24-39`)

| Property | Type | Optional | Meaning |
|---|---|---|---|
| id | Int | No | Package id |
| name | String | No | Display name |
| description | String? | Yes | — |
| sessions | Int | No | Total sessions included |
| discountPercent | Int | No, default 0 if DTO sent null | — |
| grossPrice | Double? | Yes | Pre-discount/original price, for strikethrough display |
| price | Double | No | Actual sale price |
| currency | String | No | e.g. `"INR"` (not itself used for formatting — `CurrencyUtil.formatRupees` hardcodes ₹ formatting regardless of this field; see §22) |
| taxPercent | Int | No, default 0 if DTO sent null | — |
| validityDays | Int? | Yes | Days the package stays valid after purchase |
| sellByDate | String? | Yes | ISO date after which the package can no longer be bought |
| isActive | Boolean | No | Live/Inactive pill |
| serviceNames | List\<String\> | No (can be empty) | Flattened from `services[].name` |
| ownedPlan | OwnedServicePlan? | Yes | Present only if the current patient already owns this package |

### `OwnedServicePlan`

| Property | Type | Optional | Meaning |
|---|---|---|---|
| status | String | No | Raw backend status string (not enumerated/validated client-side — no known value set from this code alone) |
| totalSessions | Int | No | Sessions granted by the purchase |
| completedSessions | Int | No | Sessions used so far |

## 7. DTOs / API MODELS

### `ServicePackagesResponseDto` / `ServicePackageDto` (`feature/catalog/data/remote/dto/CatalogDtos.kt:45-82`) — `GET packages`

```json
{
  "packages": [
    {
      "id": 12,
      "name": "Glow Package",
      "description": "5-session facial glow bundle",
      "sessions": 5,
      "discountPercent": 15,
      "grossPrice": 12000.0,
      "price": 10200.0,
      "currency": "INR",
      "taxPercent": 5,
      "validityDays": 180,
      "sellByDate": "2026-12-31",
      "isActive": true,
      "services": [ { "id": 3, "name": "Facial" }, { "id": 7, "name": "Deep Cleanse" } ],
      "ownedPlan": { "status": "active", "totalSessions": 5, "completedSessions": 2 }
    }
  ]
}
```

| JSON field | DTO property | Type | Nullable |
|---|---|---|---|
| id | id | Int | No |
| name | name | String | No |
| description | description | String? | Yes |
| sessions | sessions | Int | No |
| discountPercent | discountPercent | Int? | Yes → domain default 0 |
| grossPrice | grossPrice | Double? | Yes |
| price | price | Double | No |
| currency | currency | String | No |
| taxPercent | taxPercent | Int? | Yes → domain default 0 |
| validityDays | validityDays | Int? | Yes |
| sellByDate | sellByDate | String? | Yes |
| isActive | isActive | Boolean | No |
| services | services → `List<PackageServiceDto>` | array, default `emptyList()` | — |
| ownedPlan | ownedPlan → `OwnedServicePlanDto?` | object | Yes |

`PackageServiceDto { id: Int, name: String }`. `OwnedServicePlanDto { status: String, totalSessions: Int, completedSessions: Int }`.

**Endpoint provenance — read this before treating the contract as stable**: the code comment
explicitly states this was "reverse-engineered from the patient web portal's Catalog > Packages
tab (2026-08-31); only the read path is confirmed — purchase flow not yet implemented"
(`CatalogDtos.kt:45-47`, `WellnessApiService.kt:184-186`). This is **not** a documented/stable
backend contract — verify field names and types against a live response before building the
iOS DTO from this table alone.

## 8. API / NETWORK LAYER

| Method | Path | Purpose | Request | Response | Notes |
|---|---|---|---|---|---|
| GET | `packages` | List all packages visible to this patient (including their own ownership state per package) | — | `ServicePackagesResponseDto` | CUSTOMER JWT. Comment at `CatalogDtos.kt:45` says "absolute path (not under /api/wellness/)" while `WellnessApiService.kt:184` says "GET /api/wellness/packages" — **these two comments in the same codebase disagree; verify the actual base path against a live call before assuming either.** No pagination/filtering/sorting parameters. |

No purchase endpoint exists yet in this codebase (unlike Gift Cards' `order`/`confirm` pair,
documented elsewhere in the app) — "Buy package" is UI-only.

## 9. REPOSITORY LAYER

`CatalogRepository` interface: `getServices(): List<Service>`, `getCategories(): List<ServiceCategory>`, `getPackages(): List<ServicePackage>` — all three in one interface (the Catalog tab's three data sources), no packages-specific interface.

`CatalogRepositoryImpl.getPackages()`: `api.getServicePackages()` → throws `HttpException` if `!isSuccessful` → `response.body()?.packages.orEmpty().map { it.toDomain() }`. No caching, no error transformation at this layer (that's the UseCase's job).

## 10. USE CASES / BUSINESS LOGIC

`GetPackagesUseCase`: `Result.Success(repository.getPackages())`; `HttpException` → `Result.Error("HTTP_{code}", e.message() ?: "Server error", code)`; `IOException` → `Result.Error("NETWORK_ERROR", "No internet connection.")` (note: shorter/plainer message text than Prescriptions' equivalent — not "Please try again."); any other `Exception` → `UNEXPECTED_ERROR`. No offline fallback, no permission check.

## 11. MAPPERS / TRANSFORMATIONS

`CatalogMappers.kt:42-63`:

| DTO field | Domain field | Transform |
|---|---|---|
| id, name, description, price, currency, sellByDate, isActive | same-named | direct |
| discountPercent | discountPercent | `?: 0` |
| taxPercent | taxPercent | `?: 0` |
| grossPrice, validityDays | same-named | direct (stay nullable) |
| services | serviceNames | `services.map { it.name }` |
| ownedPlan | ownedPlan | `ownedPlan?.toDomain()` |
| (OwnedServicePlanDto) status/totalSessions/completedSessions | same-named | direct, all required |

## 12. ENUMS / CONSTANTS

No true enum types in this sub-feature. `OwnedServicePlan.status` and package `isActive` are the
only "state-like" fields, and neither has a client-defined enum — `isActive` is a plain
`Boolean`, and `status` is an unvalidated raw `String` (its value set is not established
anywhere in the traced code — see §23).

## 13. NAVIGATION

| From | To | Trigger | Params | Mechanism |
|---|---|---|---|---|
| Catalog tab, tab 0/2/3 | Catalog tab, tab 1 (Packages) | Tap "Packages" in `ScrollableTabRow` | — | Local Compose `remember { mutableIntStateOf }`, not a NavGraph route change |
| Packages tab | (nothing — purchase not implemented) | "Buy package" tap | — | Toast only |

Packages is reached exclusively by navigating to `Screen.CatalogTab.route` (mechanism outside
this document's scope — presumably a bottom nav item) and then switching tabs locally; there is
no independent back-stack entry for the Packages tab and no deep link targets it directly.

## 14. DEPENDENCY INJECTION

`CatalogRepository → CatalogRepositoryImpl` bound in `RepositoryModule.kt:94`. `CatalogViewModel` is `@HiltViewModel`, constructor-injected with `GetServicesUseCase`, `GetCategoriesUseCase`, `GetPackagesUseCase` — all three `@Inject constructor` classes needing no explicit `@Provides`.

## 15. STATE & BUSINESS FLOW

```
Catalog tab composed
    ↓
CatalogViewModel.init: loadServices() + loadCategories() + loadPackages() (concurrent, independent)
    ↓
loadPackages(): packagesLoading=true
    ↓
GetPackagesUseCase() → GET packages
    ├─ Success → packages = data, packagesLoading=false
    │      ↓
    │   packages.isEmpty() → EmptyState "No packages found."
    │   packages.isNotEmpty() → LazyColumn of PackageCards
    └─ Error → packagesError = message, packagesLoading=false → ErrorState + Retry(LoadPackages)
    ↓
User taps "Buy package" (only shown if ownedPlan == null)
    ↓
packageActionMessage = "Package purchase is coming soon." → Toast → dismissed
    (no state change to `packages`, no network call, no navigation)
```

## 16. ERROR HANDLING

| Origin | Propagation | UI result | Retry? |
|---|---|---|---|
| `GET packages` non-2xx | `HttpException` → `Result.Error` | `ErrorState` inside the Packages tab only (Services/Categories tabs unaffected) | Yes, `onRetry` → `LoadPackages` |
| `GET packages` offline | `IOException` → `Result.Error("NETWORK_ERROR", "No internet connection.")` | Same `ErrorState` | Yes |
| "Buy package" tap | No error path — always "succeeds" into the stub toast | Toast, no error state possible | N/A |

## 17. LOCAL STORAGE / CACHE

None. No Room entity, no DataStore entry for packages. Every tab-open and pull-to-refresh is a
live network call; there is no offline view of packages.

## 18. PERMISSIONS / DEVICE FEATURES

None specific to this sub-feature (no camera/location/notifications/etc.). No client-side RBAC
permission check either (contrast with Prescriptions' `my_prescriptions.read` gate) — the only
gate is holding a valid CUSTOMER JWT at all, enforced server-side.

## 19. SIDE EFFECTS

None beyond the one-shot informational toast. No analytics, no local persistence, no file I/O,
no background work.

## 20. COMPLETE END-TO-END DATA FLOW EXAMPLE

**User opens the Catalog tab and switches to Packages.**

```
NavGraph composable(Screen.CatalogTab.route) → hiltViewModel<CatalogViewModel>()
    ↓
CatalogViewModel.init → loadPackages() (concurrently with loadServices/loadCategories)
    ↓ packagesLoading = true
GetPackagesUseCase() invoke()
    ↓
CatalogRepositoryImpl.getPackages()
    ↓
WellnessApiService.getServicePackages()  →  GET packages
    ↓ 200 OK
    { "packages": [ { "id":12, "name":"Glow Package", ..., "ownedPlan": null } ] }
    ↓
response.body()?.packages.orEmpty().map { it.toDomain() }
    ↓ ServicePackageDto.toDomain() per CatalogMappers.kt
    → ServicePackage(id=12, name="Glow Package", ..., ownedPlan=null)
    ↓
Result.Success(List<ServicePackage>) returned up through UseCase
    ↓
CatalogViewModel: _uiState = _uiState.copy(packagesLoading=false, packages=[ServicePackage(...)])
    ↓
User has selectedTab==1 (Packages) in CatalogTabScreen's local state
    ↓
ServicePackagesContent recomposes: LazyColumn renders one PackageCard
    ↓ ownedPlan == null → shows "Buy package" Button instead of progress bar
```

## 21. iOS IMPLEMENTATION REFERENCE

See the combined mapping table (Section "iOS Mapping — All Features").

## 22. IMPLEMENTATION RULES / NON-OBVIOUS BEHAVIOR (Packages)

- **`CurrencyUtil.formatRupees` ignores `ServicePackage.currency`** — it always formats using an Indian-locale `NumberFormat` (₹), regardless of what the `currency` field says. If the iOS app is expected to support non-INR clinics, this is a gap to consciously decide on, not something to blindly port.
- **"Buy package" is a dead-end by design, not a bug** — do not build a purchase flow unless separately specced; the current Android app explicitly stops at a "coming soon" toast.
- **No package-detail view** — unlike Services, there's no bottom sheet/detail screen; all information a user can see about a package is on its card in the list. If iOS wants a tap-to-expand or detail screen, that is new UX, not a port.
- **Packages load eagerly with the other two tabs**, not lazily on first tab-selection — all three network calls fire the moment the Catalog screen is composed, even if the user never leaves the Services tab.
- **Two disagreeing code comments about the endpoint's base path** (`/api/wellness/packages` vs. "absolute path, not under /api/wellness/") — resolve this against a live request before finalizing the iOS endpoint URL, don't trust either comment blindly.

## 23. UNKNOWN / AMBIGUOUS AREAS (Packages)

- **Actual base path of `GET packages`** — see §22; the two source comments contradict each other. Verify with a live network capture.
- **Full value set of `OwnedServicePlan.status`** — only ever read, never compared against a known set of values, anywhere in the traced files. The UI doesn't branch on its value at all (it's not even displayed) — it exists in the domain model but is presently unused by any Composable. Confirm with the backend team what values are possible (e.g. `active`, `expired`, `completed`?) before deciding whether iOS needs to branch on it.
- **What `currency` is for**, given it's fetched but never used for formatting (see §22) — possibly meant for future multi-currency support, possibly vestigial. Do not assume it drives anything today.
- **Whether a purchase endpoint exists on the backend at all** — the DTO/API comments say "purchase flow not yet implemented" from the Android side; whether the backend even has `order`/`confirm`-style endpoints for packages (as Gift Cards do) was not established by any file in this feature. Do not assume Razorpay integration is a drop-in for this feature without separate backend verification.

---

# PART 3 — NOTIFICATIONS

## 1. FEATURE OVERVIEW

**What it does.** Two connected surfaces: (a) an in-app **Notification Inbox** — a list of
notifications synced from the backend and cached locally, with read/unread state and tap-to-
deep-link — and (b) **Notification Settings** — per-category and per-channel mute toggles plus a
quiet-hours window, stored device-locally (no backend endpoint exists for preferences). A third,
mostly-non-functional piece — a Socket.IO client meant to push notifications in real time — is
also part of this feature's code and is documented here because it directly governs how/whether
a notification ever reaches the inbox or the system tray outside of a manual sync.

**Entry points:**
- Dashboard bell icon → `onNotificationsClick` → `Screen.Notifications.route` (`NavGraph.kt:149-150`).
- Dashboard quick action `DashboardNavEvent.ToNotifications` → same route (`NavGraph.kt:243`).
- Deep link `globuscrm://screen/notifications`.
- Profile screen → "Notification Settings" → `Screen.NotificationSettings.route` (`NavGraph.kt:512`).
- Tapping a notification row inside a system-tray notification raised by the socket path (`WellnessSocketManager`) opens `MainActivity` (not a specific screen — see §13).
- Tapping a medication-reminder notification (Part 1) deep-links to the PDF viewer — a **separate** notification pipeline from this one (local `AlarmManager`, not synced/stored as a `Notification` domain object at all).

**Screens:**
1. `feature/notifications/presentation/screen/NotificationInboxScreen.kt`
2. `feature/notifications/presentation/screen/NotificationSettingsScreen.kt`

**User actions — Inbox:**
- Pull-to-refresh → re-syncs from backend and reloads the Room-backed list.
- Tap a notification row → marks it read, and if it carries a recognized `screen` value, navigates there.
- (No explicit "mark all read" UI control was found wired to `MarkAllRead` in the inbox screen itself — the event/UseCase exist, but no button/menu item calling it was found in `NotificationInboxScreen.kt`. See §23.)

**User actions — Settings:**
- Toggle any of 5 category switches or 3 channel switches.
- View (but, per the traced code, **not edit** — see §22) quiet-hours start/end times.
- Tap "Save settings" → persists to DataStore, shows a "Settings saved." confirmation line (not a toast — inline text).

**States:**
- Inbox: Loading → centered spinner. Empty → `EmptyState` "No notifications yet" with a bell icon. Success → `LazyColumn` of rows, unread rows highlighted with a tinted background + a leading dot indicator. There is **no explicit error state UI** on this screen — a sync failure just leaves the list as whatever Room already had (see §16).
- Settings: `isLoading` field exists on the state but nothing in the traced screen code branches on it (see §23) — the screen always renders the full toggle list immediately using default values until the real saved values arrive and the state updates in place.

**Permissions involved:** `POST_NOTIFICATIONS` (system tray display, requested app-wide in `MainActivity`, not per-feature).

**Conditional behavior:**
- Unread rows: tinted background (`secondaryContainer` at 25% alpha) + a small filled-square unread dot + semibold title. Read rows: plain surface background, normal-weight title, no dot (a same-sized invisible `Spacer` keeps row heights consistent).
- Tapping a notification always marks it read (even if it has no `screen` to navigate to).
- A tap only navigates if `notification.screen` is a **known** value (`appointments`/`prescriptions`/`memberships`/`wallet`/`book`) — any other string, or null, results in a no-op navigation (still marked read).

## 2. ARCHITECTURE

```
NotificationInboxScreen ↔ NotificationsViewModel
    ├─ GetNotificationsUseCase() : Flow<List<Notification>>  → NotificationRepository → NotificationDao.getAllAsFlow() (Room, reactive)
    ├─ SyncPortalNotificationsUseCase (one-shot) → WellnessApiService.getPortalNotifications() → writes into NotificationRepository → Room
    └─ MarkNotificationReadUseCase → NotificationRepository → NotificationDao (local-only; no backend PUT is actually reachable from the traced UI paths — see §16/§23 re: the declared-but-seemingly-unused mark-read endpoints)

NotificationSettingsScreen ↔ NotificationSettingsViewModel
    ├─ GetNotificationPreferencesUseCase() : Flow<NotificationPreferences> → NotificationPreferencesRepository → DataStore
    └─ SaveNotificationPreferencesUseCase → NotificationPreferencesRepository → DataStore

(Real-time path, independent of the two ViewModels above)
WellnessSocketManager (Singleton, started at app-process level, not per-screen)
    ↔ Socket.IO client → on "notification_new" → NotificationPreferencesRepository (read prefs)
        → NotificationRepository.insert() (Room)  [gated by in_app channel pref]
        → NotificationManagerCompat.notify() (system tray)  [gated by push channel pref + quiet hours]
```

The inbox's `Notification` stream is **Room-first, reactively**: `GetNotificationsUseCase()`
returns a cold `Flow` straight from Room, so any writer of that table — the manual "Refresh"
sync, `WellnessSocketManager`'s real-time handler, or `MainViewModel`'s login-time sync — updates
the inbox live without the inbox screen doing anything itself.

## 3. COMPLETE FILE INVENTORY

| Layer | File | Responsibility | Depends on |
|---|---|---|---|
| Presentation | `feature/notifications/presentation/screen/NotificationInboxScreen.kt` | Inbox list UI | `NotificationsUiState/UiEvent` |
| Presentation | `feature/notifications/presentation/screen/NotificationSettingsScreen.kt` | Preferences toggle UI | `NotificationSettingsUiState/UiEvent` |
| Presentation | `feature/notifications/presentation/viewmodel/NotificationsViewModel.kt` | Loads/syncs inbox, mark-read, tap→deep-link nav event | `GetNotificationsUseCase`, `MarkNotificationReadUseCase`, `SyncPortalNotificationsUseCase` |
| Presentation | `feature/notifications/presentation/viewmodel/NotificationSettingsViewModel.kt` | Loads/saves preferences | `GetNotificationPreferencesUseCase`, `SaveNotificationPreferencesUseCase` |
| Presentation | `feature/notifications/presentation/state/NotificationsState.kt` | `NotificationsUiState/UiEvent` | — |
| Presentation | `feature/notifications/presentation/state/NotificationSettingsState.kt` | `NotificationSettingsUiState/UiEvent`, `NotificationCategory`, `NotificationChannel` (incl. the 5 categories' and 3 channels' hardcoded default labels) | — |
| Domain | `feature/notifications/domain/model/Notification.kt` | `Notification` domain model | — |
| Domain | `feature/notifications/domain/model/NotificationPreferences.kt` | `NotificationPreferences` model + category/channel key constants + quiet-hours logic | — |
| Domain | `feature/notifications/domain/repository/NotificationRepository.kt` | Interface: reactive list, mark-read(-all), delete-old, insert | — |
| Domain | `feature/notifications/domain/repository/NotificationPreferencesRepository.kt` | Interface: observe/get/save preferences | — |
| Domain | `feature/notifications/domain/usecase/GetNotificationsUseCase.kt` | Thin `Flow` passthrough | `NotificationRepository` |
| Domain | `feature/notifications/domain/usecase/SyncPortalNotificationsUseCase.kt` | One-shot REST pull + Room upsert | `NotificationRepository`, `WellnessApiService` |
| Domain | `feature/notifications/domain/usecase/MarkNotificationReadUseCase.kt` | `invoke(id)` + `markAll()` | `NotificationRepository` |
| Domain | `feature/notifications/domain/usecase/GetNotificationPreferencesUseCase.kt` | Thin `Flow` passthrough | `NotificationPreferencesRepository` |
| Domain | `feature/notifications/domain/usecase/SaveNotificationPreferencesUseCase.kt` | Persist preferences, wrap errors | `NotificationPreferencesRepository` |
| Data | `feature/notifications/data/remote/dto/PortalNotificationDto.kt` | `PortalNotificationDto`, `PortalNotificationsResponseDto` | Moshi |
| Data | `feature/notifications/data/mapper/NotificationMappers.kt` | Dto/Entity ↔ Domain mappers | `java.time.Instant` |
| Data | `feature/notifications/data/repository/NotificationRepositoryImpl.kt` | Room-backed reactive repo | `NotificationDao` |
| Data | `feature/notifications/data/repository/NotificationPreferencesRepositoryImpl.kt` | DataStore-backed repo | `DataStore<Preferences>` |
| Data | `feature/notifications/data/local/entity/CachedNotification.kt` | Room entity | Room |
| Data | `feature/notifications/data/local/dao/NotificationDao.kt` | Room DAO incl. `getUnreadCountAsFlow()` (drives the dashboard bell badge) | Room |
| Real-time | `core/websocket/WellnessSocketManager.kt` | Socket.IO client, preference-gated notification handling, system-tray display | `TokenManager`, `NotificationRepository`, `NotificationPreferencesRepository`, Socket.IO |
| Shared | `MainViewModel.kt` | Triggers `SyncPortalNotificationsUseCase` on every sign-in; exposes `unreadNotificationCount` for the dashboard badge | `DataStoreManager`, `NotificationDao`, `SyncPortalNotificationsUseCase` |
| Shared | `MainActivity.kt` | Creates all 5 notification channels; requests `POST_NOTIFICATIONS` at runtime | — |
| Shared | `core/network/WellnessApiService.kt` | `getPortalNotifications`, `markPortalNotificationRead`, `markAllPortalNotificationsRead` declarations | Retrofit |

## 4. PRESENTATION / UI

### `NotificationInboxScreen`

- `PullToRefreshBox` → Loading spinner / `EmptyState("No notifications yet", Notifications icon)` / `LazyColumn` (8dp vertical content padding, no horizontal padding at the list level).
- **`NotificationRow`** (per item, key = `notification.id`):
  - Full-width `Row`, background tinted `secondaryContainer @ 25% alpha` if unread else `surface`; `clickable` → `TapNotification`; 16dp horizontal / 12dp vertical padding; 12dp gap between the dot and text column.
  - Leading: 8dp filled square dot (extraSmall shape, primary color) offset 6dp down, only if unread; else an 8dp invisible `Spacer` holding the layout's width.
  - Text column: title (bodyMedium, semibold if unread else normal), body (bodySmall, onSurfaceVariant), timestamp (`DateUtil.toDisplayDateTime(receivedAt)`, labelSmall, onSurfaceVariant).
  - `HorizontalDivider` after every row (16dp horizontal inset), including the last.
- No pagination in the UI — the whole Room table is rendered every time (bounded upstream by the 50-item sync page size and the 90-day eviction, not by any UI-level limit).

### `NotificationSettingsScreen`

- Single `LazyColumn` (16dp padding, 8dp spacing, 80dp bottom padding to clear the save button) inside a `Box`:
  1. `SectionLabel("Notification Categories")` + one `ToggleRow` per category (label + `Switch`).
  2. Divider + `SectionLabel("Delivery Channels")` + one `ToggleRow` per channel.
  3. Divider + `SectionLabel("Quiet Hours")` + two `QuietHoursRow`s ("Start time"/"End time").
  4. Conditionally, "Settings saved." (primary color, bodySmall) if `state.saveSuccess`.
- `QuietHoursRow`: label on the left, the time value (or "Not set" if blank) on the right in primary/medium-weight text — **this row has no `clickable` modifier and no time-picker is wired anywhere in this screen file**; the `onEdit` callback parameter exists and is threaded through to a `NotificationSettingsUiEvent.SetQuietStart/SetQuietEnd` dispatch, but nothing in `NotificationSettingsScreen.kt` ever invokes it. **Quiet hours are effectively read-only through this screen as currently shipped** — see §22.
- Bottom-pinned full-width "Save settings" `Button` (extraLarge shape) — shows a `CircularProgressIndicator` in place of the label while `isSaving`; disabled while `isSaving`.

## 5. VIEWMODEL / PRESENTATION LOGIC

### `NotificationsViewModel`

| Method | Called by | Logic | State/side effects |
|---|---|---|---|
| `load()` | `init`, `Refresh` | `isLoading=true`, then `getNotifications().collect { ... }` — this is a **long-lived collector on a cold Room Flow**, so every subsequent Room write (from anywhere: sync, socket, mark-read) re-triggers this block and replaces the whole `NotificationsUiState` | `notifications`, `isLoading=false` on first emission |
| `init` | — | Calls `load()` **and** launches a one-shot `syncPortalNotifications()` | — |
| `onEvent(Refresh)` | Pull-to-refresh | Calls `load()` again (re-subscribes; harmless since Room Flow is idempotent) **and** re-runs the one-shot sync | — |
| `onEvent(MarkRead(id))` | Row tap (indirectly, via `TapNotification`) | `markRead(id)` → Room `UPDATE ... SET isRead=1 WHERE id=:id` | Room write → `load()`'s collector re-emits automatically |
| `onEvent(MarkAllRead)` | (declared; no confirmed UI caller — see §23) | `markRead.markAll()` | Same reactive re-emit |
| `onEvent(TapNotification(notification))` | Row tap | Always calls `markRead(notification.id)` first; then, only if `notification.screen` is non-blank, sends `OpenDeepLink(notification.screen, notification.entityId)` | Nav event |
| `onEvent(NavigateBack)` | — | Sends `Back` nav event | — |

`load()`'s use of `.collect{}` inside a `viewModelScope.launch` means **every call to `load()` starts a new, independent, never-cancelled collector** on the same underlying Flow (the old one isn't explicitly cancelled — it just becomes redundant since both write the same derived state). This is a minor inefficiency, not a correctness bug, since Room Flow re-emissions are idempotent and cheap here; noted for completeness in §22.

### `NotificationSettingsViewModel`

| Method | Called by | Logic |
|---|---|---|
| `loadSavedPreferences()` | `init` | `getPreferences().first()` (one-shot read of the DataStore Flow, not a live collector) → maps the saved `enabledCategories`/`enabledChannels` sets onto the **hardcoded default category/channel list** (`state.categories.map { it.copy(enabled = saved.isCategoryEnabled(it.key)) }`) — so the set of togglable items is always the 5+3 hardcoded ones; only their `enabled` flags come from storage |
| `onEvent(ToggleCategory/ToggleChannel)` | Switch flip | Updates the matching item's `enabled` in local state only; also clears `saveSuccess=false` | No persistence until "Save" is tapped |
| `onEvent(SetQuietStart/SetQuietEnd)` | Not currently reachable from the screen (§4) | Would update `quietStart`/`quietEnd` in local state | — |
| `save()` | "Save settings" tap | Builds a fresh `NotificationPreferences` from current local state (`categories.filter{enabled}.map{key}.toSet()`, same for channels, plus the current `quietStart`/`quietEnd` strings) → `SaveNotificationPreferencesUseCase(...)` | Success → `isSaving=false, saveSuccess=true`. Error → `isSaving=false, saveSuccess=false, error=message` |

## 6. DOMAIN MODELS

### `Notification` (`feature/notifications/domain/model/Notification.kt`)

| Property | Type | Optional | Meaning |
|---|---|---|---|
| id | String | No | Backend numeric id, stringified (Room PK is `String`) |
| type | String | No | Free-text category tag from backend (e.g. `"info"` default) — not a client-enforced enum |
| title | String | No | — |
| body | String | No | — |
| screen | String? | Yes | Normalized deep-link target key (see §12) — `null`/unrecognized ⇒ tap doesn't navigate |
| entityId | String? | Yes | Stringified id of the related entity, for the target screen to use |
| isRead | Boolean | No | — |
| receivedAt | Long | No | Epoch ms |

### `NotificationPreferences` (`feature/notifications/domain/model/NotificationPreferences.kt`)

| Property | Type | Default | Meaning |
|---|---|---|---|
| enabledCategories | Set\<String\> | `ALL_CATEGORIES` (all 5) | Which notification categories are not muted |
| enabledChannels | Set\<String\> | `ALL_CHANNELS` (all 3) | Which delivery channels are not muted |
| quietStart | String | `"22:00"` | `"HH:mm"` 24h format |
| quietEnd | String | `"07:00"` | `"HH:mm"` 24h format |

Methods: `isCategoryEnabled(key)`, `isChannelEnabled(key)` — plain set membership. `isQuietAt(minuteOfDay: Int): Boolean` — handles the midnight-wrapping case: if `start == end`, always `false` (quiet hours effectively disabled); if `start < end`, simple range check; else (wraps midnight, e.g. 22:00→07:00), `true` when `minute >= start || minute < end`. `parseMinutes("HH:mm")` returns `null` (and thus `isQuietAt` returns `false`) for any malformed time string — malformed quiet-hours settings **fail open** (notifications still shown), not closed.

Category key constants: `appointment_reminders`, `prescription_ready`, `payment_receipts`, `membership_updates`, `gift_card_activity`. Channel key constants: `in_app`, `push`, `email` (note: **`email` has no implementation anywhere in the traced code** — it's a UI toggle and a stored preference with no code path that reads it to decide whether to send an email; see §23).

## 7. DTOs / API MODELS

### `PortalNotificationDto` / `PortalNotificationsResponseDto` — `GET portal/me/notifications`

```json
{
  "notifications": [
    {
      "id": 4821,
      "type": "appointment",
      "title": "Appointment reminder",
      "message": "Your visit is tomorrow at 10:30 AM",
      "link": "/appointments",
      "entityId": 900,
      "isRead": false,
      "createdAt": "2026-08-31T09:00:00.000Z"
    }
  ],
  "unreadCount": 3,
  "count": 12
}
```

| JSON field | DTO property | Type | Default if absent |
|---|---|---|---|
| id | id | Int | — (required) |
| type | type | String | `"info"` |
| title | title | String | `""` |
| message | body (`@Json(name="message")`) | String? | `null` |
| link | screen (`@Json(name="link")`) | String? | `null` |
| entityId | entityId | Int? | `null` |
| isRead | isRead | Boolean | `false` |
| createdAt | receivedAt (`@Json(name="createdAt")`) | String? | `null` |

`PortalNotificationsResponseDto`: `notifications: List<PortalNotificationDto>` (required, no default — a response missing this key would fail to parse), `unreadCount: Int = 0`, `count: Int = 0`. **`unreadCount`/`count` are parsed but never read anywhere downstream** — the app derives its own unread badge count from Room (`NotificationDao.getUnreadCountAsFlow()`), not from this response field (see §22).

Note the DTO's `screen`/`title` (from `@Json(name="link")` and raw `title`) are **not** the same shape as `WellnessSocketManager`'s socket-side handling: the REST DTO keeps `link`'s raw string as-is in `PortalNotificationDto.screen` (mapped straight through, unnormalized, by `PortalNotificationDto.toDomain()` — see §11), whereas the socket path normalizes `link` through `linkToScreen()` before constructing its own `Notification`. **This is an inconsistency between the two ingestion paths** — see §22.

## 8. API / NETWORK LAYER

| Method | Path | Purpose | Request | Response | Notes |
|---|---|---|---|---|---|
| GET | `portal/me/notifications` | Fetch a page of notifications | `page: Int = 1`, `limit: Int = 50` (query params) | `PortalNotificationsResponseDto` | verifyPatientToken. Called with defaults everywhere in the traced code — **no pagination is ever actually exercised** (no "load more" UI, no page-2 call found) — see §22/§23. |
| PUT | `portal/me/notifications/{id}/read` | Mark one notification read server-side | path `id: String` | `Unit` (empty body expected) | **Declared but no call site found** in `NotificationRepositoryImpl`/`MarkNotificationReadUseCase`/`NotificationsViewModel` — mark-read is Room-local only in the traced code (see §16, §22). |
| POST | `portal/me/notifications/read-all` | Mark all read server-side | — | `Unit` | Same — **declared, not called** from any traced path. |

## 9. REPOSITORY LAYER

### `NotificationRepository` / `NotificationRepositoryImpl`

Interface: `getNotificationsAsFlow(): Flow<List<Notification>>`, `markRead(id)`, `markAllRead()`, `deleteOldNotifications()`, `insert(notification)`. Impl is a thin Room wrapper — `markRead`/`markAllRead`/`insert` map straight to `NotificationDao` calls (`insert` uses `REPLACE` conflict strategy, so re-syncing an already-known id just overwrites the row rather than erroring or duplicating). `deleteOldNotifications()`: deletes rows with `receivedAt < now - 90 days`.

### `NotificationPreferencesRepository` / `NotificationPreferencesRepositoryImpl`

Backed by Jetpack DataStore `Preferences` (not a dedicated `.proto` schema — plain key-value). `observe()` maps the raw `Preferences` object to a `NotificationPreferences` domain object, **defaulting absent keys to "everything on"** (`ALL_CATEGORIES`/`ALL_CHANNELS`) rather than an empty set — explicitly to avoid a first-run/never-configured device silently muting everything (`NotificationPreferencesRepositoryImpl.kt:25-26` comment). `get()` = `observe().first()`. `save()` writes all four keys in one `dataStore.edit {}` transaction.

DataStore keys: `notif_enabled_categories` (string set), `notif_enabled_channels` (string set), `notif_quiet_start` (string), `notif_quiet_end` (string).

## 10. USE CASES / BUSINESS LOGIC

| UseCase | Input | Output | Logic |
|---|---|---|---|
| `GetNotificationsUseCase` | — | `Flow<List<Notification>>` | Pure passthrough to the repository's reactive Room stream — no error handling, no `Result` wrapper (this one UseCase in the whole app returns a raw `Flow`, not `Result`, because it can't fail in a way distinct from "Room is broken") |
| `SyncPortalNotificationsUseCase` | — | `Result<Int>` (count synced) | Calls the API; on success, **inserts every returned DTO into Room one at a time** (`dtos.forEach { repository.insert(it.toDomain()) }` — no batch/transaction); on non-2xx → `Result.Error("HTTP_{code}", "Sync failed")`; `IOException` → `Result.Error("NETWORK_ERROR", "Offline — showing cached notifications")`; other `Exception` → `SYNC_ERROR`. **This UseCase's `Result` is never actually inspected by any caller** — both `NotificationsViewModel` and `MainViewModel` call it as `viewModelScope.launch { syncPortalNotifications() }` and discard the result entirely (see §22) — so a sync failure is invisible to the user beyond "the list just doesn't update." |
| `MarkNotificationReadUseCase` | `notificationId: String` (invoke) / — (markAll) | `Unit` | Direct passthrough, no error wrapping — an exception here would propagate uncaught to the calling `viewModelScope.launch` (Room writes essentially never throw in practice, so this is low-risk but is a real asymmetry vs. every other UseCase in the app, which wrap errors in `Result`) |
| `GetNotificationPreferencesUseCase` | — | `Flow<NotificationPreferences>` | Passthrough |
| `SaveNotificationPreferencesUseCase` | `NotificationPreferences` | `Result<Unit>` | `IOException` → `STORAGE_ERROR`, other → `UNEXPECTED_ERROR` |

## 11. MAPPERS / TRANSFORMATIONS

`NotificationMappers.kt`:

| Source | Target | Notes |
|---|---|---|
| `PortalNotificationDto.toDomain()` | `Notification` | `id = id.toString()`; `body = body ?: ""`; `entityId = entityId?.toString()`; `receivedAt = runCatching { Instant.parse(receivedAt).toEpochMilli() }.getOrDefault(0L)` — a malformed/absent `createdAt` silently becomes epoch 0 (Jan 1 1970), which would sort that notification as "oldest" in any date-sorted view. `screen` is passed through **unnormalized** (raw `link` string, e.g. `"/appointments"`, not the short key `"appointments"` the socket path produces — see §22). |
| `CachedNotification.toDomain()` | `Notification` | 1:1 field copy, no transformation |
| `Notification.toEntity()` | `CachedNotification` | 1:1 field copy |

## 12. ENUMS / CONSTANTS

**Category keys** (`NotificationPreferences` companion): `appointment_reminders`, `prescription_ready`, `payment_receipts`, `membership_updates`, `gift_card_activity`. Labels shown in Settings (`NotificationSettingsState.kt:7-13`): "Appointment reminders & changes", "Prescription ready", "Payment & transaction receipts", "Membership updates", "Gift card activity" — in that fixed order.

**Channel keys**: `in_app`, `push`, `email`. Labels: "In-App Bell", "Push Notifications", "Email".

**`entityType` → category** (socket path only, `WellnessSocketManager.categoryForEntityType`): `Appointment`→`appointment_reminders`, `Prescription`→`prescription_ready`, `Wallet`/`Payment`/`Transaction`→`payment_receipts`, `Membership`→`membership_updates`, `GiftCard`→`gift_card_activity`, **any other/unknown value → `appointment_reminders`** (deliberate: "the category patients are least likely to have muted — better a stray notification than a silently dropped one," `WellnessSocketManager.kt:150-152`).

**`link` → `screen` key** (socket path, `linkToScreen`): prefix-match — `/appointments*`→`"appointments"`, `/prescriptions*`→`"prescriptions"`, `/wallet*`→`"wallet"`, `/memberships*`→`"memberships"`, `/book*`→`"book"`, else `null`.

**`screen` → route** (NavGraph's `OpenDeepLink` handler, `NavGraph.kt:529-536`): `"appointments"`→`Screen.MyAppointments.route`, `"prescriptions"`→`Screen.Prescriptions.route`, `"memberships"`→`Screen.Memberships.route`, `"wallet"`→`Screen.Wallet.route`, `"book"`→`Screen.BookAppointment.createRoute()`, else no navigation.

**`entityType`/`priority` → Android notification channel** (socket path, `channelForType`): `Appointment`→`wellness_reminders`, `Prescription`→`wellness_health`, `Wallet`/`Payment`→`wellness_wallet`, else if `priority` is `"high"`/`"urgent"`→`wellness_reminders`, else→`wellness_health`. **`GiftCard` and `Membership` entity types fall through to the `priority`/default branch here** — i.e. they're categorized as `gift_card_activity`/`membership_updates` for the *preference* check, but not given their own dedicated *Android notification channel* (they end up on `wellness_health` unless marked high-priority) — a real inconsistency between the preference-category table and the channel-routing table, both in the same file (`WellnessSocketManager.kt:144-153` vs. `186-192`).

**Android notification channels** (created in `MainActivity.createNotificationChannels()`): `wellness_medication_reminders` (HIGH, vibration — Part 1's channel, created twice: once here and once lazily by the receiver itself), `wellness_reminders` "Appointment Reminders" (HIGH, vibration), `wellness_health` "Health Updates" (DEFAULT), `wellness_wallet` "Wallet & Payments" (DEFAULT), `wellness_offers` "Offers & Surveys" (LOW). Note: **`wellness_offers` is declared but no code path in this feature ever targets it** — `channelForType` never returns it (see §22).

## 13. NAVIGATION

| From | To | Trigger | Params | Mechanism |
|---|---|---|---|---|
| Dashboard bell / quick action | Notification Inbox | Tap | — | `navController.navigate(Screen.Notifications.route)` |
| Any screen (external) | Notification Inbox | Deep link | — | `globuscrm://screen/notifications` |
| Profile | Notification Settings | Tap | — | `navController.navigate(Screen.NotificationSettings.route)` |
| Notification Inbox | Target screen (appointments/prescriptions/memberships/wallet/book) | Row tap, if `screen` is recognized | `entityId` (passed in the nav event but **not actually consumed by any of the destination routes in the traced `NavGraph.kt` snippet** — the routes navigated to (`MyAppointments`, `Prescriptions`, `Memberships`, `Wallet`, `BookAppointment`) are argument-less or use their own separate args, not `entityId` — see §22) | `navController.navigate(route)` |
| System-tray notification (socket path) tap | `MainActivity` (app foreground, no specific screen) | System tap | — | Plain activity-launch `PendingIntent`, `FLAG_ACTIVITY_SINGLE_TOP` — **does not deep-link to the notification's actual target screen**, unlike the medication-reminder notifications in Part 1 which do deep-link (see §22) |
| Inbox | back | Back nav event | — | `popBackStack()` |

## 14. DEPENDENCY INJECTION

`NotificationRepository → NotificationRepositoryImpl`, `NotificationPreferencesRepository → NotificationPreferencesRepositoryImpl` bound in `RepositoryModule.kt:77,80-82`. `WellnessSocketManager` is `@Singleton @Inject constructor` — Hilt constructs it once and its `init {}` block (which starts observing the auth token) runs at first-injection time. **Where the first injection actually happens** (i.e. what forces it to be constructed at all, since nothing calls a method on it from the UI layer) was not found in the files traced for this document — see §23.

## 15. STATE & BUSINESS FLOW

```
App process starts / user signs in
    ↓
MainViewModel.init: observes DataStoreManager.tokenFlow() → on sign-in, calls SyncPortalNotificationsUseCase()
WellnessSocketManager.init: observes TokenManager.tokenFlow() → on sign-in, calls connect(token)
    (two independent token observers, two independent side effects)
    ↓
connect(): if BuildConfig.SOCKET_URL is blank → logs and returns, no connection attempt ever made
           (this is the actual shipped state — see root CLAUDE.md "Real-time Notifications": no gateway is reachable)
    ↓
User opens Notification Inbox
    ↓
NotificationsViewModel.init: load() [subscribes to Room Flow] + syncPortalNotifications() [one-shot REST pull]
    ↓
GET portal/me/notifications → Room upsert (one row at a time) → Room Flow re-emits → UI updates
    ↓
User taps a row
    ↓
markRead(id) [Room UPDATE] → Room Flow re-emits (isRead=true) → row re-renders as read
    ↓ (if screen recognized)
OpenDeepLink nav event → NavGraph maps screen key → route → navigate
    ↓
(Independently, if the socket were ever actually connected —)
"notification_new" socket event → handleNotification()
    → category muted? → drop entirely
    → in_app channel enabled? → Room insert (feeds the same Flow the Inbox screen is watching)
    → push channel enabled AND not in quiet hours? → system tray notification
```

## 16. ERROR HANDLING

| Origin | Propagation | UI result | Retry? |
|---|---|---|---|
| `SyncPortalNotificationsUseCase` failure (any cause) | `Result.Error` returned but **discarded by every caller** (`NotificationsViewModel`, `MainViewModel` both do `viewModelScope.launch { syncPortalNotifications() }` with no `when` on the result) | **None** — the Inbox screen shows no error state for a failed sync; it silently continues showing whatever was already in Room | Only implicitly, via pull-to-refresh triggering another sync attempt |
| `GET portal/me/notifications` offline | `IOException` inside the UseCase → `Result.Error("NETWORK_ERROR", "Offline — showing cached notifications")` | Message text exists but, per above, is never surfaced anywhere | User must know to pull-to-refresh again once back online — no automatic retry |
| `markRead`/`markAllRead` Room write failure | Not caught anywhere in this feature's chain — would propagate as an uncaught exception in the ViewModel's coroutine | Would crash the app (or be caught by whatever global coroutine exception handler exists elsewhere, not traced here) | N/A — Room writes essentially never fail in practice |
| `SaveNotificationPreferencesUseCase` failure | `Result.Error` → `NotificationSettingsViewModel.save()` sets `state.error` | **No UI in `NotificationSettingsScreen.kt` reads `state.error` at all** — the field is set but never displayed (see §22/23) | Re-tap "Save settings" |
| Socket connection failure | Logged only (`Log.w(TAG, ...)`) | No UI surface whatsoever — by design, this is meant to fail silently into the REST-sync fallback | N/A |

## 17. LOCAL STORAGE / CACHE

- **Room, table `cached_notifications`** (`CachedNotification`): the durable store for the inbox; the Inbox screen reads *only* from here, never directly from a network response. 90-day eviction (`deleteOldNotifications()`) — call site not found in this feature's files (see §23, same caveat as Prescriptions' PDF eviction). `getUnreadCountAsFlow()` powers the dashboard bell badge via `MainViewModel`.
- **DataStore `Preferences`** (shared app-wide DataStore instance, 4 keys as listed in §9): preferences survive logout — "they belong to the device, not the patient" (root `CLAUDE.md`, corroborated by there being no logout-triggered clear call in the traced files).
- Offline behavior: Inbox is inherently offline-capable by construction (Room-first reads); Settings reads/writes are always local-only (no backend endpoint exists for preferences at all — this is not a fallback, it's the only implementation).

## 18. PERMISSIONS / DEVICE FEATURES

- **`POST_NOTIFICATIONS`**: requested once by `MainActivity` at startup (not by this feature's screens specifically) via an `ActivityResultContracts.RequestPermission()` launcher (`MainActivity.kt:73-79`), only if not already granted, only on API ≥ 33. If denied, `WellnessSocketManager.showSystemNotification` calls `NotificationManagerCompat.notify` under a `@SuppressLint("MissingPermission")` — i.e. the call would silently throw-and-be-swallowed-by-the-OS / no-op rather than crash, but no in-app fallback/banner exists to tell the user tray notifications are off.
- No other device permissions in this feature.

## 19. SIDE EFFECTS

- System tray notifications (socket path only, when connected — currently never, per root `CLAUDE.md`).
- Room writes on every sync/socket-receive/mark-read.
- DataStore writes on Settings save.
- No analytics/logging beyond the socket connection's `Log.i`/`Log.w` calls (Logcat only, not any analytics SDK).
- No file generation, no background work (WorkManager) — sync is always foreground/on-demand (`init` + pull-to-refresh + `MainViewModel`'s login trigger), never scheduled periodically.

## 20. COMPLETE END-TO-END DATA FLOW EXAMPLE

**User pulls to refresh the Notification Inbox.**

```
NotificationInboxScreen: PullToRefreshBox onRefresh → isRefreshing=true, onEvent(NotificationsUiEvent.Refresh)
    ↓
NotificationsViewModel.onEvent(Refresh)
    ↓ load() — re-subscribes to the Room Flow (already-subscribed collector keeps running too)
    ↓ viewModelScope.launch { syncPortalNotifications() }
    ↓
SyncPortalNotificationsUseCase()
    ↓
WellnessApiService.getPortalNotifications(page=1, limit=50)  →  GET portal/me/notifications?page=1&limit=50
    ↓ 200 OK
    { "notifications": [ {...}, {...} ], "unreadCount": 2, "count": 30 }
    ↓
dtos = response.body()?.notifications ?: emptyList()
dtos.forEach { dto -> repository.insert(dto.toDomain()) }
    ↓ per-dto: PortalNotificationDto.toDomain() → Notification(id="4821", ..., screen="/appointments" [RAW, unnormalized])
    ↓ NotificationRepositoryImpl.insert → NotificationDao.insert (REPLACE) → CachedNotification row written/overwritten
    ↓
Result.Success(dtos.size) returned — discarded by the ViewModel (see §16)
    ↓ (independently, driven by Room's own change notification, not by the above return value)
NotificationDao.getAllAsFlow() re-emits the full list
    ↓
NotificationRepositoryImpl.getNotificationsAsFlow() maps entities → domain
    ↓
GetNotificationsUseCase's Flow re-emits inside NotificationsViewModel's load() collector
    ↓
_uiState = NotificationsUiState(isLoading=false, notifications=[...])
    ↓
NotificationInboxScreen recomposes: isRefreshing set back to false (LaunchedEffect(state.isLoading)), rows update
```

## 21. iOS IMPLEMENTATION REFERENCE

See the combined mapping table (Section "iOS Mapping — All Features"). Socket.IO specifically:
if the iOS app is expected to include the same (currently-inert) Socket.IO client, use a Socket.IO
Swift client (e.g. `socket.io-client-swift`) configured the same way — connect only if a
gateway URL is configured, auth via a `token` field in the handshake, path configurable
separately from the host, bounded reconnection attempts. Given the gateway is not reachable in
production today (per root `CLAUDE.md`), **confirm with the team whether iOS should even include
this dead code path**, rather than porting it by default.

## 22. IMPLEMENTATION RULES / NON-OBVIOUS BEHAVIOR (Notifications)

- **The real-time channel does not work today.** `BuildConfig.SOCKET_URL` is empty by default and `connect()` no-ops immediately if so (`WellnessSocketManager.kt:59-63`). All notification delivery in the shipped app is REST-pull: on sign-in (`MainViewModel`) and on Inbox open/pull-to-refresh (`NotificationsViewModel`). **A backgrounded/closed app receives nothing.** Do not build the iOS app assuming push notifications currently work end-to-end — they don't, at either the Android or backend level as currently configured.
- **`SyncPortalNotificationsUseCase`'s `Result` is discarded by both callers** — sync failures are invisible to the user. If the iOS team wants this fixed, it's a deliberate behavior change vs. the current Android app, not a like-for-like port.
- **Mark-read is local-only** — `PUT portal/me/notifications/{id}/read` and `POST portal/me/notifications/read-all` are declared in `WellnessApiService` but have **no confirmed call site** in `NotificationRepositoryImpl`, `MarkNotificationReadUseCase`, or either ViewModel traced for this document. As shipped, marking a notification read on one device does **not** sync that state to the backend or to any other device/session. Verify with the backend/Android team whether this is intentional or a known gap before deciding whether iOS should call these endpoints (which the Android app apparently does not).
- **`screen` normalization is inconsistent between the two ingestion paths.** The REST path (`PortalNotificationDto.toDomain()`) stores the raw `link` string (e.g. `"/appointments"`) directly as `Notification.screen`, with no normalization. The socket path (`WellnessSocketManager.linkToScreen()`) normalizes the same kind of value to a short key (e.g. `"appointments"`) before ever constructing a `Notification`. Since `NavGraph.kt`'s `OpenDeepLink` handler only recognizes the **short-key** forms (`"appointments"`, `"prescriptions"`, etc. — see §12), **a REST-synced notification whose `link` starts with `/appointments` will hold `screen = "/appointments"` and will NOT match any case in the `when` block, silently failing to navigate anywhere on tap** (it still gets marked read, per §5). This is very likely an existing bug in the Android app, not intended behavior — an iOS implementation should almost certainly normalize both ingestion paths the same way (recommend applying `linkToScreen()`-equivalent logic to the REST path too) rather than reproducing the bug, but **flag this explicitly to the team rather than silently fixing it**, since the fix changes observable behavior.
- **Quiet-hours times cannot be edited from the Settings screen as shipped** — the `SetQuietStart`/`SetQuietEnd` events and their ViewModel handling exist, but `NotificationSettingsScreen.kt`'s `QuietHoursRow` has no `clickable` modifier and no time-picker dialog anywhere in the file, so `onEdit` is never invoked by any user gesture. The displayed values are always the loaded defaults (`22:00`/`07:00`) unless changed by some other means. **Do not port this as "read-only by design"** without checking with the team — it reads as an incomplete feature (dead event wiring), not an intentional read-only display; decide with the team whether iOS should implement an actual time picker (recommended) or preserve the gap.
- **`SaveNotificationPreferencesUseCase` errors are computed but never displayed** — `NotificationSettingsUiState.error` is set on failure but nothing in `NotificationSettingsScreen.kt` reads it. A failed save currently looks identical to a successful one from the UI's perspective **except** that "Settings saved." never appears (since `saveSuccess` stays false) — there's no error message shown, just an absence of the success message. Recommend iOS surface the actual error text; flag the gap either way.
- **`unreadCount`/`count` from the sync response are parsed but unused** — the app's unread badge is entirely Room-derived (`NotificationDao.getUnreadCountAsFlow()`), not from these response fields. If iOS computes its badge from the response fields directly instead of from local storage, it may disagree with what's actually rendered in the list (e.g. if a sync partially fails or a notification was already marked read locally after a previous sync).
- **`GiftCard`/`Membership` entity types get their preference category right but their Android notification channel wrong** (§12) — both fall into `wellness_health` rather than a dedicated channel unless `priority` happens to be high/urgent. If iOS uses category-to-channel mapping for local notification categories/sounds, decide whether to replicate this quirk or align the two tables (recommend aligning, but flag it).
- **`email` channel preference has no implementation** — toggling it changes nothing observable anywhere in the traced code. Treat as reserved/future, not functional.
- **`wellness_offers` Android channel is declared but never targeted** by any notification-raising code in this feature. Likely reserved for a promotional-notification feature not yet built.
- **Pagination params (`page`/`limit`) exist on the endpoint but are never varied from their defaults** (`page=1, limit=50`) anywhere in the app — there is no "load more" affordance. An iOS client could technically request more pages, but there's no existing UI pattern to match.
- **System-tray tap (socket path) does not deep-link**, unlike the medication-reminder notifications in Part 1 — it just foregrounds `MainActivity` at whatever screen it was last on. This is presumably an oversight (the `Notification.screen` value is computed and available at construction time in `handleNotification()` but never attached to the `PendingIntent`'s `Intent` data), but is being documented as current behavior, not fixed.

## 23. UNKNOWN / AMBIGUOUS AREAS (Notifications)

- **No confirmed "Mark all read" UI control** in `NotificationInboxScreen.kt` — the event and UseCase method exist end-to-end, but no `Button`/menu/swipe-action calling `MarkAllRead` was found in the screen file read for this document. Either it's UI that exists elsewhere not covered here (a top bar action supplied by the NavGraph's Scaffold, perhaps), or it's dead code. **Verify by inspecting the actual running app or the Scaffold/TopBar wiring around this route** before deciding whether iOS needs a "mark all read" affordance.
- **Whether `PUT .../read` / `POST .../read-all` are called from anywhere else in the app** (outside the files traced here) — a targeted grep across `feature/notifications/` and `MainViewModel`/`MainActivity` found no call sites, but a repo-wide grep beyond this document's scope wasn't performed. Recommend a full-repo grep for `markPortalNotificationRead`/`markAllPortalNotificationsRead` before concluding they're entirely unused.
- **Where `deleteOldNotifications()` (90-day eviction) is actually invoked** — not found in this feature's own files; likely a startup hook elsewhere in the app (mirrors the same open question for Prescriptions' `evictStalePdfs()`).
- **What forces `WellnessSocketManager` to be constructed at all** (Hilt `@Singleton` classes are lazy — something has to first `@Inject` or otherwise request an instance) — not found in the files traced. Likely `Application.onCreate()` or `MainActivity`, injecting it just to trigger its `init` side effect.
- **Full value set of `Notification.type`** — parsed from the backend as a free string (default `"info"`), never validated against a known enum, and not visibly used to alter row styling in `NotificationInboxScreen.kt` (only `isRead` affects visual treatment). Confirm with the backend whether `type` is meant to drive an icon/color the iOS app should implement, or is currently vestigial on the client.

---

# iOS MAPPING — ALL FEATURES

Conceptual only — preserve the Android behavior, API contracts, models, and UI documented above;
do not blindly transliterate syntax.

| Android | iOS equivalent |
|---|---|
| `@Composable` Screen (`XScreen(state, onEvent)`) | SwiftUI `View` taking a state struct + an action closure/enum, same statelessness discipline |
| `@HiltViewModel` ViewModel (`MutableStateFlow<UiState>`, `Channel<NavEvent>`) | `@Observable`/`ObservableObject` with a `@Published`/observed state property, plus an `AsyncStream`/Combine publisher (or a delegate/coordinator callback) for one-shot nav events |
| `UiEvent` sealed class | Swift `enum` with associated values |
| UseCase (`suspend operator fun invoke()`, wraps `Result<T>`) | A `struct`/`final class` with a single `func execute(...) async throws -> T` or an `async -> Result<T, AppError>`-returning function; preserve the same error-code taxonomy (`HTTP_{status}`, `NETWORK_ERROR`, `UNAUTHORIZED`, `FORBIDDEN`, etc.) as an `enum AppError` or similar |
| Repository interface (`domain/repository/X.kt`) | Swift `protocol` |
| RepositoryImpl (`data/repository/XImpl.kt`) | Concrete type conforming to the protocol |
| Moshi `@JsonClass` DTO | `Codable` `struct`, with `CodingKeys` reproducing every `@Json(name=...)` mapping documented above (e.g. `message`→`body`, `link`→`screen`, `createdAt`→`receivedAt`) |
| Mapper function (`Dto.toDomain()`) | An `init(dto:)` on the domain type, or a free mapping function — preserve every default/fallback documented per-field (e.g. `body ?: ""`, `Instant.parse(...) ?: 0L`) |
| Domain model (plain `data class`) | Swift `struct` |
| `core/navigation/NavGraph.kt` + `Screen` sealed class + `navDeepLink` | `NavigationStack`/`NavigationPath` (or a Coordinator) + a route enum + `onOpenURL`/Universal Links for the `globuscrm://` deep-link scheme |
| Hilt DI (`@Inject constructor`, `@Binds`, `@Singleton`) | Swift's own constructor injection via an app-level composition root (e.g. a `Dependencies`/`Container` struct built once in the App's `init`), or a DI library if the iOS codebase already uses one — match whatever pattern already exists in the iOS project rather than introducing a new one |
| Kotlin Coroutines (`suspend fun`) | Swift `async`/`await` |
| Kotlin `Flow` (cold, e.g. Room DAO streams) | `AsyncStream`, Combine `Publisher`, or `@Observable`-tracked storage read reactively — pick whichever the iOS app's persistence layer (Core Data / SwiftData / GRDB) naturally exposes |
| Room (`@Entity`, `@Dao`) | Core Data / SwiftData / GRDB / SQLite — whatever the iOS app already uses for local persistence; reproduce the schemas (fields, defaults, eviction rules) documented per-feature above, not the Room API itself |
| Jetpack DataStore `Preferences` | `UserDefaults` (small key-value settings, matching DataStore's own scale here) or the iOS app's existing settings-storage mechanism |
| `AlarmManager` + `BroadcastReceiver` (medication reminders) | `UNUserNotificationCenter` + `UNCalendarNotificationTrigger`/`UNTimeIntervalNotificationTrigger` local notifications — re-implement the **scheduling business logic** in Part 1 §12, not the Android API surface (there is no receiver/alarm-manager equivalent to port) |
| Socket.IO client (`WellnessSocketManager`) | `socket.io-client-swift` (or the iOS app's existing realtime client), configured the same way (auth token in handshake, configurable path, bounded reconnection) — **confirm with the team whether this is worth including at all today, given it's currently inert in production (Part 3 §22)** |
| `NotificationCompat`/`NotificationManagerCompat` (system tray) | `UNUserNotificationCenter.add(UNNotificationRequest)` for local notifications; APNs (a push framework) for anything meant to arrive while the app isn't running — note that **today, nothing in this feature set reaches the device while backgrounded/closed on Android either** (Part 3 §22), so parity, not improvement, is the default target unless the team asks otherwise |
| `Toast.makeText(...)` one-shot messages | A transient banner/alert/snackbar-style view bound to an optional state string, cleared after display — same one-shot pattern used throughout (state field → `LaunchedEffect`-equivalent → clear) |
| `PdfRenderer` (in-app PDF viewer) | `PDFKit`'s `PDFView`/`PDFDocument` — a native, more direct equivalent; no need to manually rasterize pages to bitmaps as the Android code does, that's an Android-specific workaround, not a behavior to preserve |
| `MediaStore`/legacy `File` download-to-Downloads | `UIDocumentPickerViewController` (save-to-Files) or `UIActivityViewController` (share sheet) — iOS has no direct "Downloads folder" equivalent; pick whichever the rest of the iOS app already uses for file exports |

---

# SOURCE OF TRUTH

Every factual claim in this document is traceable to a specific file/class/method in the
Android repo `WellnessCRM` (package `com.crm.enhance_wellness`), cited inline as
`relative/path/File.kt:line` or `relative/path/File.kt:startLine-endLine` next to the claim it
supports. Where a claim could not be verified from the traced files, it is listed under an
**Unknown / Requires Verification** heading in that Part's Section 23, with a note on what to
check and where. Nothing in this document was inferred from naming conventions, comments in
other repos, or assumptions about "how Android apps usually work" — every DTO field, mapper
transform, error-code string, and UI condition was read directly from the source file cited
next to it. This document reflects the codebase as of commit `29de9a3` ("Integrated the api of
packages and its ui.") on branch `karan_dev`, 2026-09-02.
