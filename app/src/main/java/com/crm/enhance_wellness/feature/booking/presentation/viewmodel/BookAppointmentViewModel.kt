package com.crm.enhance_wellness.feature.booking.presentation.viewmodel

import com.crm.enhance_wellness.core.util.DateUtil
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crm.enhance_wellness.core.network.WellnessApiService
import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.booking.domain.usecase.BookAppointmentUseCase
import com.crm.enhance_wellness.feature.booking.domain.usecase.GetPortalProductsUseCase
import com.crm.enhance_wellness.feature.booking.presentation.state.BookAppointmentUiEvent
import com.crm.enhance_wellness.feature.booking.presentation.state.BookAppointmentUiState
import com.crm.enhance_wellness.feature.booking.presentation.state.DoctorOption
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
    private val savedStateHandle: SavedStateHandle,
    private val getProducts: GetPortalProductsUseCase,
    private val bookAppointment: BookAppointmentUseCase,
    private val apiService: WellnessApiService,
) : ViewModel() {

    private val preselectedServiceId: Int? = savedStateHandle.get<String>("serviceId")?.toIntOrNull()

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
            is BookAppointmentUiEvent.UpdateServiceSearch -> _uiState.value = _uiState.value.copy(serviceSearchQuery = event.query)
            is BookAppointmentUiEvent.SelectProduct -> _uiState.value = _uiState.value.copy(selectedProduct = event.product, error = null)
            is BookAppointmentUiEvent.SelectDoctor -> _uiState.value = _uiState.value.copy(selectedDoctorId = event.doctorId, error = null)
            is BookAppointmentUiEvent.SelectDate -> {
                val normalizedDate = DateUtil.datePickerMillisToLocalMidnight(event.epochMs)
                val slots = DateUtil.generateTimeSlots(
                    normalizedDate,
                    startHour = BUSINESS_HOURS_START,
                    endHour = BUSINESS_HOURS_END,
                    intervalMinutes = SLOT_INTERVAL_MINUTES,
                )
                _uiState.value = _uiState.value.copy(
                    selectedDate = normalizedDate,
                    availableTimeSlots = slots,
                    selectedTime = null,
                    error = null,
                )
            }
            is BookAppointmentUiEvent.SelectTime -> {
                val time = event.time
                if (time in _uiState.value.availableTimeSlots &&
                    DateUtil.isFutureDateTime(_uiState.value.selectedDate ?: 0L, time)
                ) {
                    _uiState.value = _uiState.value.copy(selectedTime = time, error = null)
                } else {
                    _uiState.value = _uiState.value.copy(
                        selectedTime = null,
                        error = "Selected time slot is no longer available",
                    )
                }
            }
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
                is Result.Success -> {
                    val preselected = preselectedServiceId?.let { id -> result.data.find { it.id == id } }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = result.data,
                        selectedProduct = preselected,
                    )
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                Result.Loading -> Unit
            }
        }
    }

    private fun loadDoctors(date: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getDoctorAvailability(date)
                if (response.isSuccessful) {
                    val dtos = response.body() ?: emptyList()
                    val options = listOf(DoctorOption(id = null, name = "No preference")) +
                        dtos.filter { it.available }.map { DoctorOption(id = it.id, name = it.name) }
                    _uiState.value = _uiState.value.copy(doctors = options)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    doctors = listOf(DoctorOption(id = null, name = "No preference")),
                )
            }
        }
    }

    private fun nextStep() {
        val s = _uiState.value
        when (s.step) {
            1 -> {
                if (s.selectedProduct != null) {
                    val dateStr = s.selectedDate?.let { DateUtil.toApiDate(it) } ?: DateUtil.todayApiDate()
                    loadDoctors(dateStr)
                    _uiState.value = s.copy(step = 2, error = null)
                } else {
                    _uiState.value = s.copy(error = "Please select a service")
                }
            }
            2 -> _uiState.value = s.copy(step = 3, error = null)
            3 -> {
                val date = s.selectedDate
                val time = s.selectedTime
                when {
                    date == null || time == null -> _uiState.value = s.copy(error = "Please select a date and time")
                    date < DateUtil.startOfTodayUtcMillis() -> _uiState.value = s.copy(error = "Please select a future date")
                    !DateUtil.isFutureDateTime(date, time) -> _uiState.value = s.copy(error = "Please select a future time slot")
                    time !in s.availableTimeSlots -> _uiState.value = s.copy(error = "Selected time slot is no longer available")
                    else -> _uiState.value = s.copy(step = 4, error = null)
                }
            }
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
        when {
            dateMs < DateUtil.startOfTodayUtcMillis() -> {
                _uiState.value = s.copy(error = "Please select a future date")
                return
            }
            !DateUtil.isFutureDateTime(dateMs, time) -> {
                _uiState.value = s.copy(error = "Please select a future time slot")
                return
            }
            time !in s.availableTimeSlots -> {
                _uiState.value = s.copy(error = "Selected time slot is no longer available")
                return
            }
        }
        viewModelScope.launch {
            _uiState.value = s.copy(isBooking = true, error = null)
            val result = bookAppointment(
                appointmentDate = DateUtil.toApiDate(dateMs),
                appointmentTime = time,
                reason = s.reason,
                serviceId = s.selectedProduct?.id,
                membershipId = s.membershipId,
                doctorId = s.selectedDoctorId,
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

    companion object {
        const val BUSINESS_HOURS_START = 9
        const val BUSINESS_HOURS_END = 18
        const val SLOT_INTERVAL_MINUTES = 30
    }
}
