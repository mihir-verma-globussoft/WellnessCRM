package com.globussoft.wellness.patient.core.fcm

import com.globussoft.wellness.patient.core.network.WellnessApiService
import com.globussoft.wellness.patient.core.storage.EncryptedPrefsManager
import com.globussoft.wellness.patient.feature.wallet.data.remote.dto.FcmTokenDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmHelper @Inject constructor(
    private val apiService: WellnessApiService,
    private val encryptedPrefs: EncryptedPrefsManager,
) {
    suspend fun registerToken(token: String) {
        encryptedPrefs.saveFcmToken(token)
        try {
            apiService.registerFcmToken(FcmTokenDto(token = token, platform = "android"))
        } catch (_: Exception) {
            // Local token saved; network failure is non-fatal.
        }
    }

    suspend fun deregisterToken() {
        encryptedPrefs.saveFcmToken("")
        try {
            apiService.deregisterFcmToken()
        } catch (_: Exception) {
            // Non-fatal; token already cleared locally.
        }
    }
}
