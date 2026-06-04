package com.globussoft.wellness.patient.feature.profile.presentation.state

import com.globussoft.wellness.patient.feature.profile.domain.model.Profile

data class ProfileUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val profile: Profile? = null,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
    val exportRequested: Boolean = false,
)

sealed class ProfileUiEvent {
    object Refresh : ProfileUiEvent()
    object StartEdit : ProfileUiEvent()
    object CancelEdit : ProfileUiEvent()
    data class EditName(val name: String) : ProfileUiEvent()
    data class EditEmail(val email: String) : ProfileUiEvent()
    data class EditCurrentPassword(val password: String) : ProfileUiEvent()
    data class EditNewPassword(val password: String) : ProfileUiEvent()
    object SaveChanges : ProfileUiEvent()
    object RequestDsarExport : ProfileUiEvent()
    object Logout : ProfileUiEvent()
    object NavigateBack : ProfileUiEvent()
}
