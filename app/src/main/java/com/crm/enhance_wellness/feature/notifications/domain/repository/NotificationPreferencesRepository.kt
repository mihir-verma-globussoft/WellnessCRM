package com.crm.enhance_wellness.feature.notifications.domain.repository

import com.crm.enhance_wellness.feature.notifications.domain.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow

interface NotificationPreferencesRepository {
    fun observe(): Flow<NotificationPreferences>
    suspend fun get(): NotificationPreferences
    suspend fun save(preferences: NotificationPreferences)
}
