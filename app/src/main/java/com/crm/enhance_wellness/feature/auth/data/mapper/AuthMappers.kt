package com.crm.enhance_wellness.feature.auth.data.mapper

import com.crm.enhance_wellness.feature.auth.data.remote.dto.LoginResponseDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.RegisterResponseDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.TenantBrandingDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.TenantSummaryDto
import com.crm.enhance_wellness.feature.auth.domain.model.Patient
import com.crm.enhance_wellness.feature.auth.domain.model.TenantBranding

fun TenantBrandingDto.toDomain(): TenantBranding = TenantBranding(
    id = id,
    slug = slug,
    name = name,
    brandColor = brandColor,
    logoUrl = logoUrl,
    tagline = tagline,
)

/**
 * The tenant block returned by login/register. This is the *authoritative* tenant for
 * the signed-in user — it comes from their account, not from the build's configured
 * slug — so it takes precedence over whatever Splash pre-loaded.
 */
fun TenantSummaryDto.toDomain(): TenantBranding = TenantBranding(
    id = id,
    slug = slug,
    name = name,
    brandColor = brandColor,
    logoUrl = logoUrl,
    tagline = null,
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
