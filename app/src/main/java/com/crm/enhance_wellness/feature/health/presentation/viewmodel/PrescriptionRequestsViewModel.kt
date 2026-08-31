package com.crm.enhance_wellness.feature.health.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.health.domain.usecase.GetPrescriptionRequestsUseCase
import com.crm.enhance_wellness.feature.health.presentation.state.PrescriptionRequestsUiEvent
import com.crm.enhance_wellness.feature.health.presentation.state.PrescriptionRequestsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PrescriptionRequestsNavEvent {
    object Back : PrescriptionRequestsNavEvent()
}

@HiltViewModel
class PrescriptionRequestsViewModel @Inject constructor(
    private val getPrescriptionRequests: GetPrescriptionRequestsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrescriptionRequestsUiState())
    val uiState: StateFlow<PrescriptionRequestsUiState> = _uiState.asStateFlow()

    private val _navEvent = Channel<PrescriptionRequestsNavEvent>(Channel.BUFFERED)
    val navEvent = _navEvent.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: PrescriptionRequestsUiEvent) {
        when (event) {
            PrescriptionRequestsUiEvent.Refresh -> load()
            PrescriptionRequestsUiEvent.NavigateBack -> viewModelScope.launch {
                _navEvent.send(PrescriptionRequestsNavEvent.Back)
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = PrescriptionRequestsUiState(isLoading = true)
            when (val result = getPrescriptionRequests()) {
                is Result.Success -> _uiState.value = PrescriptionRequestsUiState(
                    isLoading = false,
                    requests = result.data,
                )
                is Result.Error -> _uiState.value = PrescriptionRequestsUiState(
                    isLoading = false,
                    error = result.message,
                )
                Result.Loading -> Unit
            }
        }
    }
}
