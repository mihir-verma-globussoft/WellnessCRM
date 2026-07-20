package com.crm.enhance_wellness.feature.profile.domain.repository

import com.crm.enhance_wellness.feature.profile.domain.model.Profile

interface ProfileRepository {
    suspend fun getProfile(): Profile
    suspend fun updateProfile(name: String?, email: String?, currentPassword: String?, newPassword: String?): Profile
    suspend fun uploadProfilePicture(bytes: ByteArray, mimeType: String): Profile
    suspend fun removeProfilePicture(): Profile
    suspend fun requestDsarExport()
    suspend fun deleteAccount()
    suspend fun logout()
}
