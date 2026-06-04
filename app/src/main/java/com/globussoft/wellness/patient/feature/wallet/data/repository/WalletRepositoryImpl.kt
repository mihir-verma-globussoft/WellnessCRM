package com.globussoft.wellness.patient.feature.wallet.data.repository

import com.globussoft.wellness.patient.core.network.WellnessApiService
import com.globussoft.wellness.patient.feature.wallet.data.mapper.toDomain
import com.globussoft.wellness.patient.feature.wallet.domain.model.WalletSummary
import com.globussoft.wellness.patient.feature.wallet.domain.repository.WalletRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
) : WalletRepository {

    override suspend fun getWalletSummary(patientId: Int): WalletSummary {
        val response = api.getPatientWallet(patientId)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.toDomain()
    }

    override suspend fun getMyTransactions(): WalletSummary {
        val response = api.getMyTransactions()
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.toDomain()
    }
}
