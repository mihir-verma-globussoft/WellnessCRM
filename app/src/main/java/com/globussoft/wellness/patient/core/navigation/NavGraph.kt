package com.globussoft.wellness.patient.core.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.globussoft.wellness.patient.feature.auth.presentation.screen.LoginScreen
import com.globussoft.wellness.patient.feature.auth.presentation.screen.RegisterScreen
import com.globussoft.wellness.patient.feature.auth.presentation.screen.SplashScreen
import com.globussoft.wellness.patient.feature.auth.presentation.viewmodel.LoginNavEvent
import com.globussoft.wellness.patient.feature.auth.presentation.viewmodel.LoginViewModel
import com.globussoft.wellness.patient.feature.auth.presentation.viewmodel.RegisterNavEvent
import com.globussoft.wellness.patient.feature.auth.presentation.viewmodel.RegisterViewModel
import com.globussoft.wellness.patient.feature.auth.presentation.viewmodel.SplashNavEvent
import com.globussoft.wellness.patient.feature.auth.presentation.viewmodel.SplashViewModel
import com.globussoft.wellness.patient.feature.booking.presentation.screen.BookAppointmentScreen
import com.globussoft.wellness.patient.feature.booking.presentation.screen.MyAppointmentsScreen
import com.globussoft.wellness.patient.feature.booking.presentation.screen.VisitHistoryScreen
import com.globussoft.wellness.patient.feature.booking.presentation.viewmodel.BookAppointmentNavEvent
import com.globussoft.wellness.patient.feature.booking.presentation.viewmodel.BookAppointmentViewModel
import com.globussoft.wellness.patient.feature.booking.presentation.viewmodel.MyAppointmentsNavEvent
import com.globussoft.wellness.patient.feature.booking.presentation.viewmodel.MyAppointmentsViewModel
import com.globussoft.wellness.patient.feature.booking.presentation.viewmodel.VisitHistoryNavEvent
import com.globussoft.wellness.patient.feature.booking.presentation.viewmodel.VisitHistoryViewModel
import com.globussoft.wellness.patient.feature.dashboard.presentation.screen.DashboardScreen
import com.globussoft.wellness.patient.feature.dashboard.presentation.viewmodel.DashboardNavEvent
import com.globussoft.wellness.patient.feature.dashboard.presentation.viewmodel.DashboardViewModel
import com.globussoft.wellness.patient.feature.health.presentation.screen.PrescriptionPdfScreen
import com.globussoft.wellness.patient.feature.health.presentation.screen.PrescriptionsScreen
import com.globussoft.wellness.patient.feature.health.presentation.viewmodel.PrescriptionPdfNavEvent
import com.globussoft.wellness.patient.feature.health.presentation.viewmodel.PrescriptionPdfViewModel
import com.globussoft.wellness.patient.feature.health.presentation.viewmodel.PrescriptionsNavEvent
import com.globussoft.wellness.patient.feature.health.presentation.viewmodel.PrescriptionsViewModel
import com.globussoft.wellness.patient.feature.membership.presentation.screen.MembershipsScreen
import com.globussoft.wellness.patient.feature.membership.presentation.viewmodel.MembershipsNavEvent
import com.globussoft.wellness.patient.feature.membership.presentation.viewmodel.MembershipsViewModel
import com.globussoft.wellness.patient.feature.notifications.presentation.screen.NotificationInboxScreen
import com.globussoft.wellness.patient.feature.notifications.presentation.viewmodel.NotificationsNavEvent
import com.globussoft.wellness.patient.feature.notifications.presentation.viewmodel.NotificationsViewModel
import com.globussoft.wellness.patient.feature.profile.presentation.screen.ProfileScreen
import com.globussoft.wellness.patient.feature.profile.presentation.viewmodel.ProfileNavEvent
import com.globussoft.wellness.patient.feature.profile.presentation.viewmodel.ProfileViewModel
import com.globussoft.wellness.patient.feature.wallet.presentation.screen.GiftCardsScreen
import com.globussoft.wellness.patient.feature.wallet.presentation.screen.WalletScreen
import com.globussoft.wellness.patient.feature.wallet.presentation.viewmodel.GiftCardsNavEvent
import com.globussoft.wellness.patient.feature.wallet.presentation.viewmodel.GiftCardsViewModel
import com.globussoft.wellness.patient.feature.health.presentation.screen.ConsentFormPdfScreen
import com.globussoft.wellness.patient.feature.health.presentation.screen.ConsentFormsScreen
import com.globussoft.wellness.patient.feature.health.presentation.screen.TreatmentPlansScreen
import com.globussoft.wellness.patient.feature.health.presentation.viewmodel.ConsentFormPdfNavEvent
import com.globussoft.wellness.patient.feature.health.presentation.viewmodel.ConsentFormPdfViewModel
import com.globussoft.wellness.patient.feature.health.presentation.viewmodel.ConsentFormsNavEvent
import com.globussoft.wellness.patient.feature.health.presentation.viewmodel.ConsentFormsViewModel
import com.globussoft.wellness.patient.feature.health.presentation.viewmodel.TreatmentPlansNavEvent
import com.globussoft.wellness.patient.feature.health.presentation.viewmodel.TreatmentPlansViewModel
import com.globussoft.wellness.patient.feature.loyalty.presentation.screen.LoyaltyScreen
import com.globussoft.wellness.patient.feature.loyalty.presentation.viewmodel.LoyaltyNavEvent
import com.globussoft.wellness.patient.feature.loyalty.presentation.viewmodel.LoyaltyViewModel
import com.globussoft.wellness.patient.feature.wallet.presentation.viewmodel.WalletNavEvent
import com.globussoft.wellness.patient.feature.wallet.presentation.viewmodel.WalletViewModel

@Composable
fun WellnessNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route,
    notificationIntent: Intent? = null,
) {
    // Re-process deep link when a notification brings the app to foreground (onNewIntent path).
    // Initial-launch deep links are handled automatically by NavHost via navDeepLink patterns.
    LaunchedEffect(notificationIntent) {
        notificationIntent?.let { navController.handleDeepLink(it) }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // ── Auth ──────────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            val vm: SplashViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        SplashNavEvent.NavigateToDashboard -> navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                        SplashNavEvent.NavigateToLogin -> navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            }
            SplashScreen(state = state)
        }

        composable(Screen.Login.route) {
            val vm: LoginViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        LoginNavEvent.NavigateToDashboard -> navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        LoginNavEvent.NavigateToRegister -> navController.navigate(Screen.Register.route)
                    }
                }
            }
            LoginScreen(state = state, onEvent = vm::onEvent)
        }

        composable(Screen.Register.route) {
            val vm: RegisterViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        RegisterNavEvent.NavigateToDashboard -> navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        RegisterNavEvent.NavigateToLogin -> navController.popBackStack()
                    }
                }
            }
            RegisterScreen(state = state, onEvent = vm::onEvent)
        }

        // ── Dashboard ─────────────────────────────────────────────────────────
        composable(
            route = Screen.Dashboard.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/dashboard" }),
        ) {
            val vm: DashboardViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navigationEvent.collect { event ->
                    when (event) {
                        DashboardNavEvent.ToAppointments -> navController.navigate(Screen.MyAppointments.route)
                        DashboardNavEvent.ToBooking -> navController.navigate(Screen.BookAppointment.createRoute())
                        DashboardNavEvent.ToPrescriptions -> navController.navigate(Screen.Prescriptions.route)
                        DashboardNavEvent.ToProfile -> navController.navigate(Screen.Profile.route)
                        DashboardNavEvent.ToWallet -> navController.navigate(Screen.Wallet.route)
                        DashboardNavEvent.ToMemberships -> navController.navigate(Screen.Memberships.route)
                        DashboardNavEvent.ToLoyalty -> navController.navigate(Screen.Loyalty.route)
                        DashboardNavEvent.ToNotifications -> navController.navigate(Screen.Notifications.route)
                        DashboardNavEvent.ToGiftCards -> navController.navigate(Screen.GiftCards.route)
                        DashboardNavEvent.ToVisitHistory -> navController.navigate(Screen.VisitHistory.route)
                        DashboardNavEvent.ToTreatmentPlans -> navController.navigate(Screen.TreatmentPlans.route)
                        DashboardNavEvent.ToConsentForms -> navController.navigate(Screen.ConsentForms.route)
                        DashboardNavEvent.ToLogin -> navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                }
            }
            DashboardScreen(state = state, onEvent = vm::onEvent)
        }

        // ── Booking ───────────────────────────────────────────────────────────
        composable(
            route = Screen.BookAppointment.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/book" }),
        ) {
            val vm: BookAppointmentViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        BookAppointmentNavEvent.Back -> navController.popBackStack()
                        BookAppointmentNavEvent.BookingSuccess -> navController.navigate(Screen.MyAppointments.route) {
                            popUpTo(Screen.BookAppointment.route) { inclusive = true }
                        }
                    }
                }
            }
            BookAppointmentScreen(state = state, onEvent = vm::onEvent)
        }

        composable(
            route = Screen.MyAppointments.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/appointments" }),
        ) {
            val vm: MyAppointmentsViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        MyAppointmentsNavEvent.ToBook -> navController.navigate(Screen.BookAppointment.createRoute())
                        MyAppointmentsNavEvent.ToHistory -> navController.navigate(Screen.VisitHistory.route)
                        MyAppointmentsNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            MyAppointmentsScreen(state = state, onEvent = vm::onEvent)
        }

        composable(Screen.VisitHistory.route) {
            val vm: VisitHistoryViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        VisitHistoryNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            VisitHistoryScreen(state = state, onEvent = vm::onEvent)
        }

        // ── Health ────────────────────────────────────────────────────────────
        composable(
            route = Screen.Prescriptions.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/prescriptions" }),
        ) {
            val vm: PrescriptionsViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        is PrescriptionsNavEvent.ToPdf -> navController.navigate(Screen.PrescriptionPdf.createRoute(event.prescriptionId))
                        PrescriptionsNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            PrescriptionsScreen(state = state, onEvent = vm::onEvent)
        }

        composable(route = Screen.PrescriptionPdf.route) {
            val vm: PrescriptionPdfViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        PrescriptionPdfNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            PrescriptionPdfScreen(state = state, onEvent = vm::onEvent)
        }

        // ── Memberships ───────────────────────────────────────────────────────
        composable(
            route = Screen.Memberships.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/memberships" }),
        ) {
            val vm: MembershipsViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        MembershipsNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            MembershipsScreen(state = state, onEvent = vm::onEvent)
        }

        // ── Wallet & Gift Cards ───────────────────────────────────────────────
        composable(
            route = Screen.Wallet.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/wallet" }),
        ) {
            val vm: WalletViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        WalletNavEvent.ToGiftCards -> navController.navigate(Screen.GiftCards.route)
                        WalletNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            WalletScreen(state = state, onEvent = vm::onEvent)
        }

        composable(Screen.GiftCards.route) {
            val vm: GiftCardsViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        is GiftCardsNavEvent.LaunchRazorpay -> { /* Razorpay SDK launched from Activity context */ }
                        GiftCardsNavEvent.Back -> navController.popBackStack()
                        GiftCardsNavEvent.PurchaseComplete -> navController.navigate(Screen.Wallet.route) {
                            popUpTo(Screen.GiftCards.route) { inclusive = true }
                        }
                    }
                }
            }
            GiftCardsScreen(state = state, onEvent = vm::onEvent)
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(Screen.Profile.route) {
            val vm: ProfileViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        ProfileNavEvent.Back -> navController.popBackStack()
                        ProfileNavEvent.ToLogin -> navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                }
            }
            ProfileScreen(state = state, onEvent = vm::onEvent)
        }

        // ── Notifications ─────────────────────────────────────────────────────
        composable(
            route = Screen.Notifications.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/notifications" }),
        ) {
            val vm: NotificationsViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        is NotificationsNavEvent.OpenDeepLink -> {
                            val route = when (event.screen) {
                                "appointments" -> Screen.MyAppointments.route
                                "prescriptions" -> Screen.Prescriptions.route
                                "memberships" -> Screen.Memberships.route
                                "wallet" -> Screen.Wallet.route
                                "book" -> Screen.BookAppointment.createRoute()
                                else -> null
                            }
                            route?.let { navController.navigate(it) }
                        }
                        NotificationsNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            NotificationInboxScreen(state = state, onEvent = vm::onEvent)
        }

        // ── Treatment Plans ───────────────────────────────────────────────────
        composable(
            route = Screen.TreatmentPlans.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/treatment_plans" }),
        ) {
            val vm: TreatmentPlansViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        TreatmentPlansNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            TreatmentPlansScreen(state = state, onEvent = vm::onEvent)
        }

        // ── Consent Forms ─────────────────────────────────────────────────────
        composable(
            route = Screen.ConsentForms.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/consent_forms" }),
        ) {
            val vm: ConsentFormsViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        is ConsentFormsNavEvent.ToPdf -> navController.navigate(
                            Screen.ConsentFormPdf.createRoute(event.consentId)
                        )
                        ConsentFormsNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            ConsentFormsScreen(state = state, onEvent = vm::onEvent)
        }

        composable(Screen.ConsentFormPdf.route) {
            val vm: ConsentFormPdfViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        ConsentFormPdfNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            ConsentFormPdfScreen(state = state, onEvent = vm::onEvent)
        }

        // ── Loyalty & Referrals ───────────────────────────────────────────────
        composable(
            route = Screen.Loyalty.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/loyalty" }),
        ) {
            val vm: LoyaltyViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.navEvent.collect { event ->
                    when (event) {
                        LoyaltyNavEvent.Back -> navController.popBackStack()
                    }
                }
            }
            LoyaltyScreen(state = state, onEvent = vm::onEvent)
        }
    }
}
