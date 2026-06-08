package com.globussoft.wellness.patient.feature.finance.data.repository

import com.globussoft.wellness.patient.core.network.WellnessApiService
import com.globussoft.wellness.patient.feature.finance.data.mapper.toDomain
import com.globussoft.wellness.patient.feature.finance.domain.model.Payment
import com.globussoft.wellness.patient.feature.finance.domain.model.PaymentConfig
import com.globussoft.wellness.patient.feature.finance.domain.repository.FinanceRepository
import retrofit2.HttpException
import javax.inject.Inject

class FinanceRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
) : FinanceRepository {

    override suspend fun getPayments(): List<Payment> {
        val response = api.getPayments()
        if (!response.isSuccessful) throw HttpException(response)
        return response.body().orEmpty().map { it.toDomain() }
    }

    override suspend fun getPaymentConfig(): PaymentConfig? {
        val response = api.getPaymentConfig()
        if (!response.isSuccessful) return null
        return response.body()?.toDomain()
    }
}
