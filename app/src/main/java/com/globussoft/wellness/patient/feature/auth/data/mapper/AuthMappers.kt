package com.globussoft.wellness.patient.feature.auth.data.mapper

import com.globussoft.wellness.patient.feature.auth.data.remote.dto.LoginResponseDto
import com.globussoft.wellness.patient.feature.auth.data.remote.dto.RegisterResponseDto
import com.globussoft.wellness.patient.feature.auth.data.remote.dto.TenantBrandingDto
import com.globussoft.wellness.patient.feature.auth.domain.model.Patient
import com.globussoft.wellness.patient.feature.auth.domain.model.TenantBranding

fun TenantBrandingDto.toDomain(): TenantBranding = TenantBranding(
    id = id,
    slug = slug,
    name = name,
    brandColor = brandColor,
    logoUrl = logoUrl,
    tagline = tagline,
)

fun LoginResponseDto.toPatient(): Patient = Patient(
    userId = user.id,
    name = user.name,
    email = user.email,
)

fun RegisterResponseDto.toPatient(): Patient = Patient(
    userId = user.id,
    name = user.name,
    email = user.email,
)
