package com.crm.enhance_wellness.feature.auth.presentation.state

import com.crm.enhance_wellness.feature.auth.domain.usecase.PasswordRule

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordRules: List<PasswordRule> = emptyList(),
    val otp: String = "",
    val otpSent: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class RegisterUiEvent {
    data class NameChanged(val name: String) : RegisterUiEvent()
    data class EmailChanged(val email: String) : RegisterUiEvent()
    data class PasswordChanged(val password: String) : RegisterUiEvent()
    data class ConfirmPasswordChanged(val password: String) : RegisterUiEvent()
    data class OtpChanged(val otp: String) : RegisterUiEvent()
    object ResendOtp : RegisterUiEvent()
    object TogglePasswordVisibility : RegisterUiEvent()
    object Submit : RegisterUiEvent()
    object NavigateToLogin : RegisterUiEvent()
}
