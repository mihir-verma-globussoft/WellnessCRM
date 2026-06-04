package com.globussoft.wellness.patient.feature.booking.presentation.viewmodel

import com.globussoft.wellness.patient.core.util.DateUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.booking.domain.usecase.BookAppointmentUseCase
import com.globussoft.wellness.patient.feature.booking.domain.usecase.GetPortalProductsUseCase
import com.globussoft.wellness.patient.feature.booking.presentation.state.BookAppointmentUiEvent
import com.globussoft.wellness.patient.feature.booking.presentation.state.BookAppointmentUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BookAppointmentNavEvent {
    object Back : BookAppointmentNavEvent()
    object BookingSuccess : BookAppointmentNavEvent()
}

@HiltViewModel
class BookAppointmentViewModel @Inject constructor(
    private val getProducts: GetPortalProductsUseCase,
    private val bookAppointment: BookAppointmentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookAppointmentUiState())
    val uiState: StateFlow<BookAppointmentUiState> = _uiState.asStateFlow()

    private val _navEvent = Channel<BookAppointmentNavEvent>(Channel.BUFFERED)
    val navEvent = _navEvent.receiveAsFlow()

    init {
        loadProducts()
    }

    fun onEvent(event: BookAppointmentUiEvent) {
        when (event) {
            BookAppointmentUiEvent.LoadProducts -> loadProducts()
            is BookAppointmentUiEvent.SelectProduct -> _uiState.value = _uiState.value.copy(selectedProduct = event.product)
            is BookAppointmentUiEvent.SelectDate -> _uiState.value = _uiState.value.copy(selectedDate = event.epochMs)
            is BookAppointmentUiEvent.SelectTime -> _uiState.value = _uiState.value.copy(selectedTime = event.time)
            is BookAppointmentUiEvent.EnterReason -> _uiState.value = _uiState.value.copy(reason = event.reason)
            is BookAppointmentUiEvent.SelectMembership -> _uiState.value = _uiState.value.copy(membershipId = event.membershipId)
            BookAppointmentUiEvent.NextStep -> nextStep()
            BookAppointmentUiEvent.PreviousStep -> previousStep()
            BookAppointmentUiEvent.ConfirmBooking -> confirmBooking()
            BookAppointmentUiEvent.NavigateBack -> viewModelScope.launch { _navEvent.send(BookAppointmentNavEvent.Back) }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = getProducts()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = result.data,
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                Result.Loading -> Unit
            }
        }
    }

    private fun nextStep() {
        val s = _uiState.value
        when (s.step) {
            1 -> if (s.selectedProduct != null) _uiState.value = s.copy(step = 2)
                 else _uiState.value = s.copy(error = "Please select a service")
            2 -> if (s.selectedDate != null && s.selectedTime != null) _uiState.value = s.copy(step = 3)
                 else _uiState.value = s.copy(error = "Please select a date and time")
            else -> Unit
        }
    }

    private fun previousStep() {
        val s = _uiState.value
        if (s.step > 1) _uiState.value = s.copy(step = s.step - 1, error = null)
        else viewModelScope.launch { _navEvent.send(BookAppointmentNavEvent.Back) }
    }

    private fun confirmBooking() {
        val s = _uiState.value
        if (s.reason.isBlank()) {
            _uiState.value = s.copy(error = "Please enter a reason for your visit")
            return
        }
        val dateMs = s.selectedDate ?: return
        val time = s.selectedTime ?: return
        viewModelScope.launch {
            _uiState.value = s.copy(isBooking = true, error = null)
            val result = bookAppointment(
                appointmentDate = DateUtil.toApiDate(dateMs) + "T00:00:00Z",
                appointmentTime = time,
                reason = s.reason,
                serviceId = s.selectedProduct?.id,
                membershipId = s.membershipId,
            )
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isBooking = false, bookingSuccess = result.data)
                    _navEvent.send(BookAppointmentNavEvent.BookingSuccess)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isBooking = false, error = result.message)
                Result.Loading -> Unit
            }
        }
    }
}
