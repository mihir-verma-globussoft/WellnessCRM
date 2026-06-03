package com.globussoft.wellness.patient.core.fcm

import com.globussoft.wellness.patient.core.network.WellnessApiService
import com.globussoft.wellness.patient.feature.wallet.data.remote.dto.FcmTokenDto
import javax.inject.Inject
import javax.inject.Singleton

// Full implementation in Phase 9 — requires POST/DELETE /portal/me/fcm-token backend endpoints
@Singleton
class FcmHelper @Inject constructor(
    private val apiService: WellnessApiService,
) {
    suspend fun registerToken(token: String) {
        // TODO Phase 9: POST /portal/me/fcm-token  🔴 BLOCKED — endpoint not yet on backend
        apiService.registerFcmToken(FcmTokenDto(token = token, platform = "android"))
    }

    suspend fun deregisterToken() {
        // TODO Phase 9: DELETE /portal/me/fcm-token  🔴 BLOCKED — endpoint not yet on backend
        apiService.deregisterFcmToken()
    }
}
