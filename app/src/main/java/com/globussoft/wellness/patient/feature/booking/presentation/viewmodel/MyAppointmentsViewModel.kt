package com.globussoft.wellness.patient.feature.booking.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.booking.domain.usecase.CancelAppointmentUseCase
import com.globussoft.wellness.patient.feature.booking.domain.usecase.GetMyAppointmentsUseCase
import com.globussoft.wellness.patient.feature.booking.presentation.state.MyAppointmentsUiEvent
import com.globussoft.wellness.patient.feature.booking.presentation.state.MyAppointmentsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MyAppointmentsNavEvent {
    object ToBook : MyAppointmentsNavEvent()
    object ToHistory : MyAppointmentsNavEvent()
    object Back : MyAppointmentsNavEvent()
}

@HiltViewModel
class MyAppointmentsViewModel @Inject constructor(
    private val getMyAppointments: GetMyAppointmentsUseCase,
    private val cancelAppointment: CancelAppointmentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyAppointmentsUiState())
    val uiState: StateFlow<MyAppointmentsUiState> = _uiState.asStateFlow()

    private val _navEvent = Channel<MyAppointmentsNavEvent>(Channel.BUFFERED)
    val navEvent = _navEvent.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: MyAppointmentsUiEvent) {
        when (event) {
            MyAppointmentsUiEvent.Refresh -> load()
            is MyAppointmentsUiEvent.Cancel -> cancel(event.appointmentId)
            MyAppointmentsUiEvent.NavigateToBook -> emit(MyAppointmentsNavEvent.ToBook)
            MyAppointmentsUiEvent.NavigateToHistory -> emit(MyAppointmentsNavEvent.ToHistory)
            MyAppointmentsUiEvent.NavigateBack -> emit(MyAppointmentsNavEvent.Back)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val upcomingResult = getMyAppointments("upcoming")
            val pastResult = getMyAppointments("past")
            _uiState.value = MyAppointmentsUiState(
                isLoading = false,
                upcoming = if (upcomingResult is Result.Success) upcomingResult.data else emptyList(),
                past = if (pastResult is Result.Success) pastResult.data else emptyList(),
                error = if (upcomingResult is Result.Error) upcomingResult.message else null,
            )
        }
    }

    private fun cancel(appointmentId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cancellingId = appointmentId)
            when (cancelAppointment(appointmentId)) {
                is Result.Success -> load()
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    cancellingId = null,
                    error = "Failed to cancel appointment",
                )
                Result.Loading -> Unit
            }
        }
    }

    private fun emit(event: MyAppointmentsNavEvent) {
        viewModelScope.launch { _navEvent.send(event) }
    }
}
