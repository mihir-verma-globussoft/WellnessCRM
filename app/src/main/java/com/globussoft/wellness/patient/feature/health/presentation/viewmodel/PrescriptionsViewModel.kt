package com.globussoft.wellness.patient.feature.health.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.health.domain.usecase.GetPrescriptionsUseCase
import com.globussoft.wellness.patient.feature.health.presentation.state.PrescriptionsUiEvent
import com.globussoft.wellness.patient.feature.health.presentation.state.PrescriptionsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PrescriptionsNavEvent {
    data class ToPdf(val prescriptionId: Int) : PrescriptionsNavEvent()
    object Back : PrescriptionsNavEvent()
}

@HiltViewModel
class PrescriptionsViewModel @Inject constructor(
    private val getPrescriptions: GetPrescriptionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrescriptionsUiState())
    val uiState: StateFlow<PrescriptionsUiState> = _uiState.asStateFlow()

    private val _navEvent = Channel<PrescriptionsNavEvent>(Channel.BUFFERED)
    val navEvent = _navEvent.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: PrescriptionsUiEvent) {
        when (event) {
            PrescriptionsUiEvent.Refresh -> load()
            is PrescriptionsUiEvent.ViewPdf -> viewModelScope.launch { _navEvent.send(PrescriptionsNavEvent.ToPdf(event.prescriptionId)) }
            PrescriptionsUiEvent.NavigateBack -> viewModelScope.launch { _navEvent.send(PrescriptionsNavEvent.Back) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = getPrescriptions()) {
                is Result.Success -> _uiState.value = PrescriptionsUiState(isLoading = false, prescriptions = result.data)
                is Result.Error -> _uiState.value = PrescriptionsUiState(isLoading = false, error = result.message)
                Result.Loading -> Unit
            }
        }
    }
}
