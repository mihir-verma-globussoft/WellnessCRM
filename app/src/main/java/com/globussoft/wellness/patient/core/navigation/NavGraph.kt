package com.globussoft.wellness.patient.core.navigation

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
import com.globussoft.wellness.patient.feature.dashboard.presentation.screen.DashboardScreen
import com.globussoft.wellness.patient.feature.dashboard.presentation.viewmodel.DashboardNavEvent
import com.globussoft.wellness.patient.feature.dashboard.presentation.viewmodel.DashboardViewModel

@Composable
fun WellnessNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
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
                        DashboardNavEvent.ToNotifications -> navController.navigate(Screen.Notifications.route)
                        DashboardNavEvent.ToGiftCards -> navController.navigate(Screen.GiftCards.route)
                        DashboardNavEvent.ToLogin -> navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                }
            }
            DashboardScreen(state = state, onEvent = vm::onEvent)
        }

        composable(
            route = Screen.BookAppointment.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/book" }),
        ) {
            // TODO Phase 4: BookAppointmentScreen()
        }

        composable(
            route = Screen.MyAppointments.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/appointments" }),
        ) {
            // TODO Phase 4: MyAppointmentsScreen()
        }

        composable(Screen.VisitHistory.route) {
            // TODO Phase 4: VisitHistoryScreen()
        }

        composable(
            route = Screen.Prescriptions.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/prescriptions" }),
        ) {
            // TODO Phase 5: PrescriptionsScreen()
        }

        composable(
            route = Screen.PrescriptionPdf.route,
        ) {
            // TODO Phase 5: PrescriptionPdfScreen()
        }

        composable(
            route = Screen.Memberships.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/memberships" }),
        ) {
            // TODO Phase 6: MembershipsScreen()
        }

        composable(
            route = Screen.Wallet.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/wallet" }),
        ) {
            // TODO Phase 7: WalletScreen()
        }

        composable(Screen.GiftCards.route) {
            // TODO Phase 7: GiftCardsScreen()
        }

        composable(Screen.Profile.route) {
            // TODO Phase 8: ProfileScreen()
        }

        composable(
            route = Screen.Notifications.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/notifications" }),
        ) {
            // TODO Phase 8: NotificationInboxScreen()
        }

        composable(Screen.TreatmentPlans.route) { /* TODO Phase 2 */ }
        composable(Screen.ConsentForms.route) { /* TODO Phase 2 */ }
        composable(Screen.Loyalty.route) { /* TODO Phase 2 */ }
    }
}
