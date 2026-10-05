package com.crm.enhance_wellness.feature.auth.domain.repository

import com.crm.enhance_wellness.feature.auth.domain.model.Patient
import com.crm.enhance_wellness.feature.auth.domain.model.PatientPermissions
import com.crm.enhance_wellness.feature.auth.domain.model.TenantBranding

interface AuthRepository {
    suspend fun getTenantBranding(slug: String): TenantBranding
    suspend fun login(email: String, password: String): Patient
    suspend fun register(email: String, password: String, name: String, otp: String): Patient
    /** Throws if the email is already registered; otherwise emails a sign-up OTP. */
    suspend fun sendRegisterOtp(email: String)
    suspend fun logout()
    suspend fun hasValidToken(): Boolean
    suspend fun getPatientPermissions(): PatientPermissions
    suspend fun isSmsAvailable(): Boolean
}
