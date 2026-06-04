package com.globussoft.wellness.patient.feature.loyalty.data.repository

import com.globussoft.wellness.patient.core.network.WellnessApiService
import com.globussoft.wellness.patient.core.storage.EncryptedPrefsManager
import com.globussoft.wellness.patient.feature.loyalty.data.mapper.toDomain
import com.globussoft.wellness.patient.feature.loyalty.domain.model.LoyaltyData
import com.globussoft.wellness.patient.feature.loyalty.domain.repository.LoyaltyRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoyaltyRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
    private val encryptedPrefs: EncryptedPrefsManager,
) : LoyaltyRepository {

    override suspend fun getLoyalty(): LoyaltyData {
        val patientId = encryptedPrefs.getPatientId()
            ?: throw IllegalStateException("patientId not cached")
        val response = api.getLoyalty(patientId)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.toDomain()
    }
}
