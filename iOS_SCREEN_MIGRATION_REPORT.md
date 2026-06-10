# WellnessCRM Patient App — iOS Screen Migration Report
# Complete Screen-by-Screen iOS 16+ Blueprint

**Generated:** 2026-06-09  
**Android Source:** `com.globussoft.wellness.patient` (Kotlin + Jetpack Compose + Material 3)  
**iOS Target:** Swift + SwiftUI + iOS 16.0 + MVVM + Clean Architecture  
**Purpose:** Complete UI reconstruction blueprint for iOS engineers — no Android code inspection required.

---

## Table of Contents

1. [Complete Screen Inventory](#1-complete-screen-inventory)
2. [Android → iOS Screen Mapping Matrix](#2-android--ios-screen-mapping-matrix)
3. [Screen-by-Screen Functional Documentation](#3-screen-by-screen-functional-documentation)
   - 3.1 SplashScreen
   - 3.2 LoginScreen
   - 3.3 RegisterScreen
   - 3.4 DashboardScreen
   - 3.5 BookAppointmentScreen
   - 3.6 MyAppointmentsScreen
   - 3.7 VisitHistoryScreen
   - 3.8 WaitlistScreen
   - 3.9 CatalogTabScreen
   - 3.10 FinanceTabScreen
   - 3.11 PrescriptionsScreen
   - 3.12 PrescriptionPdfScreen
   - 3.13 TreatmentPlansScreen
   - 3.14 ConsentFormsScreen
   - 3.15 ConsentFormPdfScreen
   - 3.16 MembershipsScreen
   - 3.17 WalletScreen
   - 3.18 GiftCardsScreen
   - 3.19 LoyaltyScreen
   - 3.20 ProfileScreen
   - 3.21 NotificationInboxScreen
   - 3.22 NotificationSettingsScreen
4. [SwiftUI View Hierarchy Report](#4-swiftui-view-hierarchy-report)
5. [Navigation Architecture Report](#5-navigation-architecture-report)
6. [Reusable Component Inventory](#6-reusable-component-inventory)
7. [Design System Migration Report](#7-design-system-migration-report)
8. [iOS 16+ Native UI Recommendations](#8-ios-16-native-ui-recommendations)
9. [Screen Complexity Matrix](#9-screen-complexity-matrix)
10. [Estimated Development Effort Per Screen](#10-estimated-development-effort-per-screen)

---

## 1. Complete Screen Inventory

### 1.1 Full Screen Inventory Table

| # | Module | Android Screen | Type | Purpose | User Role | Route |
|---|--------|---------------|------|---------|-----------|-------|
| 1 | auth | SplashScreen | Full Screen | Branding + auth token check | Public | `splash` |
| 2 | auth | LoginScreen | Full Screen | Email + password login | Unauthenticated | `login` |
| 3 | auth | RegisterScreen | Full Screen | New account creation | Unauthenticated | `register` |
| 4 | dashboard | DashboardScreen | Tab Root | Home overview: stats, next appt, quick actions | Patient | `tab_home` |
| 5 | booking | BookAppointmentScreen | Full Screen (4-step wizard) | Book a new appointment | Patient | `book_appointment` |
| 6 | booking | MyAppointmentsScreen | Tab Root (Bookings) | Manage upcoming / past appointments | Patient | `tab_bookings` |
| 7 | booking | VisitHistoryScreen | Sub-screen | Chronological visit history grouped by month | Patient | `visit_history` |
| 8 | booking | WaitlistScreen | Sub-screen | View + join service waitlists | Patient | `waitlist` |
| 9 | catalog | CatalogTabScreen | Tab Root (Catalog) | Browse services, categories, membership plans | Patient | `tab_catalog` |
| 10 | finance | FinanceTabScreen | Tab Root (Finance) | Payments overview, gift cards, transaction history | Patient | `tab_finance` |
| 11 | health | PrescriptionsScreen | Sub-screen | List of prescriptions (requires permission) | Patient | `prescriptions` |
| 12 | health | PrescriptionPdfScreen | Sub-screen | In-app PDF viewer for a single prescription | Patient | `prescription_pdf/{id}` |
| 13 | health | TreatmentPlansScreen | Sub-screen | Active treatment plans with sessions | Patient | `treatment_plans` |
| 14 | health | ConsentFormsScreen | Sub-screen | List of consent forms | Patient | `consent_forms` |
| 15 | health | ConsentFormPdfScreen | Sub-screen | In-app PDF viewer for consent form | Patient | `consent_form_pdf/{id}` |
| 16 | membership | MembershipsScreen | Sub-screen | Active/available membership plans | Patient | `memberships` |
| 17 | wallet | WalletScreen | Sub-screen | Wallet balance + transaction timeline | Patient | `wallet` |
| 18 | wallet | GiftCardsScreen | Sub-screen | Gift card storefront + purchase flow | Patient | `gift_cards` |
| 19 | loyalty | LoyaltyScreen | Sub-screen | Loyalty points + referrals | Patient | `loyalty` |
| 20 | profile | ProfileScreen | Tab Root (Profile) | View/edit profile, change password, logout | Patient | `tab_profile` |
| 21 | notifications | NotificationInboxScreen | Sub-screen | FCM-backed notification list | Patient | `notifications` |
| 22 | notifications | NotificationSettingsScreen | Sub-screen | Per-category/channel notification toggles, quiet hours | Patient | `notification_settings` |

### 1.2 Overlay / Sheet / Dialog Inventory

| # | Parent Screen | Name | Type | Trigger |
|---|--------------|------|------|---------|
| A | VisitHistoryScreen | VisitDetailSheet | ModalBottomSheet | Tap visit card |
| B | WaitlistScreen | AddWaitlistSheet | ModalBottomSheet | FAB tap |
| C | CatalogTabScreen | ServiceDetailSheet | ModalBottomSheet | Tap service card |
| D | MyAppointmentsScreen | AppointmentActionSheet | ModalBottomSheet | Tap appointment card |
| E | MyAppointmentsScreen | RescheduleSheet | ModalBottomSheet | "Reschedule" from action sheet |
| F | MyAppointmentsScreen | CancelConfirmDialog | AlertDialog | "Cancel" from action sheet |
| G | MembershipsScreen | PlanDetailSheet | ModalBottomSheet | Tap "View Details" on plan card |
| H | MembershipsScreen | JoinConfirmDialog | AlertDialog | "Join Now" in PlanDetailSheet |
| I | WalletScreen | TransactionReceiptSheet | ModalBottomSheet | Tap transaction row |
| J | GiftCardsScreen | GiftCardBuySheet | ModalBottomSheet | Tap gift card |
| K | FinanceTabScreen | PaymentActionSheet | ModalBottomSheet | Tap payment card |
| L | FinanceTabScreen | RefundConfirmDialog | AlertDialog | "Request Refund" in PaymentActionSheet |
| M | ProfileScreen | EditProfileSheet | ModalBottomSheet | "Edit Profile" button |
| N | ProfileScreen | ChangePasswordCard | Inline expanded card | Password section tapped |
| O | App-wide | WellnessTopAppBar | Persistent toolbar | Always visible (non-auth screens) |
| P | App-wide | WellnessBottomNavBar | Persistent tab bar | Always visible (non-auth screens) |

### 1.3 Reusable Components

| Component | File | Used In |
|-----------|------|---------|
| WellnessCard | `core/ui/WellnessComponents.kt` | All feature screens |
| StatusChip | `core/ui/WellnessComponents.kt` | Appointments, Waitlist, Finance |
| SectionLabel | `core/ui/WellnessComponents.kt` | Visit History, Dashboard |
| EmptyState | `core/ui/WellnessComponents.kt` | All list screens |
| ErrorState | `core/ui/WellnessComponents.kt` | All screens with network calls |
| WellnessTopAppBar | `core/navigation/WellnessTopAppBar.kt` | Global chrome |
| WellnessBottomNavBar | `core/navigation/WellnessBottomNavBar.kt` | Global chrome |

---

## 2. Android → iOS Screen Mapping Matrix

| Android Screen | iOS View Name | Nav Style | Sheet → iOS | Dialog → iOS |
|---------------|--------------|-----------|-------------|--------------|
| SplashScreen | `SplashView` | No nav chrome | — | — |
| LoginScreen | `LoginView` | `NavigationStack` root | — | — |
| RegisterScreen | `RegisterView` | `.navigationDestination` push | — | — |
| DashboardScreen | `HomeView` | `TabView` tab | — | — |
| BookAppointmentScreen | `BookAppointmentView` | `.sheet` or push from any tab | — | — |
| MyAppointmentsScreen | `MyAppointmentsView` | `TabView` tab | AppointmentActionSheet → `.confirmationDialog` + `.sheet` | CancelConfirm → `.alert` |
| VisitHistoryScreen | `VisitHistoryView` | `.navigationDestination` push | VisitDetailSheet → `.sheet(.medium)` | — |
| WaitlistScreen | `WaitlistView` | `.navigationDestination` push | AddWaitlistSheet → `.sheet(.large)` | — |
| CatalogTabScreen | `CatalogView` | `TabView` tab | ServiceDetailSheet → `.sheet(.large)` | — |
| FinanceTabScreen | `FinanceView` | `TabView` tab | PaymentActionSheet → `.confirmationDialog` | RefundConfirm → `.alert` |
| PrescriptionsScreen | `PrescriptionsView` | `.navigationDestination` push | PDF download confirm → `.alert` | — |
| PrescriptionPdfScreen | `PrescriptionPdfView` | `.navigationDestination` push | — | — |
| TreatmentPlansScreen | `TreatmentPlansView` | `.navigationDestination` push | — | — |
| ConsentFormsScreen | `ConsentFormsView` | `.navigationDestination` push | — | — |
| ConsentFormPdfScreen | `ConsentFormPdfView` | `.navigationDestination` push | — | — |
| MembershipsScreen | `MembershipsView` | `.navigationDestination` push | PlanDetailSheet → `.sheet(.large)` | JoinConfirm → `.alert` |
| WalletScreen | `WalletView` | `.navigationDestination` push | ReceiptSheet → `.sheet(.medium)` | — |
| GiftCardsScreen | `GiftCardsView` | `.navigationDestination` push | BuySheet → `.sheet(.large)` | — |
| LoyaltyScreen | `LoyaltyView` | `.navigationDestination` push | — | — |
| ProfileScreen | `ProfileView` | `TabView` tab | EditProfileSheet → `.sheet(.large)` | — |
| NotificationInboxScreen | `NotificationInboxView` | `.navigationDestination` push | — | — |
| NotificationSettingsScreen | `NotificationSettingsView` | `.navigationDestination` push | — | — |

---

## 3. Screen-by-Screen Functional Documentation

---

### 3.1 SplashScreen

#### Overview
- **Screen Name:** SplashScreen
- **Module:** auth
- **Business Purpose:** Display clinic branding while checking if a stored JWT is valid. Routes to Login or Dashboard automatically.
- **User Goal:** Transparent loading experience; no user action required.
- **Entry Points:** App launch, cold start.
- **Exit Points:** → LoginScreen (no token or expired token), → DashboardScreen (valid token), → ErrorScreen (network failure on branding fetch).

#### Android Implementation
- **Compose Screen:** `feature/auth/presentation/screen/SplashScreen.kt`
- **ViewModel:** `SplashViewModel.kt` — calls `CheckAuthStatusUseCase` + `GetTenantBrandingUseCase` on init
- **State:** `SplashUiState(isLoading: Boolean, tenantBranding: TenantBranding?, error: String?)`
- **Background colour:** hardcoded `Color(0xFF133F3E)` — matches the app_logo.jpg dark teal border exactly
- **Logo:** 70% width, 1:1 aspect ratio, `ContentScale.Fit`
- **Spinner:** `CircularProgressIndicator` (white) shown while `isLoading = true`
- **Error text:** white, semi-transparent (0.85 alpha), centered under spinner

#### UI Structure
| Element | Android | Behaviour |
|---------|---------|-----------|
| Full-screen background | `Box + background(LogoBgColor #133F3E)` | Immersive, edge-to-edge |
| App logo | `Image(R.drawable.app_logo)` 70% width | Centered, aspect 1:1 |
| Loading indicator | `CircularProgressIndicator` white | Visible when isLoading |
| Error text | `Text` white 85% alpha | Visible when error != null |

#### iOS Equivalent Mapping
| Android | iOS 16+ |
|---------|---------|
| `Box + background(Color)` | `ZStack` with `.background(Color)` |
| `Image + painter` | `Image("app_logo")` or `AsyncImage` |
| `CircularProgressIndicator` | `ProgressView()` |
| `Column` | `VStack` |

#### SwiftUI View Hierarchy
```
SplashView
└── ZStack
    ├── Color(hex: "#133F3E").ignoresSafeArea()
    └── VStack(spacing: 40)
        ├── Image("app_logo")
        │   └── .resizable().scaledToFit().frame(maxWidth: UIScreen.main.bounds.width * 0.70)
        ├── if isLoading: ProgressView().tint(.white)
        └── if let error: Text(error).foregroundColor(.white.opacity(0.85))
```

#### State Management
```swift
@MainActor
class SplashViewModel: ObservableObject {
    @Published var isLoading = true
    @Published var error: String?
    
    // On appear: checkAuthStatus() → route to Login or Dashboard
    // On appear: fetchTenantBranding() → store brand color in AppStorage
}
```

#### Navigation
- iOS: Use `@SceneStorage` or `@EnvironmentObject` `AppRouter` to set `.login` or `.dashboard` state. No NavigationStack — this is the root.
- After 0.5s minimum display, auto-navigate based on token validity.

#### Accessibility
- Logo: `accessibilityLabel("Wellness app logo")`
- ProgressView: `accessibilityLabel("Loading")`
- Background: decorative, `accessibilityHidden(true)`

---

### 3.2 LoginScreen

#### Overview
- **Screen Name:** LoginScreen
- **Module:** auth
- **Business Purpose:** Authenticate a patient using email and password. Login calls `POST /api/auth/login` (absolute path, bypasses wellness base URL).
- **User Goal:** Sign in to their health portal.
- **Entry Points:** SplashScreen (no token), RegisterScreen (back/sign-in link), deep link.
- **Exit Points:** → DashboardScreen (success), → RegisterScreen (sign-up tap).

#### Android Implementation
- **Compose Screen:** `feature/auth/presentation/screen/LoginScreen.kt`
- **ViewModel:** `LoginViewModel.kt`
- **State:** `LoginUiState(email, password, isPasswordVisible, isLoading, error, smsUnavailable, smsBannerDismissed)`
- **Events:** `EmailChanged`, `PasswordChanged`, `TogglePasswordVisibility`, `Submit`, `NavigateToRegister`, `DismissSmsBanner`
- **Init behaviour:** `CheckSmsAvailabilityUseCase` called on init; sets `smsUnavailable` banner if `GET /portal/health` returns `{ smsConfigured: false }`

#### UI Structure
| Element | Details |
|---------|---------|
| Background | `MaterialTheme.colorScheme.background` (cream `#F5F0E8`) |
| Title | "Welcome back" — `headlineMedium` (Poppins), primary colour |
| Subtitle | "Sign in to access your health portal" — `bodyMedium`, muted |
| Email field | `OutlinedTextField`, `keyboardType = Email`, `imeAction = Next` |
| Password field | `OutlinedTextField`, password visibility toggle (eye icon), `imeAction = Done` |
| Error text | `bodySmall`, `colorScheme.error`, shown when `state.error != null` |
| Sign In button | `Button`, full-width, 52dp height, extraLarge shape (24dp radius), loading spinner replaces text |
| Sign Up link | `Row`: "Don't have an account?" + tappable "Sign up" in primary colour |
| SMS unavailable banner | Not shown unless `smsUnavailable && !smsBannerDismissed` (informational notice) |

#### iOS Equivalent Mapping
| Android | iOS 16+ |
|---------|---------|
| `OutlinedTextField` (email) | `TextField` with `textContentType(.emailAddress)`, `keyboardType(.emailAddress)` |
| `OutlinedTextField` (password) + eye icon | `SecureField` toggled with `TextField` via `@State isPasswordVisible` |
| `Button` full-width | `Button` with `.buttonStyle(.wellnessPrimary)` (custom) |
| `CircularProgressIndicator` in button | `ProgressView().tint(.white)` inside button label |
| `Column + verticalScroll` | `ScrollView { VStack }` |

#### SwiftUI View Hierarchy
```
LoginView
└── ScrollView
    └── VStack(alignment: .leading, spacing: 16)
        ├── Text("Welcome back") — .font(.wellness(.headlineMedium)).foregroundColor(.wellnessPrimary)
        ├── Text("Sign in to access your health portal") — .font(.wellness(.bodyMedium)).foregroundColor(.secondary)
        ├── TextField("Email", text: $email)
        │   └── .textContentType(.emailAddress).keyboardType(.emailAddress)
        ├── PasswordFieldView(text: $password, isVisible: $isPasswordVisible, label: "Password")
        ├── if let error: Text(error).foregroundColor(.red).font(.caption)
        ├── Spacer(minLength: 4)
        ├── Button("Sign In") { viewModel.submit() }
        │   └── .frame(maxWidth: .infinity).frame(height: 52).wellnessPrimaryButtonStyle()
        │   └── overlay: if isLoading { ProgressView().tint(.white) }
        └── HStack(spacing: 4)
            ├── Text("Don't have an account? ").font(.caption).foregroundColor(.secondary)
            └── Button("Sign up") { viewModel.navigateToRegister() }.font(.caption).foregroundColor(.wellnessPrimary)
        └── .padding(.horizontal, 24).padding(.vertical, 48)
```

#### State Management
```swift
@MainActor
class LoginViewModel: ObservableObject {
    @Published var email = ""
    @Published var password = ""
    @Published var isPasswordVisible = false
    @Published var isLoading = false
    @Published var error: String?
    @Published var smsUnavailable = false
    @Published var smsBannerDismissed = false
    
    // Intents
    func submit() async { ... }        // POST /api/auth/login → save JWT → navigate
    func navigateToRegister() { ... }  // fire navigation event
    func checkSmsAvailability() async { ... }  // GET /portal/health on appear
}
```

#### Navigation
- Push `RegisterView` via `NavigationStack` path append.
- On success: replace entire navigation stack with `DashboardView` (via `AppRouter.currentRoute = .dashboard`).

#### Accessibility
- Email field: `accessibilityLabel("Email address")`
- Password field: `accessibilityLabel("Password")`, `accessibilityHint("Double tap to toggle visibility")`
- Sign In button: `accessibilityLabel(isLoading ? "Signing in" : "Sign In")`
- Support Dynamic Type on all text elements.

---

### 3.3 RegisterScreen

#### Overview
- **Screen Name:** RegisterScreen
- **Module:** auth
- **Business Purpose:** Create a new patient account. Calls `POST /api/auth/customer/register` with `{ email, password, name, registrationTenantId }`.
- **User Goal:** Self-register before first appointment.
- **Entry Points:** LoginScreen (sign-up tap).
- **Exit Points:** → DashboardScreen (success), → LoginScreen (back/sign-in tap).

#### Android Implementation
- **Compose Screen:** `RegisterScreen.kt`
- **ViewModel:** `RegisterViewModel.kt`
- **State:** `RegisterUiState(name, email, password, confirmPassword, isPasswordVisible, isLoading, error)`
- **Events:** `NameChanged`, `EmailChanged`, `PasswordChanged`, `ConfirmPasswordChanged`, `TogglePasswordVisibility`, `Submit`, `NavigateToLogin`
- **Validation:** password == confirmPassword checked before API call
- **`registrationTenantId`** injected from `BuildConfig.TENANT_ID`

#### UI Structure
| Element | Details |
|---------|---------|
| Title | "Create your account" — headlineMedium, primary |
| Subtitle | "Join your clinic's health portal" — bodyMedium, muted |
| Full name field | OutlinedTextField, `capitalization = Words`, `imeAction = Next` |
| Email field | OutlinedTextField, email keyboard, `imeAction = Next` |
| Password field | OutlinedTextField with eye toggle, `imeAction = Next` |
| Confirm password | OutlinedTextField (same visibility toggle as password), `imeAction = Done` |
| Error text | bodySmall, error colour |
| Create Account button | Full-width, 52dp, extraLarge shape, spinner while loading |
| Sign in link | "Already have an account? Sign in" |

#### SwiftUI View Hierarchy
```
RegisterView
└── ScrollView
    └── VStack(alignment: .leading, spacing: 16)
        ├── Text("Create your account") — headlineMedium, primary
        ├── Text("Join your clinic's health portal") — bodyMedium, secondary
        ├── Spacer(minLength: 8)
        ├── TextField("Full name *", text: $name).textContentType(.name).autocapitalization(.words)
        ├── TextField("Email *", text: $email).textContentType(.emailAddress).keyboardType(.emailAddress)
        ├── PasswordFieldView(text: $password, isVisible: $isPasswordVisible, label: "Password *")
        ├── PasswordFieldView(text: $confirmPassword, isVisible: $isPasswordVisible, label: "Confirm password *")
        ├── if let error: Text(error).foregroundColor(.red).font(.caption)
        ├── Spacer(minLength: 4)
        ├── Button("Create Account") { viewModel.submit() }
        │   └── .frame(maxWidth: .infinity).frame(height: 52).wellnessPrimaryButtonStyle()
        └── HStack { Text("Already have an account? ") + Button("Sign in") }
        └── .padding(.horizontal, 24).padding(.vertical, 32)
```

---

### 3.4 DashboardScreen

#### Overview
- **Screen Name:** DashboardScreen
- **Module:** dashboard
- **Business Purpose:** Home tab showing time-based greeting, 3 stat tiles, next appointment banner, quick-action grid (5 tiles), and Today At A Glance card. Also exposes global search bar via top app bar.
- **User Goal:** Get a quick overview of their health status and navigate to key actions.
- **Entry Points:** App start (after login), BottomNav "Home" tab.
- **Exit Points:** → BookAppointmentScreen, → WalletScreen, → MembershipsScreen, → LoyaltyScreen, → PrescriptionsScreen, → TreatmentPlansScreen, → ProfileScreen (edit), → MyAppointmentsScreen.

#### Android Implementation
- **Compose Screen:** `DashboardScreen.kt`
- **ViewModel:** `DashboardViewModel.kt`
- **State:** `DashboardUiState(isLoading, greeting, customerName, walletBalance, membershipStatus, loyaltyPoints, nextAppointment, todayVisitCount, pendingCount, error)`
- **API:** `GET /my-transactions` for wallet balance; `GET /portal/appointments?bucket=upcoming` for next appointment; `GET /appointments/my-memberships` for membership status
- **Greeting logic:** time-of-day based ("Good morning", "Good afternoon", "Good evening") + patient name from `EncryptedPrefsManager`
- **Stats Row:** 3 chips — Wallet (₹ balance), Membership (Active/—), Loyalty (points)
- **Next Appointment Banner:** Shows next upcoming appointment with doctor name, date, service; "Book Now" CTA if no appointment; cancel action
- **Today At A Glance card:** `todayVisitCount` and `pendingCount`
- **Quick Actions Grid:** 5 `MenuTile` items: Book Appointment, My Prescriptions, My Memberships, Loyalty & Referrals, Visit History
- **Waitlist tile:** shown in Appointments section

#### UI Structure
| Element | Details |
|---------|---------|
| Background | `colorScheme.background` |
| Pull-to-refresh | `PullRefreshIndicator` |
| Greeting | "Good morning, [Name]" — titleLarge |
| CUSTOMER badge | Small chip/badge below greeting |
| Stats row | 3 horizontal `StatChip` cards (Wallet / Membership / Loyalty) |
| Next appt banner | `WellnessCard` with doctor, date, service, cancel icon |
| Book Now CTA | Shown when no upcoming appointment |
| Today At A Glance | Card with today's counts |
| Section headers | "Clinical" and "Catalog" sections |
| Quick action grid | 2-column grid of `MenuTile` items |
| Waitlist tile | Entry in Appointments section |
| Search bar | Animated visibility (slides in from top when activated from TopAppBar search icon) |

#### iOS Equivalent Mapping
| Android | iOS |
|---------|-----|
| `LazyColumn` | `List` or `ScrollView { LazyVStack }` |
| `PullRefreshIndicator` | `.refreshable {}` modifier |
| `LazyVerticalGrid(2 cols)` | `LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())])` |
| `WellnessCard` | Custom card container with `.background(.surfaceContainerLow).cornerRadius(12)` |
| `AnimatedVisibility` search bar | `.animation(.easeInOut)` + `@State showSearch` |

#### SwiftUI View Hierarchy
```
HomeView (TabView tab 0)
├── NavigationStack
└── ScrollView
    └── .refreshable { await viewModel.refresh() }
    └── LazyVStack(alignment: .leading, spacing: 20)
        ├── if showSearch: SearchBarView(text: $searchQuery) — animated
        ├── GreetingView(greeting: state.greeting, name: state.customerName)
        │   └── VStack(alignment: .leading)
        │       ├── Text(state.greeting).font(.titleLarge)
        │       └── Badge("CUSTOMER").font(.caption)
        ├── StatsRowView(wallet: state.walletBalance, membership: state.membershipStatus, loyalty: state.loyaltyPoints)
        │   └── HStack(spacing: 12)
        │       ├── StatChip("Wallet", value: "₹\(wallet)")
        │       ├── StatChip("Membership", value: membershipStatus)
        │       └── StatChip("Loyalty", value: "\(loyaltyPoints) pts")
        ├── if let next = state.nextAppointment: NextAppointmentCard(appointment: next)
        │   else: BookNowCard { viewModel.bookNow() }
        ├── TodayAtAGlanceCard(todayCount: state.todayVisitCount, pendingCount: state.pendingCount)
        ├── SectionLabel("Clinical")
        └── LazyVGrid(columns: [.flexible(), .flexible()])
            ├── MenuTile("Book Appointment", icon: "calendar.badge.plus") { viewModel.openBooking() }
            ├── MenuTile("My Prescriptions", icon: "pills") { viewModel.openPrescriptions() }
            ├── MenuTile("My Memberships", icon: "creditcard") { viewModel.openMemberships() }
            ├── MenuTile("Loyalty & Referrals", icon: "star") { viewModel.openLoyalty() }
            └── MenuTile("Visit History", icon: "clock") { viewModel.openVisitHistory() }
        └── .padding(16)
```

#### State Management
```swift
@MainActor
class DashboardViewModel: ObservableObject {
    @Published var isLoading = true
    @Published var greeting = ""
    @Published var customerName = ""
    @Published var walletBalance: Double = 0
    @Published var membershipStatus = "—"
    @Published var loyaltyPoints = 0
    @Published var nextAppointment: Appointment?
    @Published var todayVisitCount = 0
    @Published var pendingCount = 0
    @Published var error: String?
    
    func loadDashboard() async { ... }    // parallel: transactions + appointments + memberships
    func cancelAppointment(_ id: Int) async { ... }
    func refresh() async { await loadDashboard() }
}
```

---

### 3.5 BookAppointmentScreen

#### Overview
- **Screen Name:** BookAppointmentScreen (4-step wizard)
- **Module:** booking
- **Business Purpose:** Guide patient through 4-step appointment booking: (1) Service selection, (2) Doctor selection, (3) Date/time selection, (4) Reason + membership + confirm.
- **User Goal:** Book an appointment with a specific service, doctor, date/time, and reason.
- **Entry Points:** Dashboard CTA, CatalogTabScreen "Book this service" button, deep link `wellnesspatient://screen/book`.
- **Exit Points:** → Success state (shows confirmation card), → MyAppointmentsScreen.

#### Android Implementation
- **Compose Screen:** `BookAppointmentScreen.kt`
- **ViewModel:** `BookAppointmentViewModel.kt`
- **State:** `BookAppointmentUiState(step: Int 1-4, products: List<Product>, doctors: List<DoctorOption>, selectedProductId?, selectedDoctorId?, selectedDate: Long?, selectedTime: String?, reason: String, membershipId?: Int, isLoading, error, bookingSuccess?: BookAppointmentResponse)`
- **APIs:** `GET /services?public=true` → products, `GET /doctors/availability?date=YYYY-MM-DD` → doctors, `POST /portal/appointments/book`
- **Date format:** `YYYY-MM-DD` (NOT ISO8601 with time — critical bug history: was broken with time component)

#### Step 1 — Service Selection
| Element | Details |
|---------|---------|
| Section header | "Select a Service" |
| Grid | `GridCells.Adaptive(156dp)` — service cards |
| ProductCard | Name (`titleSmall`), category label (`bodySmall`), price (`labelLarge`) |
| Search bar | Filters grid in real-time |
| Loading state | `CircularProgressIndicator` |

#### Step 2 — Doctor Selection
| Element | Details |
|---------|---------|
| Date picker | `DatePickerDialog` Android system picker (today + future dates only) |
| Info note | "Availability updates based on selected date" |
| "No preference" option | First in list; selects `doctorId = null` |
| Doctor cards | Name, specialty, available time slots |
| Loading state | Spinner while fetching availability |

#### Step 3 — Date & Time
| Element | Details |
|---------|---------|
| Date display | Selected date from Step 2 (fixed, already chosen) |
| Time picker | `AlertDialog` wrapping `TimePicker` (Material 3 time input) |
| Selected time display | Formatted "HH:mm" string |
| Validation | Cannot proceed without both date and time |

#### Step 4 — Reason & Confirm
| Element | Details |
|---------|---------|
| Reason field | `OutlinedTextField`, minLines=3, "Reason for visit" |
| Membership picker | Dropdown — "None" or list of active membership plans |
| Summary card | Service name, doctor (or "No preference"), date+time, membership |
| Book button | Full-width, shows spinner |
| Success state | Inline card with appointment ID, patient name, doctor, date, status badge |
| Error state | Text in error colour |

#### iOS Equivalent Mapping
| Android | iOS |
|---------|-----|
| Step progress indicator (custom LinearProgressIndicator) | Custom `ProgressView` or step dots |
| `GridCells.Adaptive(156dp)` | `LazyVGrid(columns: [GridItem(.adaptive(minimum: 156))])` |
| `DatePickerDialog` | `DatePicker` in `.graphical` or `.compact` displayedComponents: `.date` inside `.sheet` |
| `TimePicker` in `AlertDialog` | `DatePicker` with `displayedComponents: .hourAndMinute` inside `.sheet` |
| `DropdownMenu` / `ExposedDropdownMenuBox` | `Picker` with `.menu` style |

#### SwiftUI View Hierarchy
```
BookAppointmentView
└── NavigationStack
    └── VStack
        ├── StepProgressIndicator(currentStep: state.step, totalSteps: 4)
        ├── switch state.step:
        │   case 1:
        │   └── ServiceSelectionStep
        │       ├── SearchBar(text: $searchQuery)
        │       └── LazyVGrid(columns: .adaptive(min: 156))
        │           └── ServiceCard(product: product) { viewModel.selectProduct(product) }
        │   case 2:
        │   └── DoctorSelectionStep
        │       ├── DatePicker("Select date", selection: $selectedDate, in: Date()..., displayedComponents: .date)
        │       └── List { ForEach(doctors) { DoctorRow(doctor: $0) { viewModel.selectDoctor($0) } } }
        │   case 3:
        │   └── DateTimeStep
        │       ├── Text("Date: \(formattedDate)")
        │       └── DatePicker("Select time", selection: $selectedTime, displayedComponents: .hourAndMinute)
        │   case 4:
        │   └── ReasonConfirmStep
        │       ├── TextField("Reason for visit", text: $reason, axis: .vertical).lineLimit(3...)
        │       ├── Picker("Membership", selection: $membershipId) { ... }.pickerStyle(.menu)
        │       ├── BookingSummaryCard(state: state)
        │       └── Button("Book Appointment") { await viewModel.book() }
        └── HStack
            ├── if step > 1: Button("Back") { viewModel.previousStep() }
            └── if step < 4: Button("Next") { viewModel.nextStep() }
```

---

### 3.6 MyAppointmentsScreen

#### Overview
- **Screen Name:** MyAppointmentsScreen (Bookings Tab)
- **Module:** booking
- **Business Purpose:** Show all appointments in 4 buckets: Upcoming, Pending, Completed, Cancelled. Allow cancel and reschedule.
- **User Goal:** Manage and track appointment status.
- **Entry Points:** BottomNav "Bookings" tab.
- **Exit Points:** → BookAppointmentScreen (FAB), → RescheduleSheet (sheet), Cancel flow (dialog).

#### Android Implementation
- **Compose Screen:** `MyAppointmentsScreen.kt`
- **ViewModel:** `MyAppointmentsViewModel.kt`
- **State:** `MyAppointmentsUiState(upcoming, past, pending, cancelled: List<Appointment>, selectedTab: Int, actionSheetAppointment?: Appointment, rescheduleSheetAppointmentId?: Int, cancelConfirmAppointmentId?: Int, isRefreshing, error)`
- **APIs:** `GET /portal/appointments?bucket=upcoming|past|pending|cancelled`
- **4 KPI cards:** Count chips for each bucket (Upcoming/Pending/Completed/Cancelled)
- **Tabs:** 4 tabs (Upcoming / Pending / Completed / Cancelled) with `TabRow`
- **Pull-to-refresh:** `PullRefreshIndicator`
- **FAB:** "+" → navigates to BookAppointmentScreen
- **Appointment card:** whole card tappable → `AppointmentActionSheet`

#### AppointmentActionSheet
- Triggered by tapping any appointment card
- Options: "View Details", "Reschedule", "Cancel appointment"
- "Reschedule" → opens `RescheduleSheet` (ModalBottomSheet with DatePickerDialog + TimePicker)
- "Cancel" → opens `CancelConfirmDialog` (AlertDialog)

#### UI Structure
| Element | Details |
|---------|---------|
| KPI row | 4 cards: Upcoming count, Pending count, Completed count, Cancelled count |
| TabRow | 4 tabs |
| Tab content | `LazyColumn` of `AppointmentCard` |
| AppointmentCard | Service name, doctor name, date, status chip — full card tappable |
| FAB | Bottom-end, "+" icon, primary color |
| Loading | CircularProgressIndicator centered |
| Empty state | Per-tab empty state text |
| LazyColumn bottom padding | 96dp (clears FAB) |

#### iOS Equivalent Mapping
| Android | iOS |
|---------|-----|
| `TabRow` (4 tabs) | `Picker` with `.segmented` style or custom tab buttons |
| `PullRefreshIndicator` | `.refreshable {}` |
| `ModalBottomSheet` (action) | `.confirmationDialog` |
| `AlertDialog` (cancel confirm) | `.alert("Cancel Appointment?", isPresented:)` |
| `ModalBottomSheet` (reschedule) | `.sheet(isPresented:) { RescheduleView }` |
| FAB | `overlay { Button { } }` bottom-trailing with SF Symbols |

#### SwiftUI View Hierarchy
```
MyAppointmentsView (TabView tab 1)
└── NavigationStack
    └── ZStack(alignment: .bottomTrailing)
        ├── VStack
        │   ├── KPIRowView(upcoming: n, pending: n, completed: n, cancelled: n)
        │   │   └── ScrollView(.horizontal) { HStack { ForEach(kpis) { KPICard } } }
        │   ├── AppointmentTabPicker(selectedTab: $selectedTab)
        │   │   └── Picker("", selection: $selectedTab) { ... }.pickerStyle(.segmented)
        │   └── List
        │       └── ForEach(currentTabAppointments) { appointment in
        │           AppointmentCard(appointment: appointment)
        │               .onTapGesture { viewModel.selectAppointment(appointment) }
        │               .swipeActions { Button("Cancel") { } }
        │       }
        │       └── .refreshable { await viewModel.refresh() }
        └── FABButton(icon: "plus") { viewModel.bookNew() }
            └── .padding(.bottom, 16).padding(.trailing, 16)
    └── .sheet(item: $rescheduleAppointment) { RescheduleView(appointment: $0) }
    └── .confirmationDialog("", isPresented: $showActionSheet) { ... }
    └── .alert("Cancel Appointment?", isPresented: $showCancelConfirm) { ... }
```

---

### 3.7 VisitHistoryScreen

#### Overview
- **Screen Name:** VisitHistoryScreen
- **Module:** booking
- **Business Purpose:** Show chronological history of all clinic visits, grouped by month.
- **User Goal:** Review past visits and see details (service, doctor, amount, status).
- **Entry Points:** Dashboard quick action, BottomNav sub-navigation.
- **Exit Points:** Back to Dashboard/Bookings tab.

#### Android Implementation
- **Compose Screen:** `VisitHistoryScreen.kt`
- **ViewModel:** `VisitHistoryViewModel.kt`
- **State:** `VisitHistoryUiState(isLoading, error, visits: List<Visit>, selectedVisit?: Visit)`
- **API:** `GET /portal/visits` (limit 50)
- **Grouping:** `visits.groupBy { DateUtil.toDisplayMonthYear(it.visitDate) }` — sticky section headers
- **Detail sheet:** tapping a card sets `selectedVisit` → `VisitDetailSheet` shown as `ModalBottomSheet`

#### VisitDetailSheet contents
- Title "Visit Details"
- `HorizontalDivider`
- Rows: Service, Doctor, Date, Status, Location (if present), Type (if present), Amount (if present)

#### SwiftUI View Hierarchy
```
VisitHistoryView
└── NavigationStack
    └── Group:
        case loading: ProgressView().centered()
        case error: ErrorStateView(message: error) { viewModel.refresh() }
        case empty: EmptyStateView("No visits yet")
        default:
        List
        └── ForEach(groupedVisits.keys) { month in
            Section(month) {
                ForEach(groupedVisits[month]!) { visit in
                    VisitRow(visit: visit)
                        .onTapGesture { viewModel.selectVisit(visit) }
                }
            }
        }
        .listStyle(.insetGrouped)
        .refreshable { await viewModel.refresh() }
    └── .sheet(item: $selectedVisit) { visit in
        VisitDetailSheet(visit: visit) { viewModel.dismissDetail() }
            .presentationDetents([.medium])
    }
```

---

### 3.8 WaitlistScreen

#### Overview
- **Screen Name:** WaitlistScreen
- **Module:** booking
- **Business Purpose:** Show the patient's active waitlist entries. Allow adding new entries via a bottom sheet form.
- **User Goal:** Join a waitlist for a service when no immediate appointment slots are available.
- **Entry Points:** Dashboard Appointments section tile, deep link `wellnesspatient://screen/waitlist`.
- **Exit Points:** Back navigation.

#### Android Implementation
- **APIs:** `GET /waitlist` (list), `POST /waitlist` (add — requires `patientId` in body, fetched from EncryptedPrefsManager)
- **State:** `WaitlistUiState(isLoading, error, entries, services, showAddSheet, selectedServiceId, formNotes, isSubmitting, formError)`
- **FAB:** primary-coloured, bottom-end, opens `AddWaitlistSheet`

#### AddWaitlistSheet
- `ExposedDropdownMenuBox` — services dropdown (fetched from `GET /services?public=true`)
- `OutlinedTextField` — notes (optional), 3+ lines
- Submit button — disabled if no service selected or `isSubmitting`
- Error text below form

#### SwiftUI View Hierarchy
```
WaitlistView
└── NavigationStack
    └── ZStack(alignment: .bottomTrailing)
        ├── Group:
        │   case loading: ProgressView()
        │   case error + empty: ErrorStateView { viewModel.load() }
        │   case empty: EmptyStateView("You are not on any waitlist yet.\nTap + to add yourself.")
        │   default:
        │   List
        │   └── ForEach(entries) { WaitlistEntryRow(entry: $0) }
        │       .listStyle(.insetGrouped)
        │       .refreshable { await viewModel.load() }
        └── FABButton(icon: "plus") { viewModel.showAddSheet = true }
    └── .sheet(isPresented: $viewModel.showAddSheet) {
        AddWaitlistView(state: viewModel.addState) { event in viewModel.handle(event) }
            .presentationDetents([.large])
    }
```

---

### 3.9 CatalogTabScreen

#### Overview
- **Screen Name:** CatalogTabScreen (Catalog Tab)
- **Module:** catalog
- **Business Purpose:** Three-tab browsing experience: Services (2-col grid with detail sheet), Categories (list that cross-filters Services), Memberships (Available/Mine toggle).
- **User Goal:** Discover services and membership plans; book from catalog.
- **Entry Points:** BottomNav "Catalog" tab.
- **Exit Points:** → BookAppointmentScreen (from service detail or membership).

#### Android Implementation
- **Compose Screen:** `CatalogTabScreen.kt`
- **3 inner tabs:** Services | Categories | Memberships
- **ViewModels:** `CatalogViewModel` (services + categories), `MembershipsViewModel` (passed as lambda)

#### Services Tab
| Element | Details |
|---------|---------|
| Search bar | `SearchBar` composable, real-time filter |
| Grid | `GridCells.Adaptive(156dp)`, 2 implicit columns on most phones |
| Service card | Name (`titleSmall`), category label, price |
| Tap → ServiceDetailSheet | Category label, severity pill, 3-box stats, description, "Book this service" CTA, "Service ID #N" footer, "Got it" close |

#### Categories Tab
| Element | Details |
|---------|---------|
| List | `LazyColumn`, pull-to-refresh |
| Category row | Name, service count chip |
| Tap | Switches to Services tab + sets active `FilterChip` (`widthIn(max=280dp)`) |
| Active filter chip | Shows "Category: [name]" above grid; tapping × clears |

#### Memberships Tab (inline)
| Element | Details |
|---------|---------|
| Toggle | "Available" / "Mine" toggle buttons |
| Available view | `ProfessionalPlanCard` — Diamond/Gold/Platinum coloured cards with perks list, "View Details" ghost button + "Join Now" button |
| Mine view | Active memberships list |
| PlanDetailSheet | Tapping "View Details" — ModalBottomSheet with plan details, "Join Now" + confirm dialog |

#### iOS Equivalent Mapping
| Android | iOS |
|---------|-----|
| `TabRow` + `HorizontalPager` | `Picker(.segmented)` + `TabView` with `.tabViewStyle(.page)` or manual state |
| `GridCells.Adaptive(156dp)` | `LazyVGrid(columns: [GridItem(.adaptive(minimum: 156))])` |
| `ModalBottomSheet` | `.sheet(item:) { }.presentationDetents([.large])` |
| `FilterChip` (active category) | Custom chip with dismiss button |
| `ExposedDropdownMenuBox` | `Picker(.menu)` |

---

### 3.10 FinanceTabScreen

#### Overview
- **Screen Name:** FinanceTabScreen (Finance Tab)
- **Module:** finance
- **Business Purpose:** Three-tab finance view: Payments (KPI row + list), Gift Cards (inline GiftCardsScreen), Transactions (inline WalletScreen).
- **User Goal:** Review payment history, buy gift cards, view transaction timeline.
- **Entry Points:** BottomNav "Finance" tab.
- **Exit Points:** → Razorpay checkout (gift card purchase).

#### Android Implementation
- **3 inner tabs:** Payments | Gift Cards | Transactions (embedded inline — no navigate-away flash)
- **Payments tab:** KPI row (Collected / Pending / Failed totals), `FlowRow` (3 per row), LazyColumn payment cards
- **Payment card:** description (no maxLines), amount, status chip, date
- **Tap payment card → PaymentActionSheet:** "View Invoice" + "Request Refund"
- **Refund → AlertDialog confirm → POST /api/payments/:id/refund**
- **APIs:** `GET /api/payments`, `GET /api/payments/config`

#### iOS Equivalent Mapping
| Android | iOS |
|---------|-----|
| `FlowRow(maxItemsInEachRow=3)` | `LazyVGrid(columns: Array(repeating: .flexible(), count: 3))` |
| Embedded sub-screens | Child views inside tab, not pushed |
| `.confirmationDialog` | Payment action sheet |

---

### 3.11 PrescriptionsScreen

#### Overview
- **Screen Name:** PrescriptionsScreen
- **Module:** health
- **Business Purpose:** List patient prescriptions. Requires `my_prescriptions.read` permission.
- **User Goal:** View medication details and download/view PDF.
- **Entry Points:** Dashboard quick action, deep link `wellnesspatient://screen/prescriptions`.
- **Exit Points:** → PrescriptionPdfScreen, back.

#### Android Implementation
- **API:** `GET /portal/prescriptions` → `List<PrescriptionDto>`
- **CRITICAL:** `PrescriptionDto.drugs` is a `String` (JSON-encoded array), NOT a real array. Must be double-parsed.
- **Permission check:** `PatientPermissions.has("my_prescriptions.read")` — if false, show "Access restricted" state
- **Caching:** `CachedPrescription` Room entity — PDF bytes cached for 7 days
- **PDF download confirm dialog:** `AlertDialog` before downloading
- **Pull-to-refresh:** enabled

#### UI Structure
| Element | Details |
|---------|---------|
| Permission gate | If no `my_prescriptions.read` → "Access restricted" empty state |
| Loading | `CircularProgressIndicator` |
| Prescription card | Doctor name, service, date, drug count, "View PDF" button |
| PDF confirm dialog | "Download and view prescription PDF?" — Yes/No |
| → PrescriptionPdfScreen | Navigation with prescription ID |

#### SwiftUI View Hierarchy
```
PrescriptionsView
└── NavigationStack
    └── Group:
        case !hasPermission: PermissionGateView("Prescriptions require additional access")
        case loading: ProgressView()
        case error: ErrorStateView
        default:
        List
        └── ForEach(prescriptions) { rx in
            PrescriptionRow(prescription: rx)
                .swipeActions { Button("View PDF") { ... } }
        }
        .refreshable { await viewModel.refresh() }
    └── .alert("View PDF?", isPresented: $showPdfConfirm) {
        Button("Download & View") { viewModel.openPdf(selectedId) }
        Button("Cancel", role: .cancel) {}
    }
    └── .navigationDestination(for: Int.self) { id in PrescriptionPdfView(prescriptionId: id) }
```

---

### 3.12 PrescriptionPdfScreen

#### Overview
- **Screen Name:** PrescriptionPdfScreen
- **Module:** health
- **Business Purpose:** Display prescription PDF inside the app using Android `PdfRenderer`. Prescription PDF bytes are served via `FileProvider`.
- **User Goal:** Read prescription without leaving the app.
- **Entry Points:** PrescriptionsScreen "View PDF" action.
- **Exit Points:** Back button.

#### Android Implementation
- **API:** `GET /portal/prescriptions/{id}/pdf` → `ResponseBody` (binary PDF)
- **Rendering:** Android `PdfRenderer` — renders each page to a `Bitmap`, displayed in `LazyColumn`
- **Caching:** PDF bytes stored as `pdfBytes: ByteArray?` in Room `cached_prescriptions` — 7-day TTL
- **FileProvider:** `{appId}.wellness_pdfs` authority — serves PDF only within app, never auto-saved to Downloads

#### iOS Implementation Notes
- **PDF rendering:** Use `PDFKit` (`PDFView` wrapped in `UIViewRepresentable`)
- **Storage:** Use `FileManager` app-private Documents directory (equivalent security to Room BLOB)
- **No UIKit bypass needed:** PDFKit is fully usable from SwiftUI via `UIViewRepresentable`
- **7-day cache:** Track `pdfCachedAt` in SwiftData/CoreData entity

#### SwiftUI View Hierarchy
```
PrescriptionPdfView(prescriptionId: Int)
└── NavigationStack
    └── Group:
        case loading: ProgressView("Loading prescription...")
        case error: ErrorStateView
        case loaded(url: URL):
        PDFKitView(url: url)   // UIViewRepresentable wrapping PDFView
            .ignoresSafeArea()
```

```swift
struct PDFKitView: UIViewRepresentable {
    let url: URL
    func makeUIView(context: Context) -> PDFView {
        let pdfView = PDFView()
        pdfView.autoScales = true
        pdfView.document = PDFDocument(url: url)
        return pdfView
    }
    func updateUIView(_ uiView: PDFView, context: Context) {}
}
```

---

### 3.13 TreatmentPlansScreen

#### Overview
- **Screen Name:** TreatmentPlansScreen
- **Module:** health
- **Business Purpose:** Show active treatment plans with session counts and progress.
- **User Goal:** Track ongoing clinical treatment.
- **Entry Points:** Dashboard quick action (Clinical section), deep link.
- **Exit Points:** Back navigation.

#### Android Implementation
- **API:** `GET /patients/{patientId}/treatment-plans` — patientId from `EncryptedPrefsManager`
- **State:** `TreatmentPlanUiState(isLoading, error, plans: List<TreatmentPlan>)`
- **Card contents:** Plan name, status chip, session counts (completed/total), progress indicator

#### SwiftUI View Hierarchy
```
TreatmentPlansView
└── NavigationStack
    └── List
        └── ForEach(plans) { plan in
            TreatmentPlanRow(plan: plan)
        }
        .refreshable { await viewModel.refresh() }
```

---

### 3.14 ConsentFormsScreen

#### Overview
- **Screen Name:** ConsentFormsScreen
- **Module:** health
- **Business Purpose:** List consent forms the patient has signed.
- **User Goal:** Access signed consent documents.
- **Entry Points:** Navigation sub-link from Profile or Dashboard.
- **Exit Points:** → ConsentFormPdfScreen, back.

#### Android Implementation
- **API:** `GET /patients/{patientId}/consents`
- **Card:** Form name, date signed, status, "View PDF" button

---

### 3.15 ConsentFormPdfScreen

#### Overview
- PDF viewer for consent forms. Same implementation pattern as `PrescriptionPdfScreen`.
- **API:** `GET /consents/{id}/pdf`
- **iOS:** Use `PDFKit` via `UIViewRepresentable` (identical to PrescriptionPdfView)

---

### 3.16 MembershipsScreen

#### Overview
- **Screen Name:** MembershipsScreen
- **Module:** membership
- **Business Purpose:** Show patient's active/expired memberships AND available plans to purchase. Toggle between "Mine" and "Available" tabs.
- **User Goal:** View membership benefits, track usage, and browse/join new plans.
- **Entry Points:** Dashboard stats chip, Catalog tab Memberships section, deep link.
- **Exit Points:** → PlanDetailSheet (tap plan card).

#### Android Implementation
- **APIs:** `GET /appointments/my-memberships` (active), `GET /membership-plans` (catalog)
- **State:** `MembershipsUiState(isLoading, error, myMemberships, availablePlans, activeTab: "Mine"|"Available", selectedPlan?, showPlanDetailSheet, showJoinConfirmDialog)`
- **"Mine" tab:** Active/Expired memberships, `LinearProgressIndicator` for credits remaining, "Book with this" CTA
- **"Available" tab:** `ProfessionalPlanCard` — Diamond/Gold/Platinum colour-coded cards
  - Diamond: `Color(0xFF1A237E)` deep blue
  - Gold: `Color(0xFFFFB300)` amber
  - Platinum: `Color(0xFF37474F)` blue-grey
  - Each has: plan name, price, perks list, "View Details" + "Join Now" buttons
- **PlanDetailSheet:** Full plan detail, "Join Now" button → JoinConfirmDialog
- **`MembershipDto.balance`:** always empty `[]` in current backend (known gap)
- **`MembershipPlanDto.entitlements`:** raw JSON string — must be parsed client-side

#### SwiftUI View Hierarchy
```
MembershipsView
└── NavigationStack
    └── VStack
        ├── Picker("", selection: $activeTab) {
        │   Text("Available").tag(0)
        │   Text("Mine").tag(1)
        │   }.pickerStyle(.segmented).padding()
        ├── switch activeTab:
        │   case 0:
        │   ScrollView
        │   └── LazyVStack
        │       └── ForEach(availablePlans) { plan in
        │           ProfessionalPlanCard(plan: plan, tier: plan.tier)
        │               .onTapGesture { viewModel.selectPlan(plan) }
        │       }
        │   case 1:
        │   List
        │   └── ForEach(myMemberships) { membership in
        │       MembershipRow(membership: membership)
        │       └── ProgressView(value: Double(membership.creditsUsed), total: Double(membership.creditsTotal))
        │   }
    └── .sheet(item: $selectedPlan) { plan in
        PlanDetailSheet(plan: plan) { viewModel.initiateJoin(plan) }
            .presentationDetents([.large])
    }
    └── .alert("Join Plan?", isPresented: $showJoinConfirm) { ... }
```

---

### 3.17 WalletScreen

#### Overview
- **Screen Name:** WalletScreen
- **Module:** wallet
- **Business Purpose:** Display wallet balance (4 KPI cards) and filterable transaction timeline.
- **User Goal:** Track credits, debits, gift card redemptions, and treatment payments.
- **Entry Points:** Dashboard "Wallet" stat chip, Finance tab Transactions sub-tab.
- **Exit Points:** Back navigation, → TransactionReceiptSheet.

#### Android Implementation
- **APIs:** `GET /patients/{patientId}/wallet`, `GET /my-transactions`
- **State:** `WalletUiState(isLoading, error, wallet: PatientWallet?, transactions: List<Transaction>, activeFilter: "All"|"Wallet"|"GiftCards"|"Memberships"|"Treatments", selectedTransaction?)`
- **KPI row:** `FlowRow(maxItemsInEachRow=4)` (wraps to 2×2 on narrow devices) — 4 cards: Total Credits, Total Debits, Wallet Balance, Gift Card Balance
- **KPI card:** `value → titleSmall SemiBold`, `label → bodySmall`, `padding(horizontal=8, vertical=12)`
- **Filter chips:** `LazyRow` of `FilterChip` — All / Wallet / GiftCards / Memberships / Treatments
- **Transaction list:** `LazyColumn`, each item has type icon, title, amount (green=credit, red=debit), date
- **Receipt sheet:** tapping transaction → `ModalBottomSheet` with full details
- **Pull-to-refresh:** enabled

#### iOS Equivalent Mapping
| Android | iOS |
|---------|-----|
| `FlowRow(maxItemsInEachRow=4)` | `LazyVGrid(columns: Array(repeating: .flexible(), count: 2))` (2×2) |
| `LazyRow` filter chips | `ScrollView(.horizontal) { HStack { ForEach(filters) { FilterChip } } }` |
| `ModalBottomSheet` receipt | `.sheet(item:) { }.presentationDetents([.medium])` |

#### SwiftUI View Hierarchy
```
WalletView
└── NavigationStack
    └── ScrollView
        └── .refreshable { await viewModel.refresh() }
        └── LazyVStack(spacing: 16)
            ├── WalletKPIGrid(wallet: state.wallet)
            │   └── LazyVGrid(columns: [.flexible(), .flexible()], spacing: 8)
            │       ├── KPICard("Total Credits", value: state.totalCredits, color: .green)
            │       ├── KPICard("Total Debits", value: state.totalDebits, color: .red)
            │       ├── KPICard("Wallet Balance", value: state.walletBalance, color: .primary)
            │       └── KPICard("Gift Card Balance", value: state.giftBalance, color: .orange)
            ├── ScrollView(.horizontal, showsIndicators: false)
            │   └── HStack { ForEach(WalletFilter.allCases) { FilterChip(filter: $0) } }
            └── LazyVStack
                └── ForEach(filteredTransactions) { tx in
                    TransactionRow(transaction: tx)
                        .onTapGesture { viewModel.selectTransaction(tx) }
                }
        └── .sheet(item: $viewModel.selectedTransaction) { tx in
            TransactionReceiptSheet(transaction: tx)
                .presentationDetents([.medium])
        }
```

---

### 3.18 GiftCardsScreen

#### Overview
- **Screen Name:** GiftCardsScreen
- **Module:** wallet
- **Business Purpose:** Storefront for purchasing gift cards using Razorpay. 2-column grid with search. Purchase flow: tap card → review sheet → Razorpay checkout → confirm → wallet credit.
- **User Goal:** Buy a gift card as a pre-payment or as a gift.
- **Entry Points:** Finance tab Gift Cards sub-tab, Dashboard.
- **Exit Points:** Razorpay checkout → confirmation.

#### Android Implementation
- **APIs:** `GET /giftcards/storefront`, `POST /giftcards/{id}/purchase/order`, `POST /giftcards/{id}/purchase/confirm`
- **State:** `GiftCardsUiState(isLoading, error, giftCards: List<GiftCard>, searchQuery, selectedCard?, showBuySheet, isPurchasing, purchaseSuccess?)`
- **Current implementation:** Demo denominations `₹500, ₹1000, ₹2000, ₹5000, ₹10000` (local DEMO_DENOMINATIONS) — not server-driven in current build
- **Grid:** `GridCells.Adaptive(160dp)`, colourful cards
- **Search bar:** filters by denomination/name
- **Buy sheet:** selected card summary, "Proceed to Payment" button
- **Razorpay:** Android SDK `Checkout` — `startPayment(activity, options)`
- **Confirmation:** `POST /giftcards/{id}/purchase/confirm` with Razorpay signature

#### iOS Notes — Razorpay
- Use Razorpay iOS SDK (CocoaPods: `pod 'Razorpay-Pod'` or SPM if available)
- Entry: `RazorpayCheckout.open(controller, options: [String: Any])` — requires `UIViewController`
- Wrap in `UIViewControllerRepresentable` for SwiftUI
- Delegate: `RazorpayPaymentCompletionProtocol` — `onPaymentSuccess(_ paymentId: String)` + `onPaymentError(_ code: Int, description: String)`
- After success: call `POST /giftcards/{id}/purchase/confirm` to verify and credit wallet

#### SwiftUI View Hierarchy
```
GiftCardsView
└── NavigationStack
    └── VStack
        ├── SearchBar(text: $searchQuery)
        └── LazyVGrid(columns: [GridItem(.adaptive(minimum: 160))], spacing: 16)
            └── ForEach(filteredCards) { card in
                GiftCardTile(card: card)
                    .onTapGesture { viewModel.selectCard(card) }
            }
    └── .sheet(item: $viewModel.selectedCard) { card in
        GiftCardBuySheet(card: card) { viewModel.purchase(card) }
            .presentationDetents([.medium, .large])
    }
```

---

### 3.19 LoyaltyScreen

#### Overview
- **Screen Name:** LoyaltyScreen
- **Module:** loyalty (Phase 2 UI)
- **Business Purpose:** Display loyalty points, tier, referral code, and history.
- **User Goal:** Track and redeem loyalty rewards; share referral link.
- **Entry Points:** Dashboard "Loyalty" stat chip, Profile loyalty link.

#### Android Implementation
- **API:** `GET /loyalty/{patientId}` — **SECURITY NOTE:** backend does NOT verify patientId ownership. iOS must use only the stored patientId from Keychain, never from user input.
- **State:** `LoyaltyUiState(isLoading, error, loyalty: LoyaltyData?)`
- **LoyaltyData:** points, tier, referralCode, referralCount, history

#### SwiftUI View Hierarchy
```
LoyaltyView
└── NavigationStack
    └── ScrollView
        └── LazyVStack(spacing: 20)
            ├── LoyaltyPointsCard(points: state.loyalty?.points, tier: state.loyalty?.tier)
            ├── ReferralCard(code: state.loyalty?.referralCode) { ShareLink(item: referralURL) }
            └── LoyaltyHistoryList(history: state.loyalty?.history ?? [])
```

---

### 3.20 ProfileScreen

#### Overview
- **Screen Name:** ProfileScreen (Profile Tab)
- **Module:** profile
- **Business Purpose:** View and edit patient profile. Change password. Upload/remove profile picture. DSAR export. Notification settings link. Logout.
- **User Goal:** Manage personal information and account settings.
- **Entry Points:** BottomNav "Profile" tab.
- **Exit Points:** → NotificationSettingsScreen, Logout → SplashScreen.

#### Android Implementation
- **Compose Screen:** `ProfileScreen.kt`
- **APIs (dual-layer):**
  - `GET /portal/me` → patient layer: `id, name, phone, email, dob, gender`
  - `GET /api/auth/me` → user layer: `id, name, email, role, profilePicture`
  - `PUT /api/auth/me` → update `name?, email?, currentPassword?, newPassword?` — **dob/gender/phone NOT updatable**
  - `POST /api/auth/me/profile-picture` → `Multipart` upload
  - `DELETE /api/auth/me/profile-picture`
  - `POST /portal/export` → DSAR export

#### UI Sections

**ProfileHeaderCard:**
| Element | Details |
|---------|---------|
| Avatar | Circular image (Coil AsyncImage) or initials placeholder |
| Camera overlay | Small camera FAB on avatar for photo change |
| CUSTOMER chip | Role badge |
| Remove picture | Link text if profilePicture present |

**Profile Info Card:**
| Field | API Source | Editable |
|-------|-----------|---------|
| Name | auth/me | YES (PUT /api/auth/me) |
| Email | auth/me | YES (PUT /api/auth/me) |
| Phone | portal/me | NO |
| Date of Birth | portal/me | NO (backend gap) |
| Gender | portal/me | NO (backend gap) |

**ChangePasswordCard:**
| Element | Details |
|---------|---------|
| Current password | OutlinedTextField + eye toggle |
| New password | OutlinedTextField + eye toggle |
| Confirm new password | OutlinedTextField + eye toggle |
| Local validation | password length, match check before API call |
| Save button | Calls PUT /api/auth/me with `currentPassword + newPassword` |
| Toast on success | "Password changed successfully" |

**Settings Rows:**
- Notification Settings → navigates to `NotificationSettingsScreen`
- Export My Data (DSAR) → `POST /portal/export` → confirmation Toast
- Logout → clears DataStore + EncryptedPrefs + Room + deregisters FCM token → pops to Login

#### iOS Equivalent Mapping
| Android | iOS |
|---------|-----|
| `Coil AsyncImage` (circular) | `AsyncImage` + `.clipShape(.circle)` |
| `Camera overlay` FAB | `.overlay { Button { } }` bottom-trailing on circle |
| `OutlinedTextField` + eye toggle | `SecureField` toggled with `TextField` |
| `Toast` (Snackbar) | Custom overlay banner with `withAnimation` or `Toast` library |
| Multipart photo upload | `URLRequest` with `multipart/form-data` body |
| DSAR export | `Button { await viewModel.requestExport() }` |
| Logout | `viewModel.logout()` → `AppRouter.currentRoute = .login` |

#### SwiftUI View Hierarchy
```
ProfileView (TabView tab 4)
└── NavigationStack
    └── ScrollView
        └── LazyVStack(spacing: 20)
            ├── ProfileHeaderCard
            │   └── ZStack(alignment: .bottomTrailing)
            │       ├── AsyncImage(url: profilePictureUrl)
            │       │   .clipShape(.circle).frame(80)
            │       └── Button { viewModel.showPhotoPicker = true }
            │           └── Image(systemName: "camera.fill").background(.primary)
            │       + if url != nil: Button("Remove picture") { viewModel.removePhoto() }
            │       + Badge("CUSTOMER")
            ├── ProfileInfoCard
            │   └── Form { — read-only fields + editable fields
            │       LabeledContent("Name", value: name)
            │       LabeledContent("Email", value: email)
            │       LabeledContent("Phone", value: phone)
            │       ...
            │       Button("Edit Profile") { viewModel.showEditSheet = true }
            │           .buttonStyle(.bordered)
            │   }
            ├── ChangePasswordSection
            │   └── DisclosureGroup("Change Password")
            │       └── VStack {
            │           SecureFieldWithToggle("Current Password", text: $currentPassword)
            │           SecureFieldWithToggle("New Password", text: $newPassword)
            │           SecureFieldWithToggle("Confirm Password", text: $confirmPassword)
            │           Button("Save Password") { await viewModel.changePassword() }
            │       }
            ├── WellnessSettingsRow("Notification Settings", icon: "bell") { navigate to NotificationSettings }
            ├── WellnessSettingsRow("Export My Data", icon: "square.and.arrow.up") { await viewModel.requestExport() }
            └── Button("Logout", role: .destructive) { viewModel.logout() }
    └── .sheet(isPresented: $viewModel.showEditSheet) { EditProfileView() }
    └── .photosPicker(isPresented: $viewModel.showPhotoPicker, selection: $viewModel.photoItem, matching: .images)
```

---

### 3.21 NotificationInboxScreen

#### Overview
- **Screen Name:** NotificationInboxScreen
- **Module:** notifications
- **Business Purpose:** FCM-backed inbox. Notifications are persisted to Room on receipt; shown in reverse-chronological order. Mark-read on tap, deep-link navigation on tap, 90-day eviction.
- **User Goal:** View and act on push notifications received while app was in background.
- **Entry Points:** Bell icon badge in TopAppBar, deep link.
- **Exit Points:** Deep-link destination, back.

#### Android Implementation
- **Data source:** Room `cached_notifications` table (NO network call — purely local)
- **State:** `NotificationInboxUiState(isLoading, notifications: List<CachedNotification>, unreadCount)`
- **Pull-to-refresh:** reloads from Room
- **Tap notification:** marks as read (Room update) + navigates via `DeepLinkHandler`
- **Mark all read:** option in overflow menu
- **Eviction:** 90-day TTL run on app start

#### iOS Implementation
- **Local persistence:** Use CoreData or SwiftData entity `CachedNotification`
- **FCM:** `UNUserNotificationCenter` + Firebase Messaging `application(_:didReceiveRemoteNotification:)`
- **Deep link:** custom URL scheme handler using `NavigationPath` or `OpenURLAction`
- **Badge count:** `UNUserNotificationCenter.setBadgeCount(_:)` (iOS 16+)

#### SwiftUI View Hierarchy
```
NotificationInboxView
└── NavigationStack
    └── List
        └── ForEach(notifications) { notification in
            NotificationRow(notification: notification)
                .listRowBackground(notification.isRead ? Color.clear : Color.primary.opacity(0.05))
                .onTapGesture { viewModel.tapNotification(notification) }
        }
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Mark all read") { viewModel.markAllRead() }
            }
        }
        .listStyle(.plain)
        .refreshable { viewModel.reload() }
    └── .navigationTitle("Notifications")
```

---

### 3.22 NotificationSettingsScreen

#### Overview
- **Screen Name:** NotificationSettingsScreen
- **Module:** notifications
- **Business Purpose:** Per-category and per-channel notification preferences with quiet hours. Persisted to DataStore.
- **User Goal:** Control which notifications they receive and when.
- **Entry Points:** Profile screen "Notification Settings" row.
- **Exit Points:** Back to Profile.

#### Android Implementation
- **5 category toggles:** Appointment Reminders, Health Updates, Wallet & Payments, Offers & Surveys, Treatment Updates
- **3 channel toggles:** Push Notifications, SMS (disabled if smsUnavailable), Email
- **Quiet hours:** start time + end time, enabled toggle
- **Save button:** persists to DataStore

#### SwiftUI View Hierarchy
```
NotificationSettingsView
└── NavigationStack
    └── Form
        ├── Section("Notification Categories") {
        │   Toggle("Appointment Reminders", isOn: $appointmentReminders)
        │   Toggle("Health Updates", isOn: $healthUpdates)
        │   Toggle("Wallet & Payments", isOn: $walletPayments)
        │   Toggle("Offers & Surveys", isOn: $offersSurveys)
        │   Toggle("Treatment Updates", isOn: $treatmentUpdates)
        │   }
        ├── Section("Delivery Channels") {
        │   Toggle("Push Notifications", isOn: $pushEnabled)
        │   Toggle("SMS", isOn: $smsEnabled).disabled(!smsAvailable)
        │   Toggle("Email", isOn: $emailEnabled)
        │   }
        ├── Section("Quiet Hours") {
        │   Toggle("Enable Quiet Hours", isOn: $quietHoursEnabled)
        │   if quietHoursEnabled {
        │       DatePicker("From", selection: $quietStart, displayedComponents: .hourAndMinute)
        │       DatePicker("To", selection: $quietEnd, displayedComponents: .hourAndMinute)
        │   }
        │   }
        └── Button("Save Settings") { await viewModel.save() }
    └── .navigationTitle("Notification Settings")
```

---

## 4. SwiftUI View Hierarchy Report

### 4.1 App Root

```
WellnessCRMApp
└── WindowGroup
    └── ContentView
        └── @StateObject AppRouter
        └── if router.isAuthenticated:
            MainTabView
        else:
            AuthFlow (NavigationStack)
```

### 4.2 AuthFlow

```
AuthFlow
└── NavigationStack(path: $authNavPath)
    ├── SplashView (initial)
    │   └── .onAppear { await splashVM.checkAuth() }
    └── .navigationDestination(for: AuthRoute.self) { route in
        switch route:
        case .login: LoginView()
        case .register: RegisterView()
    }
```

### 4.3 MainTabView

```
MainTabView
└── ZStack(alignment: .top)
    ├── TabView(selection: $selectedTab)
    │   ├── HomeView().tabItem { Label("Home", systemImage: "house") }.tag(0)
    │   ├── BookingsView().tabItem { Label("Bookings", systemImage: "calendar") }.tag(1)
    │   ├── CatalogView().tabItem { Label("Catalog", systemImage: "square.grid.2x2") }.tag(2)
    │   ├── FinanceView().tabItem { Label("Finance", systemImage: "indianrupeesign.square") }.tag(3)
    │   └── ProfileView().tabItem { Label("Profile", systemImage: "person") }.tag(4)
    └── WellnessTopBarView(
            clinicName: appState.clinicName,
            unreadCount: appState.unreadCount,
            isDarkMode: $appState.isDarkTheme,
            showSearch: $showSearch
        )
```

### 4.4 Navigation Destinations (per tab)

```
HomeView
└── NavigationStack
    └── .navigationDestination(for: HomeRoute.self):
        .bookAppointment → BookAppointmentView
        .prescriptions   → PrescriptionsView
        .memberships     → MembershipsView
        .visitHistory    → VisitHistoryView
        .loyalty         → LoyaltyView
        .wallet          → WalletView

BookingsView (MyAppointmentsView)
└── NavigationStack
    └── .navigationDestination(for: BookingRoute.self):
        .bookNew        → BookAppointmentView
        .visitHistory   → VisitHistoryView
        .waitlist       → WaitlistView

CatalogView
└── NavigationStack (tab-scoped)
    └── Sub-tabs managed within (no push navigation needed)

FinanceView
└── NavigationStack
    └── Sub-tabs embedded inline (GiftCardsView + WalletView rendered inline)

ProfileView
└── NavigationStack
    └── .navigationDestination(for: ProfileRoute.self):
        .notificationSettings → NotificationSettingsView
        .notifications        → NotificationInboxView
```

---

## 5. Navigation Architecture Report

### 5.1 Android Navigation Model

| Concept | Android Implementation |
|---------|----------------------|
| Tab roots | 5 `composable(Screen.Tab*.route)` destinations with `rememberNavController()` + tab-specific back stacks |
| Tab back-stack preservation | `NavOptions.restoreState = true`, `saveState = true` on tab switch |
| Sub-screen push | `navController.navigate(Screen.VisitHistory.route)` inside `LaunchedEffect` consuming `Channel<NavEvent>` |
| Deep links | `DeepLinkHandler.kt` maps `wellnesspatient://screen/{name}?id=` to Compose routes |
| Global auth check | `SplashViewModel` → `NavigateToLogin` or `NavigateToDashboard` nav event |
| Auth-to-main transition | `navController.navigate(Screen.Dashboard) { popUpTo(0) }` clears full back stack |
| Back suppression on tab roots | `TAB_ROOT_ROUTES` set — back presses on tab roots do not navigate |

### 5.2 iOS Navigation Strategy

```swift
// AppRouter.swift
enum AppRoute { case splash, auth, main }

@MainActor
class AppRouter: ObservableObject {
    @Published var route: AppRoute = .splash
    @Published var isAuthenticated: Bool = false
}

// Per-tab NavigationPath
class TabNavigator: ObservableObject {
    @Published var homePath = NavigationPath()
    @Published var bookingsPath = NavigationPath()
    @Published var catalogPath = NavigationPath()
    @Published var financePath = NavigationPath()
    @Published var profilePath = NavigationPath()
}
```

| Android Pattern | iOS 16+ Equivalent | Why |
|----------------|-------------------|-----|
| Single `NavController` shared across tabs | One `TabNavigator` with 5 `NavigationPath` | SwiftUI TabView requires per-tab path for independent back stacks |
| `popUpTo(0)` on login success | `router.route = .main` (replace root) | `@EnvironmentObject AppRouter` drives root content switch |
| `Channel<NavEvent>` consumed in `LaunchedEffect` | `@Published` navigation destination + `.onChange` or dedicated nav method on ViewModel | Direct state binding simpler in SwiftUI |
| `ModalBottomSheet` | `.sheet(isPresented:) { }.presentationDetents([.medium, .large])` | Native iOS, full presentation control |
| `AlertDialog` | `.alert("Title", isPresented: $show) { }` | Native iOS alert |
| `.confirmationDialog` | `.confirmationDialog("", isPresented:, titleVisibility: .hidden) { }` | Action sheet variant |
| Deep link `wellnesspatient://screen/x` | `.onOpenURL { url in router.handle(url) }` | Same scheme, same path tokens |
| Tab root back suppression | TabView handles this natively — back button hidden on tab roots | No custom logic needed |

### 5.3 Deep Link Routing (iOS)

```swift
// DeepLinkRouter.swift
func handle(_ url: URL) {
    guard url.scheme == "wellnesspatient", url.host == "screen" else { return }
    let screen = url.pathComponents.dropFirst().first ?? ""
    let id = URLComponents(url: url, resolvingAgainstBaseURL: false)?
        .queryItems?.first(where: { $0.name == "id" })?.value
    
    switch screen {
    case "appointments":  tabNavigator.selectedTab = 1
    case "prescriptions": tabNavigator.homePath.append(HomeRoute.prescriptions)
    case "memberships":   tabNavigator.homePath.append(HomeRoute.memberships)
    case "wallet":        tabNavigator.homePath.append(HomeRoute.wallet)
    case "book":          tabNavigator.homePath.append(HomeRoute.bookAppointment)
    case "waitlist":      tabNavigator.bookingsPath.append(BookingRoute.waitlist)
    case "notifications": tabNavigator.profilePath.append(ProfileRoute.notifications)
    default: break
    }
}
```

---

## 6. Reusable Component Inventory

### 6.1 WellnessCard

**Android:** `WellnessComponents.kt` — `WellnessCard(modifier, onClick?, content)`  
**Purpose:** Uniform card container. Rounded corners (12dp), surface colour, optional click handler.  
**Usage:** Every feature screen (50+ call sites).

```swift
// iOS Implementation
struct WellnessCard<Content: View>: View {
    var onClick: (() -> Void)? = nil
    @ViewBuilder var content: () -> Content
    
    var body: some View {
        Group {
            if let onClick {
                Button(action: onClick) { cardBody }
            } else {
                cardBody
            }
        }
    }
    
    private var cardBody: some View {
        content()
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .shadow(color: .black.opacity(0.06), radius: 4, x: 0, y: 2)
    }
}
```

### 6.2 StatusChip

**Android:** `WellnessComponents.kt` — `StatusChip(status: String)` — colour-coded pill for appointment/visit status.  
**Status → Colour mapping:**
- `booked` → primary (teal)
- `pending` → amber
- `arrived` / `checked-in` / `in-treatment` → info blue
- `completed` → green
- `cancelled` → error red
- default → surface variant (grey)

```swift
struct StatusChip: View {
    let status: String
    
    var chipColor: Color {
        switch status.lowercased() {
        case "booked": return .wellnessPrimary
        case "pending": return .orange
        case "arrived", "checked-in", "in-treatment": return .blue
        case "completed": return .green
        case "cancelled": return .red
        default: return .secondary
        }
    }
    
    var body: some View {
        Text(status.capitalized)
            .font(.caption2.weight(.medium))
            .padding(.horizontal, 8).padding(.vertical, 4)
            .background(chipColor.opacity(0.15))
            .foregroundColor(chipColor)
            .cornerRadius(20)
    }
}
```

### 6.3 SectionLabel

**Android:** `WellnessComponents.kt` — uppercase, `labelLarge` with 0.8sp tracking, muted colour.  
**Purpose:** Section header text for grouped lists.

```swift
struct SectionLabel: View {
    let text: String
    var body: some View {
        Text(text.uppercased())
            .font(.caption.weight(.semibold))
            .tracking(0.8)
            .foregroundColor(.secondary)
    }
}
```

### 6.4 EmptyState

**Android:** `WellnessComponents.kt` — icon, message, optional action button.

```swift
struct EmptyStateView: View {
    let message: String
    var actionTitle: String? = nil
    var action: (() -> Void)? = nil
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "tray")
                .font(.largeTitle)
                .foregroundColor(.secondary)
            Text(message)
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)
            if let actionTitle, let action {
                Button(actionTitle, action: action)
                    .buttonStyle(.borderedProminent)
            }
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
```

### 6.5 ErrorState

**Android:** `WellnessComponents.kt` — error message + "Retry" button.

```swift
struct ErrorStateView: View {
    let message: String
    var onRetry: (() -> Void)? = nil
    
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "exclamationmark.triangle")
                .font(.largeTitle).foregroundColor(.orange)
            Text(message).multilineTextAlignment(.center).foregroundColor(.secondary)
            if let onRetry {
                Button("Retry", action: onRetry).buttonStyle(.borderedProminent)
            }
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
```

### 6.6 WellnessTopBar

**Android:** `WellnessTopAppBar.kt` — shows clinic name, back arrow (when applicable), search icon, bell badge, dark/light toggle.  
**Per-screen visibility rules:** Hidden on Splash/Login/Register.

```swift
struct WellnessTopBarView: View {
    let clinicName: String
    let unreadCount: Int
    @Binding var isDarkMode: Bool
    @Binding var showSearch: Bool
    var showBack: Bool = false
    var onBack: (() -> Void)? = nil
    
    var body: some View {
        HStack {
            if showBack {
                Button { onBack?() } label: {
                    Image(systemName: "chevron.left").font(.title3)
                }
            }
            Text(clinicName).font(.headline).lineLimit(1)
            Spacer()
            Button { showSearch.toggle() } label: {
                Image(systemName: showSearch ? "xmark" : "magnifyingglass")
            }
            NotificationBellButton(unreadCount: unreadCount)
            Button { isDarkMode.toggle() } label: {
                Image(systemName: isDarkMode ? "sun.max" : "moon")
            }
        }
        .padding(.horizontal, 16)
        .frame(height: 56)
        .background(Color(.systemBackground).opacity(0.95))
    }
}
```

### 6.7 PasswordFieldWithToggle

**Android:** `OutlinedTextField` + eye `IconButton` — used in Login, Register, ProfileScreen (×3).

```swift
struct PasswordFieldWithToggle: View {
    let label: String
    @Binding var text: String
    @State private var isVisible = false
    
    var body: some View {
        ZStack(alignment: .trailing) {
            Group {
                if isVisible {
                    TextField(label, text: $text)
                } else {
                    SecureField(label, text: $text)
                }
            }
            .textFieldStyle(.roundedBorder)
            .padding(.trailing, 40)
            
            Button { isVisible.toggle() } label: {
                Image(systemName: isVisible ? "eye.slash" : "eye")
                    .foregroundColor(.secondary)
            }
            .padding(.trailing, 8)
        }
    }
}
```

### 6.8 PrimaryButton (WellnessPrimaryButtonStyle)

**Android:** `Button` with `shape = extraLarge (24dp)`, full-width, 52dp height, `isLoading` spinner overlay.

```swift
struct WellnessPrimaryButtonStyle: ButtonStyle {
    var isLoading: Bool = false
    
    func makeBody(configuration: Configuration) -> some View {
        ZStack {
            if isLoading {
                ProgressView().tint(.white)
            } else {
                configuration.label.foregroundColor(.white)
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: 52)
        .background(Color.wellnessPrimary.opacity(configuration.isPressed ? 0.8 : 1))
        .cornerRadius(24)
    }
}
```

### 6.9 KPICard

**Android:** Used in Wallet (4-up), Finance (3-up), MyAppointments (4-up).  
**Layout:** value (titleSmall SemiBold) above label (bodySmall), padding h=8 v=12.

```swift
struct KPICard: View {
    let label: String
    let value: String
    var valueColor: Color = .primary
    
    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.subheadline.weight(.semibold))
                .foregroundColor(valueColor)
                .lineLimit(1).minimumScaleFactor(0.8)
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
                .lineLimit(1)
        }
        .padding(.horizontal, 8).padding(.vertical, 12)
        .frame(maxWidth: .infinity)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
    }
}
```

### 6.10 MenuTile (Dashboard Quick Action)

**Android:** Icon + label + subtitle in 2-column grid. Text wraps naturally.

```swift
struct MenuTile: View {
    let title: String
    let subtitle: String?
    let icon: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack(alignment: .leading, spacing: 4) {
                Image(systemName: icon).font(.title2).foregroundColor(.wellnessPrimary)
                Text(title).font(.subheadline.weight(.medium))
                if let subtitle {
                    Text(subtitle).font(.caption).foregroundColor(.secondary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
        }
        .buttonStyle(.plain)
    }
}
```

---

## 7. Design System Migration Report

### 7.1 Color Tokens

| Token | Android | iOS |
|-------|---------|-----|
| Primary | `#1A5C53` (WellnessPrimary) | `Color("wellnessPrimary")` |
| Accent | `#CD9481` (warm blush) | `Color("wellnessAccent")` |
| Background | `#F5F0E8` (cream linen) | `Color("wellnessBackground")` |
| Surface | `#FFFFFF` | `Color(.systemBackground)` |
| Surface Variant | `#EDE9E0` | `Color(.secondarySystemBackground)` |
| On Primary | White | `.white` |
| Error | `#B00020` | `Color(.systemRed)` or `Color("wellnessError")` |
| Dark Primary | `#4DB6AC` | `Color("wellnessDarkPrimary")` |
| Dark Background | `#121212` | `Color(.systemBackground)` (auto-adapts) |
| Dark Surface | `#1E1E1E` | `Color(.secondarySystemBackground)` |

**Brand color override:** `parseBrandColor(hex?)` is called at runtime with tenant branding hex. iOS equivalent: `Color(hex: tenantBrandHex) ?? .wellnessPrimary` using a `Color(hex:)` extension.

**Asset Catalog structure:**
```
Assets.xcassets/
└── Colors/
    ├── WellnessPrimary.colorset        — Light: #1A5C53 / Dark: #4DB6AC
    ├── WellnessAccent.colorset         — #CD9481 (same both modes)
    ├── WellnessBackground.colorset     — Light: #F5F0E8 / Dark: #121212
    ├── WellnessSurface.colorset        — System (auto)
    └── WellnessError.colorset          — #B00020 / #CF6679
```

### 7.2 Typography Tokens

**Android font families:**
- Primary: `Poppins` (Regular/Medium/SemiBold/Bold) — `GoogleFonts`
- Secondary (defined but not primary): `Manrope`, `Inter`, `Playfair Display`

**iOS font strategy:**
- Bundle `Poppins-Regular.ttf`, `Poppins-Medium.ttf`, `Poppins-SemiBold.ttf`, `Poppins-Bold.ttf` in app bundle
- Register in `Info.plist` under `UIAppFonts`
- Create `Font` extension

```swift
extension Font {
    static func poppins(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        let name: String
        switch weight {
        case .medium:     name = "Poppins-Medium"
        case .semibold:   name = "Poppins-SemiBold"
        case .bold:       name = "Poppins-Bold"
        default:          name = "Poppins-Regular"
        }
        return .custom(name, size: size)
    }
}
```

**Type scale mapping:**

| Android Role | Android Size | iOS Equivalent | Size |
|-------------|-------------|---------------|------|
| `displayLarge` | 57sp | — (unused) | — |
| `headlineLarge` | 32sp | `.title` / `.largeTitle` | 28/34pt |
| `headlineMedium` | 28sp | `.title2` | 22pt |
| `headlineSmall` | 24sp | `.title3` | 20pt |
| `titleLarge` | 22sp | `.headline` | 17pt |
| `titleMedium` | 16sp | `.subheadline` | 15pt |
| `titleSmall` | 14sp | `.footnote.weight(.semibold)` | 13pt |
| `bodyLarge` | 16sp | `.body` | 17pt |
| `bodyMedium` | 14sp | `.subheadline` | 15pt |
| `bodySmall` | 12sp | `.caption` | 12pt |
| `labelLarge` | 14sp | `.subheadline.weight(.medium)` | 15pt |
| `labelMedium` | 12sp | `.caption.weight(.medium)` | 12pt |
| `labelSmall` | 11sp | `.caption2` | 11pt |

### 7.3 Spacing Tokens

**Android** (`WellnessComponents.kt` constants):

| Token | Value | iOS Usage |
|-------|-------|-----------|
| `SpacingXs` | 4dp | `spacing: 4` |
| `SpacingSm` | 8dp | `spacing: 8` |
| `SpacingMd` | 16dp | `spacing: 16` |
| `SpacingLg` | 20dp | `spacing: 20` |
| `SpacingXl` | 24dp | `spacing: 24` |
| Screen horizontal padding | 16dp | `.padding(.horizontal, 16)` |
| Card inner padding | 16dp | `.padding(16)` |
| Section spacing | 20dp | `LazyVStack(spacing: 20)` |
| Form field spacing | 16dp | `VStack(spacing: 16)` |

```swift
enum WellnessSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 20
    static let xl: CGFloat = 24
    static let screenHorizontal: CGFloat = 16
    static let cardInner: CGFloat = 16
}
```

### 7.4 Corner Radius Tokens

| Android Shape | Radius | iOS Usage |
|--------------|--------|-----------|
| `extraSmall` | 8dp | `.cornerRadius(8)` |
| `small` | 12dp | `.cornerRadius(12)` (WellnessCard) |
| `medium` | 16dp | `.cornerRadius(16)` |
| `large` | 24dp | `.cornerRadius(24)` (button) |
| `extraLarge` | 12dp | `.cornerRadius(12)` |
| Full circle | 50% | `.clipShape(.circle)` |

```swift
enum WellnessRadius {
    static let xs: CGFloat = 8
    static let sm: CGFloat = 12
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let button: CGFloat = 24
    static let card: CGFloat = 12
}
```

### 7.5 Elevation & Shadow

| Android | iOS |
|---------|-----|
| `ElevationTokens.Level1` | `shadow(color: .black.opacity(0.06), radius: 4, x: 0, y: 2)` |
| `ElevationTokens.Level2` | `shadow(color: .black.opacity(0.10), radius: 8, x: 0, y: 4)` |
| Card default (dp=1) | `.shadow(radius: 2, y: 1)` |

### 7.6 Icons

**Android source:** Material Icons Extended + custom SVGs  
**iOS mapping:** Use SF Symbols 4.0 (iOS 16+ compatible)

| Android Icon | SF Symbol |
|-------------|-----------|
| `Icons.Default.Home` | `house` |
| `Icons.Default.CalendarMonth` | `calendar` |
| `Icons.Default.GridView` | `square.grid.2x2` |
| `Icons.Default.AccountBalanceWallet` | `indianrupeesign.square` |
| `Icons.Default.Person` | `person` |
| `Icons.Default.Notifications` | `bell` |
| `Icons.Default.Add` | `plus` |
| `Icons.Default.Search` | `magnifyingglass` |
| `Icons.Default.Visibility` | `eye` |
| `Icons.Default.VisibilityOff` | `eye.slash` |
| `Icons.Default.ChevronRight` | `chevron.right` |
| `Icons.Default.DarkMode` | `moon` |
| `Icons.Default.LightMode` | `sun.max` |
| `Icons.Default.Medication` | `pills` |
| `Icons.Default.CreditCard` | `creditcard` |
| `Icons.Default.Star` | `star` |
| `Icons.Default.History` | `clock` |
| `Icons.Default.CameraAlt` | `camera.fill` |
| `Icons.Default.Download` | `square.and.arrow.down` |
| `Icons.Default.Share` | `square.and.arrow.up` |

### 7.7 Animations

| Android | iOS |
|---------|-----|
| `AnimatedVisibility` (search bar slide) | `withAnimation(.easeInOut(duration: 0.2)) { showSearch.toggle() }` + `if showSearch { SearchBar }` |
| `basicMarquee()` (tab labels) | `.lineLimit(1).minimumScaleFactor(0.8)` or custom marquee |
| Material ripple on tap | `.buttonStyle(.plain)` + custom highlight overlay |
| `NavigationHost` transitions | Default SwiftUI `NavigationStack` slide transition |

### 7.8 iOS Design Token File Structure

```
WellnessCRM/Core/DesignSystem/
├── WellnessColors.swift        — Color extension + Asset Catalog references
├── WellnessTypography.swift    — Font extension (Poppins), type scale enum
├── WellnessSpacing.swift       — CGFloat constants
├── WellnessRadius.swift        — CGFloat constants
├── WellnessShadow.swift        — ViewModifier for standard shadows
├── WellnessButtonStyles.swift  — WellnessPrimaryButtonStyle, WellnessSecondaryButtonStyle
└── WellnessTheme.swift         — Environment injection of brand color, dark mode flag
```

```swift
// WellnessTheme.swift
struct WellnessThemeKey: EnvironmentKey {
    static let defaultValue = WellnessTheme(brandColor: Color("wellnessPrimary"), isDark: false)
}

extension EnvironmentValues {
    var wellnessTheme: WellnessTheme {
        get { self[WellnessThemeKey.self] }
        set { self[WellnessThemeKey.self] = newValue }
    }
}
```

---

## 8. iOS 16+ Native UI Recommendations

### 8.1 Navigation

| Use Case | Recommended API | Avoid |
|----------|----------------|-------|
| Root navigation | `NavigationStack` | `NavigationView` (deprecated) |
| Deep navigation paths | `NavigationPath` + `navigationDestination(for:)` | Manual `@State showView` stacks |
| Tab switching | `TabView(selection:)` | Custom tab bar implementations |
| Bottom sheet | `.sheet(isPresented:) { }.presentationDetents([.medium, .large])` | Custom overlay sheets |
| Partial sheet | `.presentationDetents([.fraction(0.5)])` | Manual drag gesture sheets |
| Popover (iPad) | `.popover(isPresented:)` | Full sheets on iPad |
| Confirmation dialog | `.confirmationDialog("", isPresented:, titleVisibility: .hidden)` | Custom action sheet |
| Alert | `.alert("Title", isPresented:) { Button actions }` | Custom modal dialogs |

### 8.2 Lists

| Use Case | Recommended API | Avoid |
|----------|----------------|-------|
| Standard list | `List { ForEach } .listStyle(.insetGrouped)` | Manual `LazyVStack` for list-like content |
| Pull-to-refresh | `.refreshable { await vm.refresh() }` on `List` or `ScrollView` | Manual refresh control |
| Swipe actions | `.swipeActions(edge: .trailing) { }` | Custom gesture handlers |
| Sections | `Section("Title") { }` | Manual section headers |

### 8.3 Forms

| Use Case | Recommended API |
|----------|----------------|
| Settings/preferences screen | `Form { Section { Toggle, DatePicker, Picker } }` |
| Data entry form | `VStack { TextField, SecureField, Picker }` in `ScrollView` |
| Picker | `Picker("Label", selection: $binding) { }.pickerStyle(.menu)` |
| Date picker | `DatePicker("Label", selection: $date, displayedComponents: .date)` |
| Time picker | `DatePicker("Label", selection: $time, displayedComponents: .hourAndMinute)` |

### 8.4 Images

| Use Case | Recommended API | Notes |
|----------|----------------|-------|
| Remote images | `AsyncImage(url:) { phase in }` | Built-in iOS 15+ |
| Coil replacement | `AsyncImage` or Kingfisher (SPM) | Kingfisher offers disk cache, transforms |
| Circular avatars | `.clipShape(.circle)` | SF Symbols `person.circle` for placeholder |
| PDF rendering | `PDFKit.PDFView` via `UIViewRepresentable` | No 3rd party needed |

### 8.5 Async Loading

```swift
// Preferred pattern (iOS 15+)
.task {
    await viewModel.loadData()
}

// Refresh
.refreshable {
    await viewModel.refresh()
}
```

### 8.6 Search

```swift
// iOS 15+ searchable modifier
.searchable(text: $viewModel.searchQuery, placement: .navigationBarDrawer(displayMode: .always))
```

### 8.7 Grid Layouts

```swift
// Adaptive grid (replaces GridCells.Adaptive)
LazyVGrid(columns: [GridItem(.adaptive(minimum: 156), spacing: 12)], spacing: 12) { ... }

// Fixed 2-column grid
LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) { ... }

// KPI row (2×2 on narrow)
LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 2), spacing: 8) { ... }
```

### 8.8 UIKit Required Cases

| Case | Reason | Integration |
|------|--------|-------------|
| PDF rendering | PDFKit is UIKit-based | `UIViewRepresentable(PDFView)` |
| Razorpay checkout | Razorpay SDK requires `UIViewController` | `UIViewControllerRepresentable` |
| Profile photo picker | `PhotosUI.PhotosPicker` (SwiftUI, iOS 16+) — no UIKit needed | `.photosPicker(isPresented:, selection:, matching: .images)` |
| Camera access | `UIImagePickerController` or `PHPickerViewController` | `UIViewControllerRepresentable` or use `PhotosPicker` |

### 8.9 @Observable vs ObservableObject

For new iOS 17+ code, `@Observable` + `@Bindable` is preferred. Since the target is iOS 16+, use `@MainActor class ViewModel: ObservableObject` with `@Published` properties. Use the Swift 5.9 `@Observable` macro only if iOS 17 minimum is acceptable.

```swift
// iOS 16+ compatible (recommended for this project)
@MainActor
class DashboardViewModel: ObservableObject {
    @Published private(set) var state = DashboardUiState()
    // ...
}
```

---

## 9. Screen Complexity Matrix

| Screen | Data Complexity | UI Complexity | Sheets/Dialogs | APIs | Cache | Overall |
|--------|----------------|--------------|----------------|------|-------|---------|
| SplashScreen | Low | Low | 0 | 2 | No | **Low** |
| LoginScreen | Low | Low | 0 | 1 | No | **Low** |
| RegisterScreen | Low | Low | 0 | 1 | No | **Low** |
| DashboardScreen | High | High | 0 | 3 | No | **High** |
| BookAppointmentScreen | High | Very High | 0 (DatePicker dialogs) | 3 | No | **Very High** |
| MyAppointmentsScreen | High | High | 2 | 2 | Yes | **High** |
| VisitHistoryScreen | Medium | Medium | 1 | 1 | Yes | **Medium** |
| WaitlistScreen | Medium | Medium | 1 | 2 | No | **Medium** |
| CatalogTabScreen | High | High | 1 | 2 | No | **High** |
| FinanceTabScreen | High | High | 1 | 2 | No | **High** |
| PrescriptionsScreen | Medium | Medium | 1 | 2 | Yes (PDF) | **Medium-High** |
| PrescriptionPdfScreen | Low | Medium | 0 | 1 | Yes (7-day) | **Medium** |
| TreatmentPlansScreen | Low | Low | 0 | 1 | No | **Low** |
| ConsentFormsScreen | Low | Low | 0 | 1 | No | **Low** |
| ConsentFormPdfScreen | Low | Medium | 0 | 1 | No | **Low-Medium** |
| MembershipsScreen | High | High | 2 | 2 | Yes | **High** |
| WalletScreen | High | High | 1 | 2 | No | **High** |
| GiftCardsScreen | Medium | High | 1 | 3 + Razorpay | No | **Very High** |
| LoyaltyScreen | Low | Low | 0 | 1 | No | **Low** |
| ProfileScreen | High | High | 2 | 5 | No | **Very High** |
| NotificationInboxScreen | Low | Low | 0 | Room only | Yes | **Low** |
| NotificationSettingsScreen | Low | Medium | 0 | 0 (DataStore) | No | **Low** |

---

## 10. Estimated Development Effort Per Screen

Estimates assume: 1 senior iOS engineer, SwiftUI-proficient, familiar with Clean Architecture pattern. Includes: data layer (DTOs, Repository, UseCase), ViewModel, SwiftUI View, and basic tests.

| Screen | Estimate (days) | Key Complexity Drivers |
|--------|----------------|----------------------|
| **Foundation** (AppRouter, Navigation, Design System, Theme) | 5d | — |
| **SplashScreen** | 1d | Simple; token check + branding fetch |
| **LoginScreen** | 1.5d | Password toggle, error states, SMS banner |
| **RegisterScreen** | 1.5d | 4 fields, confirm password validation |
| **DashboardScreen** | 3d | 3 API calls, dynamic greeting, quick action grid, search bar animation |
| **BookAppointmentScreen** | 5d | 4-step wizard, 3 API calls, date/time pickers, confirmation flow |
| **MyAppointmentsScreen** | 3.5d | 4-bucket tabs, action sheet, reschedule sheet, cancel dialog, pull-to-refresh |
| **VisitHistoryScreen** | 2d | Month grouping, detail sheet, Room cache |
| **WaitlistScreen** | 2d | FAB, add sheet, service dropdown, form validation |
| **CatalogTabScreen** | 3.5d | 3 inner tabs, service grid, category cross-filter, service detail sheet, memberships inline |
| **FinanceTabScreen** | 3d | 3 inner tabs, KPI grid, payment action sheet, refund confirm |
| **PrescriptionsScreen** | 2.5d | Permission gate, PDF confirm, Room cache, pull-to-refresh |
| **PrescriptionPdfScreen** | 1.5d | PDFKit UIViewRepresentable, 7-day file cache |
| **TreatmentPlansScreen** | 1d | List + progress indicator |
| **ConsentFormsScreen** | 1d | List view |
| **ConsentFormPdfScreen** | 0.5d | Reuse PrescriptionPdfView pattern |
| **MembershipsScreen** | 3d | Available/Mine toggle, coloured plan cards, plan detail sheet, join dialog |
| **WalletScreen** | 3d | 4-KPI grid, filter chips, transaction list, receipt sheet |
| **GiftCardsScreen** | 4d | Storefront grid, buy sheet, Razorpay SDK integration, confirm flow |
| **LoyaltyScreen** | 1.5d | Points card, referral card with ShareLink, history list |
| **ProfileScreen** | 4d | Dual API, photo upload, change password section, DSAR, logout |
| **NotificationInboxScreen** | 1.5d | Room/CoreData local, mark-read, deep link routing |
| **NotificationSettingsScreen** | 1.5d | Form toggles, quiet hours DatePicker, persist to UserDefaults |
| **FCM + Push Notifications** | 3d | UNUserNotificationCenter, badge count, deep link on tap |
| **Reusable Components** | 3d | WellnessCard, StatusChip, EmptyState, ErrorState, TopBar, BottomNav |
| **Auth Interceptor / Token Refresh** | 1.5d | URLSession URLProtocol, Keychain read, 401 handling (global) |
| **Deep Link Handler** | 1d | URL scheme routing, tab switching, path append |
| **QA / Polish Pass** | 5d | All screens; accessibility, dark mode, iPad layout |

### Summary Table

| Phase | Screens | Estimate |
|-------|---------|---------|
| Foundation (arch, nav, design system) | — | 5d |
| Auth (3 screens) | SplashScreen, LoginScreen, RegisterScreen | 4d |
| Dashboard | DashboardScreen | 3d |
| Booking (4 screens) | BookAppointment, MyAppointments, VisitHistory, Waitlist | 12.5d |
| Catalog + Finance | CatalogTab, FinanceTab | 6.5d |
| Health (5 screens) | Prescriptions, PDFs ×2, TreatmentPlans, ConsentForms | 6.5d |
| Membership + Wallet + GiftCards + Loyalty | 4 screens | 11.5d |
| Profile + Notifications (4 screens) | Profile, NotificationInbox, NotificationSettings | 8d |
| Infrastructure | FCM, components, interceptor, deep links | 8.5d |
| QA + Polish | All screens | 5d |
| **Total** | **22 screens** | **~70 developer-days (~14 weeks solo, ~7 weeks pair)** |

---

## Appendix A — Navigation Route Quick Reference

| Screen | Android Route | iOS NavigationPath Type |
|--------|--------------|------------------------|
| Splash | `splash` | App root state |
| Login | `login` | `AuthRoute.login` |
| Register | `register` | `AuthRoute.register` |
| Dashboard | `tab_home` | TabView tab 0 |
| BookAppointment | `book_appointment` | `HomeRoute.bookAppointment` |
| MyAppointments | `tab_bookings` | TabView tab 1 |
| VisitHistory | `visit_history` | `BookingRoute.visitHistory` |
| Waitlist | `waitlist` | `BookingRoute.waitlist` |
| Catalog | `tab_catalog` | TabView tab 2 |
| Finance | `tab_finance` | TabView tab 3 |
| Prescriptions | `prescriptions` | `HomeRoute.prescriptions` |
| PrescriptionPdf | `prescription_pdf/{id}` | `HealthRoute.prescriptionPdf(id: Int)` |
| TreatmentPlans | `treatment_plans` | `HomeRoute.treatmentPlans` |
| ConsentForms | `consent_forms` | `HomeRoute.consentForms` |
| ConsentFormPdf | `consent_form_pdf/{id}` | `HealthRoute.consentPdf(id: Int)` |
| Memberships | `memberships` | `HomeRoute.memberships` |
| Wallet | `wallet` | `FinanceRoute.wallet` |
| GiftCards | `gift_cards` | `FinanceRoute.giftCards` |
| Loyalty | `loyalty` | `HomeRoute.loyalty` |
| Profile | `tab_profile` | TabView tab 4 |
| NotificationInbox | `notifications` | `ProfileRoute.notifications` |
| NotificationSettings | `notification_settings` | `ProfileRoute.notificationSettings` |

---

## Appendix B — API Security Notes for iOS

| Note | Android Behaviour | iOS Action Required |
|------|-----------------|-------------------|
| JWT stored in DataStore | File-encrypted, app-private | Store in Keychain (`kSecClassGenericPassword`); never `UserDefaults` |
| Patient name/phone in EncryptedSharedPrefs | AES-256-GCM, Android Keystore | Store in Keychain (same item, separate keys) |
| `patientId` used in loyalty URL | **Backend does not verify ownership** — use stored value only | Read from Keychain; never from user input or URL params |
| PDF bytes in Room BLOB | Never saved to Downloads | Write to app-private Documents directory; never Photos library |
| PDFs served via FileProvider | Only within app | Use `UIActivityViewController` with `excludedActivityTypes: [.saveToCameraRoll]` |
| PHI in logs | `patientId` (int) only — never name/phone | `os_log` with `%{private}` formatter for any patient data |
| Cert pinning | `network_security_config.xml` | `URLSession` challenge handler: compare `SecCertificate` against pinned SHA256 hash |
| FCM token registration | `POST /portal/me/fcm-token` — backend is WebPush/VAPID only, silently fails | APNs token via `UIApplication.registerForRemoteNotifications()`; FCM REST API for iOS-compatible endpoint (backend gap) |
| 401 handling | **NOT implemented globally in Android** — each screen handles independently | **Implement globally from day 1**: `URLProtocol` interceptor or `URLSessionDelegate` — on 401, clear Keychain + route to Login |

---

*End of iOS Screen Migration Report*  
*Generated: 2026-06-09 | Source: WellnessCRM Android codebase (Kotlin + Jetpack Compose)*  
*Target: iOS 16.0+ (Swift + SwiftUI + MVVM + Clean Architecture)*
