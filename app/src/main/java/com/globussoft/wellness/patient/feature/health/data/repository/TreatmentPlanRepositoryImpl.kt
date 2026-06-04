package com.globussoft.wellness.patient.feature.health.data.repository

import com.globussoft.wellness.patient.core.network.WellnessApiService
import com.globussoft.wellness.patient.core.storage.EncryptedPrefsManager
import com.globussoft.wellness.patient.feature.health.data.mapper.toDomain
import com.globussoft.wellness.patient.feature.health.domain.model.TreatmentPlan
import com.globussoft.wellness.patient.feature.health.domain.repository.TreatmentPlanRepository
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
