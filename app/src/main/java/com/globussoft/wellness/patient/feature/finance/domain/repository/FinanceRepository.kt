package com.globussoft.wellness.patient.feature.finance.domain.repository

import com.globussoft.wellness.patient.feature.finance.domain.model.Payment
import com.globussoft.wellness.patient.feature.finance.domain.model.PaymentConfig

interface FinanceRepository {
    suspend fun getPayments(): List<Payment>
    suspend fun getPaymentConfig(): PaymentConfig?
}
