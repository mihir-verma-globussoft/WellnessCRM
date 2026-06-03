package com.globussoft.wellness.patient.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

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
            // TODO Phase 2: SplashScreen()
        }

        composable(Screen.PhoneEntry.route) {
            // TODO Phase 2: PhoneEntryScreen()
        }

        composable(
            route = Screen.OtpVerify.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType }),
        ) {
            // TODO Phase 2: OtpVerifyScreen()
        }

        composable(
            route = Screen.Register.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType }),
        ) {
            // TODO Phase 2: RegisterScreen()
        }

        composable(
            route = Screen.Dashboard.route,
            deepLinks = listOf(navDeepLink { uriPattern = "wellnesspatient://screen/dashboard" }),
        ) {
            // TODO Phase 3: DashboardScreen()
        }

        composable(
            route = Screen.BookAppointment.route,
            arguments = listOf(
                navArgument("serviceId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("membershipId") { type = NavType.StringType; nullable = true; defaultValue = null },
            ),
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
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
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

        // Phase 2 screens (deferred until backend endpoints are built)
        composable(Screen.TreatmentPlans.route) { /* TODO Phase 2 */ }
        composable(Screen.ConsentForms.route) { /* TODO Phase 2 */ }
        composable(Screen.Loyalty.route) { /* TODO Phase 2 */ }
    }
}
