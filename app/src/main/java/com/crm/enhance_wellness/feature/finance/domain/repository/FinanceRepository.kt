package com.crm.enhance_wellness.feature.finance.domain.repository

import com.crm.enhance_wellness.feature.finance.domain.model.Payment
import com.crm.enhance_wellness.feature.finance.domain.model.PaymentConfig

interface FinanceRepository {
    suspend fun getPayments(): List<Payment>
    suspend fun getPaymentConfig(): PaymentConfig?
}
