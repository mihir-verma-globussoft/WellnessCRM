package com.crm.enhance_wellness.feature.health.data.repository

import com.crm.enhance_wellness.core.network.WellnessApiService
import com.crm.enhance_wellness.core.storage.EncryptedPrefsManager
import com.crm.enhance_wellness.feature.health.data.mapper.toDomain
import com.crm.enhance_wellness.feature.health.domain.model.ConsentForm
import com.crm.enhance_wellness.feature.health.domain.repository.ConsentFormRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentFormRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
    private val encryptedPrefs: EncryptedPrefsManager,
) : ConsentFormRepository {

    override suspend fun getConsentForms(): List<ConsentForm> {
        val patientId = encryptedPrefs.getPatientId()
            ?: throw IllegalStateException("patientId not cached")
        val response = api.getConsents(patientId)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.map { it.toDomain() }
    }

    override suspend fun getConsentFormPdf(consentId: Int): ByteArray {
        val response = api.getConsentPdf(consentId)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.bytes()
    }
}
