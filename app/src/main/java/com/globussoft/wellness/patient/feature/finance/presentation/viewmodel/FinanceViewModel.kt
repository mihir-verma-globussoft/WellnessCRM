package com.globussoft.wellness.patient.feature.finance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.finance.domain.usecase.GetPaymentsUseCase
import com.globussoft.wellness.patient.feature.finance.presentation.state.FinanceUiEvent
import com.globussoft.wellness.patient.feature.finance.presentation.state.FinanceUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FinanceNavEvent {
    object ToGiftCards : FinanceNavEvent()
    object ToWallet : FinanceNavEvent()
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val getPaymentsUseCase: GetPaymentsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private val _navEvent = Channel<FinanceNavEvent>(Channel.BUFFERED)
    val navEvent = _navEvent.receiveAsFlow()

    init {
        loadPayments()
    }

    fun onEvent(event: FinanceUiEvent) {
        when (event) {
            FinanceUiEvent.LoadPayments -> loadPayments()
            FinanceUiEvent.NavigateToGiftCards ->
                viewModelScope.launch { _navEvent.send(FinanceNavEvent.ToGiftCards) }
            FinanceUiEvent.NavigateToWallet ->
                viewModelScope.launch { _navEvent.send(FinanceNavEvent.ToWallet) }
        }
    }

    private fun loadPayments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = getPaymentsUseCase()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    payments = result.data,
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                Result.Loading -> Unit
            }
        }
    }
}
