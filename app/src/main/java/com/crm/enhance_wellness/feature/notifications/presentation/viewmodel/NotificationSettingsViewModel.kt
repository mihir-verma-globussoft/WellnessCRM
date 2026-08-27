package com.crm.enhance_wellness.feature.notifications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.notifications.domain.model.NotificationPreferences
import com.crm.enhance_wellness.feature.notifications.domain.usecase.GetNotificationPreferencesUseCase
import com.crm.enhance_wellness.feature.notifications.domain.usecase.SaveNotificationPreferencesUseCase
import com.crm.enhance_wellness.feature.notifications.presentation.state.NotificationSettingsUiEvent
import com.crm.enhance_wellness.feature.notifications.presentation.state.NotificationSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val getPreferences: GetNotificationPreferencesUseCase,
    private val savePreferences: SaveNotificationPreferencesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSavedPreferences()
    }

    private fun loadSavedPreferences() {
        viewModelScope.launch {
            val saved = getPreferences().first()
            _uiState.value = _uiState.value.copy(
                categories = _uiState.value.categories.map {
                    it.copy(enabled = saved.isCategoryEnabled(it.key))
                },
                channels = _uiState.value.channels.map {
                    it.copy(enabled = saved.isChannelEnabled(it.key))
                },
                quietStart = saved.quietStart,
                quietEnd = saved.quietEnd,
                isLoading = false,
            )
        }
    }

    fun onEvent(event: NotificationSettingsUiEvent) {
        when (event) {
            is NotificationSettingsUiEvent.ToggleCategory -> {
                val updated = _uiState.value.categories.map {
                    if (it.key == event.key) it.copy(enabled = event.enabled) else it
                }
                _uiState.value = _uiState.value.copy(categories = updated, saveSuccess = false)
            }
            is NotificationSettingsUiEvent.ToggleChannel -> {
                val updated = _uiState.value.channels.map {
                    if (it.key == event.key) it.copy(enabled = event.enabled) else it
                }
                _uiState.value = _uiState.value.copy(channels = updated, saveSuccess = false)
            }
            is NotificationSettingsUiEvent.SetQuietStart ->
                _uiState.value = _uiState.value.copy(quietStart = event.time, saveSuccess = false)
            is NotificationSettingsUiEvent.SetQuietEnd ->
                _uiState.value = _uiState.value.copy(quietEnd = event.time, saveSuccess = false)
            NotificationSettingsUiEvent.Save -> save()
        }
    }

    private fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isSaving = true, saveSuccess = false, error = null)
            val result = savePreferences(
                NotificationPreferences(
                    enabledCategories = state.categories.filter { it.enabled }.map { it.key }.toSet(),
                    enabledChannels = state.channels.filter { it.enabled }.map { it.key }.toSet(),
                    quietStart = state.quietStart,
                    quietEnd = state.quietEnd,
                )
            )
            _uiState.value = when (result) {
                is Result.Success -> _uiState.value.copy(isSaving = false, saveSuccess = true)
                is Result.Error -> _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = false,
                    error = result.message,
                )
                Result.Loading -> _uiState.value.copy(isSaving = true)
            }
        }
    }
}
