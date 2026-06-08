package com.globussoft.wellness.patient.feature.finance.presentation.state

import com.globussoft.wellness.patient.feature.finance.domain.model.Payment
import com.globussoft.wellness.patient.feature.finance.domain.model.PaymentConfig

data class FinanceUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val payments: List<Payment> = emptyList(),
    val config: PaymentConfig? = null,
)

sealed class FinanceUiEvent {
    object LoadPayments : FinanceUiEvent()
    object NavigateToGiftCards : FinanceUiEvent()
    object NavigateToWallet : FinanceUiEvent()
}
