package com.globussoft.wellness.patient.feature.auth.domain.model

data class Patient(
    val userId: Int,
    val name: String,
    val email: String,
)
