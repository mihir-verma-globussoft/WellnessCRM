package com.crm.enhance_wellness.core.network

import com.crm.enhance_wellness.core.storage.AuthSessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authSessionManager: AuthSessionManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.getToken() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        val response = chain.proceed(request)
        if (response.code == HTTP_UNAUTHORIZED) {
            runBlocking { authSessionManager.clearUnauthorizedSession() }
        }
        return response
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
