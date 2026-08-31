package com.crm.enhance_wellness.feature.health.domain.repository

import com.crm.enhance_wellness.feature.health.domain.model.PrescriptionRequest

interface PrescriptionRequestRepository {
    suspend fun getPrescriptionRequests(): List<PrescriptionRequest>

    suspend fun submitRenewalRequest(
        prescriptionId: Int,
        medicines: List<String>?,
        durationDays: Int?,
        notes: String?,
    )
}
