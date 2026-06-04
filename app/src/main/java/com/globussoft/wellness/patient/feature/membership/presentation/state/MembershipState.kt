package com.globussoft.wellness.patient.feature.membership.presentation.state

import com.globussoft.wellness.patient.feature.membership.domain.model.Membership
import com.globussoft.wellness.patient.feature.membership.domain.model.MembershipPlan

data class MembershipsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val memberships: List<Membership> = emptyList(),
    val plans: List<MembershipPlan> = emptyList(),
    val selectedMembership: Membership? = null,
    val showPlans: Boolean = false,
)

sealed class MembershipsUiEvent {
    object Refresh : MembershipsUiEvent()
    data class SelectMembership(val membership: Membership) : MembershipsUiEvent()
    object DismissDetail : MembershipsUiEvent()
    object TogglePlans : MembershipsUiEvent()
    object NavigateBack : MembershipsUiEvent()
}
