package com.globussoft.wellness.patient.feature.auth.data.repository

import com.globussoft.wellness.patient.core.network.WellnessApiService
import com.globussoft.wellness.patient.core.storage.DataStoreManager
import com.globussoft.wellness.patient.core.storage.EncryptedPrefsManager
import com.globussoft.wellness.patient.feature.auth.data.mapper.toDomain
import com.globussoft.wellness.patient.feature.auth.data.mapper.toPatient
import com.globussoft.wellness.patient.feature.auth.data.remote.dto.LoginRequestDto
import com.globussoft.wellness.patient.feature.auth.data.remote.dto.RegisterRequestDto
import com.globussoft.wellness.patient.feature.auth.domain.model.Patient
import com.globussoft.wellness.patient.feature.auth.domain.model.TenantBranding
import com.globussoft.wellness.patient.feature.auth.domain.repository.AuthRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
    private val dataStore: DataStoreManager,
    private val encryptedPrefs: EncryptedPrefsManager,
) : AuthRepository {

    override suspend fun getTenantBranding(slug: String): TenantBranding {
        val response = api.getTenantBranding(slug)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.tenant.toDomain()
    }

    override suspend fun login(email: String, password: String): Patient {
        val tenantId = dataStore.getTenantId()
        val response = api.login(LoginRequestDto(email = email, password = password, loginTenantId = tenantId))
        if (!response.isSuccessful) throw HttpException(response)
        val body = response.body()!!
        dataStore.saveToken(body.token)
        val patient = body.toPatient()
        encryptedPrefs.saveUserInfo(patient.userId, patient.name, patient.email)
        return patient
    }

    override suspend fun register(email: String, password: String, name: String): Patient {
        val tenantId = dataStore.getTenantId()
            ?: throw IllegalStateException("Tenant ID not loaded — splash must run first")
        val response = api.registerCustomer(
            RegisterRequestDto(
                email = email,
                password = password,
                name = name,
                registrationTenantId = tenantId,
            )
        )
        if (!response.isSuccessful) throw HttpException(response)
        val body = response.body()!!
        dataStore.saveToken(body.token)
        val patient = body.toPatient()
        encryptedPrefs.saveUserInfo(patient.userId, patient.name, patient.email)
        return patient
    }

    override suspend fun logout() {
        dataStore.clearAll()
        encryptedPrefs.clear()
    }

    override suspend fun hasValidToken(): Boolean = dataStore.getToken() != null
}
