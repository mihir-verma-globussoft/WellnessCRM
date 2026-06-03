package com.globussoft.wellness.patient.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object PhoneEntry : Screen("phone_entry")
    object OtpVerify : Screen("otp_verify/{phone}") {
        fun createRoute(phone: String) = "otp_verify/$phone"
    }
    object Register : Screen("register/{phone}") {
        fun createRoute(phone: String) = "register/$phone"
    }
    object Dashboard : Screen("dashboard")
    object BookAppointment : Screen("book_appointment?serviceId={serviceId}&membershipId={membershipId}") {
        fun createRoute(serviceId: Int? = null, membershipId: Int? = null) =
            "book_appointment?serviceId=${serviceId ?: ""}&membershipId=${membershipId ?: ""}"
    }
    object MyAppointments : Screen("my_appointments")
    object VisitHistory : Screen("visit_history")
    object Prescriptions : Screen("prescriptions")
    object PrescriptionPdf : Screen("prescription_pdf/{id}") {
        fun createRoute(id: Int) = "prescription_pdf/$id"
    }
    object TreatmentPlans : Screen("treatment_plans")  // Phase 2
    object ConsentForms : Screen("consent_forms")      // Phase 2
    object Memberships : Screen("memberships")
    object Wallet : Screen("wallet")
    object GiftCards : Screen("gift_cards")
    object Loyalty : Screen("loyalty")                 // Phase 2
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
}
