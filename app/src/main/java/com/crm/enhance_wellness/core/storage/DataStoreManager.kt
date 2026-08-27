package com.crm.enhance_wellness.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        val KEY_JWT = stringPreferencesKey("portal_jwt")
        val KEY_BRAND_COLOR = stringPreferencesKey("tenant_brand_color")
        val KEY_CLINIC_NAME = stringPreferencesKey("tenant_clinic_name")
        val KEY_CLINIC_LOGO = stringPreferencesKey("tenant_clinic_logo_url")
        val KEY_TENANT_ID = intPreferencesKey("tenant_id")
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")

        /**
         * Device preferences that survive logout. Mirrors the keys owned by
         * `NotificationPreferencesRepositoryImpl`; referenced by name rather than by
         * import so core/ does not depend on a feature package.
         */
        private val DEVICE_PREFERENCE_KEYS: List<Preferences.Key<*>> = listOf(
            stringSetPreferencesKey("notif_enabled_categories"),
            stringSetPreferencesKey("notif_enabled_channels"),
            stringPreferencesKey("notif_quiet_start"),
            stringPreferencesKey("notif_quiet_end"),
        )
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { it[KEY_JWT] = token }
    }

    suspend fun getToken(): String? =
        dataStore.data.map { it[KEY_JWT] }.firstOrNull()

    fun tokenFlow() = dataStore.data.map { it[KEY_JWT] }

    /**
     * Clear session and tenant data on logout, preserving device-level preferences.
     *
     * Dark mode and notification settings belong to the device, not the patient — wiping
     * them on logout made the app forget the user's choices every time they signed out.
     * Nothing preserved here is patient data.
     */
    suspend fun clearAll() {
        dataStore.edit { prefs ->
            val darkTheme = prefs[KEY_DARK_THEME]
            val preserved = DEVICE_PREFERENCE_KEYS.mapNotNull { key ->
                prefs.asMap()[key]?.let { key to it }
            }
            prefs.clear()
            darkTheme?.let { prefs[KEY_DARK_THEME] = it }
            preserved.forEach { (key, value) ->
                @Suppress("UNCHECKED_CAST")
                prefs[key as Preferences.Key<Any>] = value
            }
        }
    }

    suspend fun saveTenantBranding(brandColor: String, clinicName: String, logoUrl: String?) {
        dataStore.edit { prefs ->
            prefs[KEY_BRAND_COLOR] = brandColor
            prefs[KEY_CLINIC_NAME] = clinicName
            if (logoUrl != null) prefs[KEY_CLINIC_LOGO] = logoUrl
        }
    }

    suspend fun getBrandColor(): String? =
        dataStore.data.map { it[KEY_BRAND_COLOR] }.firstOrNull()

    suspend fun getClinicName(): String? =
        dataStore.data.map { it[KEY_CLINIC_NAME] }.firstOrNull()

    suspend fun saveTenantId(tenantId: Int) {
        dataStore.edit { it[KEY_TENANT_ID] = tenantId }
    }

    suspend fun getTenantId(): Int? =
        dataStore.data.map { it[KEY_TENANT_ID] }.firstOrNull()

    fun isDarkThemeFlow() = dataStore.data.map { it[KEY_DARK_THEME] ?: false }

    fun clinicNameFlow() = dataStore.data.map { it[KEY_CLINIC_NAME] ?: "" }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }
}
