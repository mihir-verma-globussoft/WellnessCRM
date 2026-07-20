package com.crm.enhance_wellness.feature.health.data.repository

import com.crm.enhance_wellness.core.network.WellnessApiService
import com.crm.enhance_wellness.core.storage.EncryptedPrefsManager
import com.crm.enhance_wellness.feature.health.data.mapper.toDomain
import com.crm.enhance_wellness.feature.health.domain.model.TreatmentPlan
import com.crm.enhance_wellness.feature.health.domain.repository.TreatmentPlanRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreatmentPlanRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
    private val encryptedPrefs: EncryptedPrefsManager,
) : TreatmentPlanRepository {

    override suspend fun getTreatmentPlans(): List<TreatmentPlan> {
        val patientId = encryptedPrefs.getPatientId()
            ?: throw IllegalStateException("patientId not cached")
        val response = api.getTreatmentPlans(patientId)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.map { it.toDomain() }
    }
}
