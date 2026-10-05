package com.crm.enhance_wellness.feature.auth.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    val email: String,
    val password: String,
//    val loginTenantId: Int?,
)

@JsonClass(generateAdapter = true)
data class UserSummaryDto(
    val id: Int,
    val email: String,
    val name: String,
    val userType: String,
)

@JsonClass(generateAdapter = true)
data class TenantSummaryDto(
    val id: Int,
    val name: String,
    val slug: String,
    val brandColor: String?,
    val logoUrl: String?,
)

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    val token: String,
    val user: UserSummaryDto,
    val tenant: TenantSummaryDto?,
)

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val name: String,
    val registrationTenantId: Int,
    // ponytail: field name assumed — confirm against backend register route
    val verificationToken: String? = null,
)

@JsonClass(generateAdapter = true)
data class CheckEmailRequestDto(val email: String, val registrationTenantId: Int)

@JsonClass(generateAdapter = true)
data class CheckEmailResponseDto(val exists: Boolean)

@JsonClass(generateAdapter = true)
data class EmailOtpRequestDto(val email: String, val purpose: String = "customer-register")

@JsonClass(generateAdapter = true)
data class EmailOtpVerifyRequestDto(val email: String, val code: String, val purpose: String = "customer-register")

@JsonClass(generateAdapter = true)
data class EmailOtpVerifyResponseDto(val verified: Boolean, val verificationToken: String?)

@JsonClass(generateAdapter = true)
data class RegisterResponseDto(
    val token: String,
    val user: UserSummaryDto,
    val tenant: TenantSummaryDto?,
)

@JsonClass(generateAdapter = true)
data class TenantBrandingDto(
    val id: Int,
    val slug: String,
    val name: String,
    val brandColor: String? = null,
    val logoUrl: String? = null,
    val tagline: String? = null,
)

// Wrapper for GET /public/tenant/{slug} — response shape: { "tenant": {...}, "services": [...] }
@JsonClass(generateAdapter = true)
data class TenantBrandingResponseDto(
    val tenant: TenantBrandingDto,
)

// GET /portal/me/permissions — { "permissions": ["my_prescriptions.read", "products.read"] }
@JsonClass(generateAdapter = true)
data class PatientPermissionsDto(
    val permissions: List<String>,
)

// GET /portal/health — { "smsConfigured": true }
@JsonClass(generateAdapter = true)
data class PortalHealthDto(
    val smsConfigured: Boolean,
)
