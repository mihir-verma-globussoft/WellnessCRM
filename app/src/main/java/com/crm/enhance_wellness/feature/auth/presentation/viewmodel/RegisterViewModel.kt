package com.crm.enhance_wellness.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.auth.domain.usecase.RegisterPatientUseCase
import com.crm.enhance_wellness.feature.auth.domain.usecase.SendRegisterOtpUseCase
import com.crm.enhance_wellness.feature.auth.domain.usecase.ValidatePasswordUseCase
import com.crm.enhance_wellness.feature.auth.presentation.state.RegisterUiEvent
import com.crm.enhance_wellness.feature.auth.presentation.state.RegisterUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegisterNavEvent {
    object NavigateToDashboard : RegisterNavEvent()
    object NavigateToLogin : RegisterNavEvent()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerPatient: RegisterPatientUseCase,
    private val sendRegisterOtp: SendRegisterOtpUseCase,
    private val validatePassword: ValidatePasswordUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _navEvent = Channel<RegisterNavEvent>(Channel.BUFFERED)
    val navEvent = _navEvent.receiveAsFlow()

    fun onEvent(event: RegisterUiEvent) {
        when (event) {
            is RegisterUiEvent.NameChanged -> _uiState.update { it.copy(name = event.name, error = null) }
            is RegisterUiEvent.EmailChanged -> _uiState.update { it.copy(email = event.email, otp = "", otpSent = false, error = null) }
            is RegisterUiEvent.PasswordChanged -> _uiState.update { it.copy(password = event.password, passwordRules = validatePassword(event.password), error = null) }
            is RegisterUiEvent.ConfirmPasswordChanged -> _uiState.update { it.copy(confirmPassword = event.password, error = null) }
            is RegisterUiEvent.OtpChanged -> _uiState.update { it.copy(otp = event.otp.filter(Char::isDigit).take(6), error = null) }
            RegisterUiEvent.ResendOtp -> sendOtp()
            RegisterUiEvent.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            RegisterUiEvent.Submit -> submitRegistration()
            RegisterUiEvent.NavigateToLogin -> viewModelScope.launch { _navEvent.send(RegisterNavEvent.NavigateToLogin) }
        }
    }

    private fun sendOtp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = sendRegisterOtp(_uiState.value.email.trim())) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, otpSent = true, otp = "") }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                Result.Loading -> Unit
            }
        }
    }

    private fun submitRegistration() {
        val state = _uiState.value
        when {
            state.name.isBlank() -> { _uiState.update { it.copy(error = "Full name is required") }; return }
            state.email.isBlank() -> { _uiState.update { it.copy(error = "Email is required") }; return }
            validatePassword(state.password).any { !it.met } -> { _uiState.update { it.copy(passwordRules = validatePassword(state.password), error = "Password does not meet all requirements") }; return }
            state.password != state.confirmPassword -> { _uiState.update { it.copy(error = "Passwords do not match") }; return }
        }
        if (!state.otpSent) { sendOtp(); return }
        if (state.otp.length < 6) { _uiState.update { it.copy(error = "Enter the 6-digit code sent to your email") }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = registerPatient(state.email.trim(), state.password, state.name.trim(), state.otp)) {
                is Result.Success -> _navEvent.send(RegisterNavEvent.NavigateToDashboard)
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                Result.Loading -> Unit
            }
        }
    }
}
