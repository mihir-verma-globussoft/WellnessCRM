package com.globussoft.wellness.patient.feature.profile.data.repository

import com.globussoft.wellness.patient.core.network.WellnessApiService
import com.globussoft.wellness.patient.core.storage.DataStoreManager
import com.globussoft.wellness.patient.core.storage.EncryptedPrefsManager
import com.globussoft.wellness.patient.feature.profile.data.mapper.mergeInto
import com.globussoft.wellness.patient.feature.profile.data.mapper.toDomain
import com.globussoft.wellness.patient.feature.profile.data.remote.dto.UpdateAuthProfileDto
import com.globussoft.wellness.patient.feature.profile.domain.model.Profile
import com.globussoft.wellness.patient.feature.profile.domain.repository.ProfileRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
    private val dataStore: DataStoreManager,
    private val encryptedPrefs: EncryptedPrefsManager,
) : ProfileRepository {

    override suspend fun getProfile(): Profile {
        val response = api.getProfile()
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.toDomain()
    }

    override suspend fun updateProfile(
        name: String?,
        email: String?,
        currentPassword: String?,
        newPassword: String?,
    ): Profile {
        val current = getProfile()
        val response = api.updateAuthProfile(
            UpdateAuthProfileDto(
                name = name,
                email = email,
                currentPassword = currentPassword,
                newPassword = newPassword,
            )
        )
        if (!response.isSuccessful) throw HttpException(response)
        val updated = response.body()!!.mergeInto(current)
        encryptedPrefs.saveUserInfo(encryptedPrefs.getUserId(), updated.name, updated.email ?: "")
        return updated
    }

    override suspend fun requestDsarExport() {
        val response = api.requestDsarExport()
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun logout() {
        dataStore.clearAll()
        encryptedPrefs.clear()
    }
}
