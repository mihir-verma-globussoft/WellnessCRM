package com.crm.enhance_wellness.feature.finance.presentation.state

import com.crm.enhance_wellness.feature.finance.domain.model.Payment
import com.crm.enhance_wellness.feature.finance.domain.model.PaymentConfig

data class FinanceUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val payments: List<Payment> = emptyList(),
    val config: PaymentConfig? = null,
    val selectedPayment: Payment? = null,
)

sealed class FinanceUiEvent {
    object LoadPayments : FinanceUiEvent()
    object NavigateToGiftCards : FinanceUiEvent()
    object NavigateToWallet : FinanceUiEvent()
    data class SelectPayment(val payment: Payment) : FinanceUiEvent()
    object DismissPaymentSheet : FinanceUiEvent()
}
