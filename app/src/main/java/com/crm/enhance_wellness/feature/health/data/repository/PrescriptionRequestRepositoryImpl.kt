package com.crm.enhance_wellness.feature.health.data.repository

import com.crm.enhance_wellness.core.network.WellnessApiService
import com.crm.enhance_wellness.feature.health.data.mapper.toDomain
import com.crm.enhance_wellness.feature.health.data.remote.dto.PrescriptionRequestDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.SubmitPrescriptionRequestDto
import com.crm.enhance_wellness.feature.health.domain.model.PrescriptionRequest
import com.crm.enhance_wellness.feature.health.domain.repository.PrescriptionRequestRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrescriptionRequestRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
    private val moshi: Moshi,
) : PrescriptionRequestRepository {

    private val listAdapter =
        moshi.adapter<List<PrescriptionRequestDto>>(
            Types.newParameterizedType(List::class.java, PrescriptionRequestDto::class.java),
        )

    override suspend fun getPrescriptionRequests(): List<PrescriptionRequest> {
        val response = api.getPrescriptionRequests()
        if (!response.isSuccessful) throw HttpException(response)

        // The backend sends an empty body (not `[]`) when the patient has no requests.
        val raw = response.body()?.string()?.trim().orEmpty()
        if (raw.isEmpty()) return emptyList()

        return listAdapter.fromJson(raw).orEmpty().map { it.toDomain() }
    }

    override suspend fun submitRenewalRequest(
        prescriptionId: Int,
        medicines: List<String>?,
        durationDays: Int?,
        notes: String?,
    ) {
        val response = api.submitPrescriptionRequest(
            SubmitPrescriptionRequestDto(
                prescriptionId = prescriptionId,
                medicines = medicines,
                durationDays = durationDays,
                notes = notes,
            ),
        )
        if (!response.isSuccessful) throw HttpException(response)
    }
}
