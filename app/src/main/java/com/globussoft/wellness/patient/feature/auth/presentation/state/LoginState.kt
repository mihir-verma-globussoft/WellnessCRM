package com.globussoft.wellness.patient.feature.auth.presentation.state

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class LoginUiEvent {
    data class EmailChanged(val email: String) : LoginUiEvent()
    data class PasswordChanged(val password: String) : LoginUiEvent()
    object TogglePasswordVisibility : LoginUiEvent()
    object Submit : LoginUiEvent()
    object NavigateToRegister : LoginUiEvent()
}
