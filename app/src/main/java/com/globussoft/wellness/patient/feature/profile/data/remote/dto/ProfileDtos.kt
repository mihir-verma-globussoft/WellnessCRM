package com.globussoft.wellness.patient.feature.profile.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileDto(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String?,
    val dob: String?,
    val gender: String?,
)

@JsonClass(generateAdapter = true)
data class UpdateProfileDto(
    val name: String?,
    val email: String?,
    val dob: String?,
    val gender: String?,
)

@JsonClass(generateAdapter = true)
data class DsarExportResponseDto(
    val ok: Boolean,
    val message: String?,
)
