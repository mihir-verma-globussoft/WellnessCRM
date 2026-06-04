package com.globussoft.wellness.patient.feature.profile.domain.repository

import com.globussoft.wellness.patient.feature.profile.domain.model.Profile

interface ProfileRepository {
    suspend fun getProfile(): Profile
    suspend fun updateProfile(name: String?, email: String?, currentPassword: String?, newPassword: String?): Profile
    suspend fun requestDsarExport()
    suspend fun logout()
}
