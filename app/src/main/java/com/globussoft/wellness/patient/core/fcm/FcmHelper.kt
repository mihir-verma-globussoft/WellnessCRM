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
    // Stores token locally and attempts backend registration.
    // POST /portal/me/fcm-token is blocked (backend is WebPush/VAPID only) —
    // network call fails silently so no error surfaces to the user.
    suspend fun registerToken(token: String) {
        encryptedPrefs.saveFcmToken(token)
        try {
            apiService.registerFcmToken(FcmTokenDto(token = token, platform = "android"))
        } catch (_: Exception) {
            // Backend endpoint not yet implemented; local token saved above.
        }
    }

    // Clears local token and attempts backend deregistration on logout.
    suspend fun deregisterToken() {
        encryptedPrefs.saveFcmToken("")
        try {
            apiService.deregisterFcmToken()
        } catch (_: Exception) {
            // Backend endpoint not yet implemented; no action needed.
        }
    }
}
