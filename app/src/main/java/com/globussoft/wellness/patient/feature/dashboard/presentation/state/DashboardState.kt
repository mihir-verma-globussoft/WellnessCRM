package com.globussoft.wellness.patient.feature.dashboard.presentation.state

import com.globussoft.wellness.patient.feature.dashboard.domain.model.Dashboard

data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val dashboard: Dashboard? = null,
)

sealed class DashboardUiEvent {
    object Refresh : DashboardUiEvent()
    object NavigateToAppointments : DashboardUiEvent()
    object NavigateToBooking : DashboardUiEvent()
    object NavigateToPrescriptions : DashboardUiEvent()
    object NavigateToProfile : DashboardUiEvent()
    object NavigateToWallet : DashboardUiEvent()
    object NavigateToMemberships : DashboardUiEvent()
    object NavigateToNotifications : DashboardUiEvent()
    object NavigateToGiftCards : DashboardUiEvent()
    object NavigateToLoyalty : DashboardUiEvent()
    object Logout : DashboardUiEvent()
}
