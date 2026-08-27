package com.crm.enhance_wellness.feature.notifications.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.crm.enhance_wellness.feature.notifications.domain.model.NotificationPreferences
import com.crm.enhance_wellness.feature.notifications.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : NotificationPreferencesRepository {

    override fun observe(): Flow<NotificationPreferences> =
        dataStore.data.map { prefs ->
            NotificationPreferences(
                // Absent keys mean "never configured" — default to everything on rather
                // than to an empty set, which would silently mute all notifications.
                enabledCategories = prefs[KEY_CATEGORIES] ?: NotificationPreferences.ALL_CATEGORIES,
                enabledChannels = prefs[KEY_CHANNELS] ?: NotificationPreferences.ALL_CHANNELS,
                quietStart = prefs[KEY_QUIET_START] ?: NotificationPreferences.DEFAULT_QUIET_START,
                quietEnd = prefs[KEY_QUIET_END] ?: NotificationPreferences.DEFAULT_QUIET_END,
            )
        }

    override suspend fun get(): NotificationPreferences = observe().first()

    override suspend fun save(preferences: NotificationPreferences) {
        dataStore.edit { prefs ->
            prefs[KEY_CATEGORIES] = preferences.enabledCategories
            prefs[KEY_CHANNELS] = preferences.enabledChannels
            prefs[KEY_QUIET_START] = preferences.quietStart
            prefs[KEY_QUIET_END] = preferences.quietEnd
        }
    }

    companion object {
        val KEY_CATEGORIES = stringSetPreferencesKey("notif_enabled_categories")
        val KEY_CHANNELS = stringSetPreferencesKey("notif_enabled_channels")
        val KEY_QUIET_START = stringPreferencesKey("notif_quiet_start")
        val KEY_QUIET_END = stringPreferencesKey("notif_quiet_end")
    }
}
