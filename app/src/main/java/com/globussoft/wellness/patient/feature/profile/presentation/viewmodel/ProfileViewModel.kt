package com.globussoft.wellness.patient.feature.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.auth.domain.usecase.LogoutUseCase
import com.globussoft.wellness.patient.feature.profile.domain.usecase.GetProfileUseCase
import com.globussoft.wellness.patient.feature.profile.domain.usecase.RequestDsarExportUseCase
import com.globussoft.wellness.patient.feature.profile.domain.usecase.UpdateProfileUseCase
import com.globussoft.wellness.patient.feature.profile.presentation.state.ProfileUiEvent
import com.globussoft.wellness.patient.feature.profile.presentation.state.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileNavEvent {
    object Back : ProfileNavEvent()
    object ToLogin : ProfileNavEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val requestDsarExport: RequestDsarExportUseCase,
    private val logout: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _navEvent = Channel<ProfileNavEvent>(Channel.BUFFERED)
    val navEvent = _navEvent.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.Refresh -> load()
            ProfileUiEvent.StartEdit -> {
                val p = _uiState.value.profile ?: return
                _uiState.value = _uiState.value.copy(
                    isEditing = true,
                    editName = p.name,
                    editEmail = p.email ?: "",
                    currentPassword = "",
                    newPassword = "",
                    saveError = null,
                )
            }
            ProfileUiEvent.CancelEdit -> _uiState.value = _uiState.value.copy(isEditing = false, saveError = null)
            is ProfileUiEvent.EditName -> _uiState.value = _uiState.value.copy(editName = event.name)
            is ProfileUiEvent.EditEmail -> _uiState.value = _uiState.value.copy(editEmail = event.email)
            is ProfileUiEvent.EditCurrentPassword -> _uiState.value = _uiState.value.copy(currentPassword = event.password)
            is ProfileUiEvent.EditNewPassword -> _uiState.value = _uiState.value.copy(newPassword = event.password)
            ProfileUiEvent.SaveChanges -> save()
            ProfileUiEvent.RequestDsarExport -> requestExport()
            ProfileUiEvent.Logout -> doLogout()
            ProfileUiEvent.NavigateBack -> viewModelScope.launch { _navEvent.send(ProfileNavEvent.Back) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = getProfile()) {
                is Result.Success -> _uiState.value = ProfileUiState(isLoading = false, profile = result.data)
                is Result.Error -> _uiState.value = ProfileUiState(isLoading = false, error = result.message)
                Result.Loading -> Unit
            }
        }
    }

    private fun save() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true, saveError = null)
            val result = updateProfile(
                name = s.editName.takeIf { it.isNotBlank() },
                email = s.editEmail.takeIf { it.isNotBlank() },
                currentPassword = s.currentPassword.takeIf { it.isNotBlank() },
                newPassword = s.newPassword.takeIf { it.isNotBlank() },
            )
            when (result) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isSaving = false, isEditing = false, saveSuccess = true, profile = result.data,
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(isSaving = false, saveError = result.message)
                Result.Loading -> Unit
            }
        }
    }

    private fun requestExport() {
        viewModelScope.launch {
            requestDsarExport()
            _uiState.value = _uiState.value.copy(exportRequested = true)
        }
    }

    private fun doLogout() {
        viewModelScope.launch {
            logout()
            _navEvent.send(ProfileNavEvent.ToLogin)
        }
    }
}
