package com.globussoft.wellness.patient.feature.auth.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OtpRequestDto(
    val phone: String,
)

@JsonClass(generateAdapter = true)
data class OtpVerifyDto(
    val phone: String,
    val otp: String,
)

@JsonClass(generateAdapter = true)
data class OtpVerifyResponseDto(
    val token: String,
    val patient: PatientSummaryDto,
)

@JsonClass(generateAdapter = true)
data class PatientSummaryDto(
    val id: Int,
    val name: String,
)

@JsonClass(generateAdapter = true)
data class RegisterPatientDto(
    val phone: String,
    val name: String,
    val email: String?,
    val dob: String?,
    val gender: String?,
)

@JsonClass(generateAdapter = true)
data class RegisterResponseDto(
    val token: String,
    val patient: PatientSummaryDto,
)

@JsonClass(generateAdapter = true)
data class TenantBrandingDto(
    val id: Int,
    val slug: String,
    val name: String,
    val brandColor: String?,
    val logoUrl: String?,
    val tagline: String?,
)
