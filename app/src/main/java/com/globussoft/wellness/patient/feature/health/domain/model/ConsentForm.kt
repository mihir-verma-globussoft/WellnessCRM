package com.globussoft.wellness.patient.feature.health.domain.model

data class ConsentForm(
    val id: Int,
    val templateName: String,
    val signedAt: String,
    val hasPdfBlob: Boolean,
    val serviceName: String?,
)
