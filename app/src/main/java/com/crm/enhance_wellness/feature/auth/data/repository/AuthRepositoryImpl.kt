package com.crm.enhance_wellness.feature.auth.data.repository

import com.crm.enhance_wellness.BuildConfig
import com.crm.enhance_wellness.core.network.WellnessApiService
import com.crm.enhance_wellness.core.storage.DataStoreManager
import com.crm.enhance_wellness.core.storage.EncryptedPrefsManager
import com.crm.enhance_wellness.feature.auth.data.mapper.toDomain
import com.crm.enhance_wellness.feature.auth.data.mapper.toPatient
import com.crm.enhance_wellness.feature.auth.data.remote.dto.LoginRequestDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.RegisterRequestDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.TenantSummaryDto
import com.crm.enhance_wellness.feature.auth.domain.model.Patient
import com.crm.enhance_wellness.feature.auth.domain.model.PatientPermissions
import com.crm.enhance_wellness.feature.auth.domain.model.TenantBranding
import com.crm.enhance_wellness.feature.auth.domain.repository.AuthRepository
import okhttp3.Cache
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
    private val dataStore: DataStoreManager,
    private val encryptedPrefs: EncryptedPrefsManager,
    private val httpCache: Cache,
) : AuthRepository {

    override suspend fun getTenantBranding(slug: String): TenantBranding {
        val response = api.getTenantBranding(slug)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.tenant.toDomain()
    }

    override suspend fun login(email: String, password: String): Patient {
        val response = api.login(LoginRequestDto(email = email, password = password))
        if (!response.isSuccessful) throw HttpException(response)
        val body = response.body()!!
        dataStore.saveToken(body.token)
        applyTenantFromAuthResponse(body.tenant)
        val patient = body.toPatient()
        encryptedPrefs.saveUserInfo(patient.userId, patient.name, patient.email)
        fetchAndSavePatientId()
        return patient
    }

    override suspend fun register(email: String, password: String, name: String): Patient {
        val response = api.registerCustomer(
            RegisterRequestDto(
                email = email,
                password = password,
                name = name,
                registrationTenantId = resolveRegistrationTenantId(),
            )
        )
        if (!response.isSuccessful) throw HttpException(response)
        val body = response.body()!!
        dataStore.saveToken(body.token)
        applyTenantFromAuthResponse(body.tenant)
        val patient = body.toPatient()
        encryptedPrefs.saveUserInfo(patient.userId, patient.name, patient.email)
        fetchAndSavePatientId()
        return patient
    }

    /**
     * Overwrite the branding Splash guessed from [BuildConfig.TENANT_SLUG] with the tenant
     * the account actually belongs to.
     *
     * Splash has to resolve branding before anyone has logged in, so it can only use the
     * build's configured slug. For a patient whose clinic differs from that slug, the
     * result was the wrong clinic name and colour on every screen. Login/register is the
     * first point at which the real tenant is known, so it wins here.
     */
    private suspend fun applyTenantFromAuthResponse(tenant: TenantSummaryDto?) {
        val branding = tenant?.toDomain() ?: return
        dataStore.saveTenantBranding(
            brandColor = branding.brandColor ?: DEFAULT_BRAND_COLOR,
            clinicName = branding.name,
            logoUrl = branding.logoUrl,
        )
        dataStore.saveTenantId(branding.id)
    }

    /**
     * Registration happens before any tenant is known for the user, so it must use the
     * tenant this build is configured for. Prefer the id Splash resolved from
     * [BuildConfig.TENANT_SLUG] over [BuildConfig.TENANT_ID]: the two are set independently
     * in build.gradle.kts and can drift, and the slug is the one that was actually verified
     * against the backend.
     */
    private suspend fun resolveRegistrationTenantId(): Int =
        dataStore.getTenantId() ?: BuildConfig.TENANT_ID

    // GET /portal/me after login to cache the patient-row ID.
    // Required for loyalty/{patientId}, patients/{patientId}/wallet, etc.
    // Non-fatal: if it fails the patientId will be fetched lazily when first needed.
    private suspend fun fetchAndSavePatientId() {
        runCatching { api.getProfile() }
            .getOrNull()
            ?.body()
            ?.id
            ?.let { encryptedPrefs.savePatientId(it) }
    }

    override suspend fun logout() {
        dataStore.clearAll()
        encryptedPrefs.clear()
        // The catalogue HTTP cache is keyed by URL, not by account. Without this, signing
        // in as a patient of a different tenant on the same device could serve the previous
        // tenant's services and plans until the entries aged out.
        runCatching { httpCache.evictAll() }
    }

    override suspend fun hasValidToken(): Boolean = dataStore.getToken() != null

    override suspend fun getPatientPermissions(): PatientPermissions {
        val response = api.getPatientPermissions()
        if (!response.isSuccessful) throw HttpException(response)
        return PatientPermissions(response.body()!!.permissions.toSet())
    }

    override suspend fun isSmsAvailable(): Boolean {
        return runCatching { api.getPortalHealth() }
            .getOrNull()
            ?.body()
            ?.smsConfigured
            ?: true
    }

    private companion object {
        const val DEFAULT_BRAND_COLOR = "#8A6D23"
    }
}
