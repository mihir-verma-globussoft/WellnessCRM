package com.crm.enhance_wellness.core.storage

import com.crm.enhance_wellness.core.network.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val encryptedPrefsManager: EncryptedPrefsManager,
) {
    suspend fun clearUnauthorizedSession() {
        tokenManager.clearToken()
        encryptedPrefsManager.clear()
    }
}
