package com.globussoft.wellness.patient.feature.health.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrescriptionDto(
    val id: Int,
    val visitId: Int?,
    val visitDate: String?,
    val doctorName: String?,
    val serviceName: String?,
    val drugs: List<DrugDto>,
)

@JsonClass(generateAdapter = true)
data class DrugDto(
    val name: String,
    val dosage: String?,
    val frequency: String?,
    val duration: String?,
    val instructions: String?,
)
