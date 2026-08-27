package com.crm.enhance_wellness.feature.notifications.domain.usecase

import com.crm.enhance_wellness.feature.notifications.domain.model.NotificationPreferences
import com.crm.enhance_wellness.feature.notifications.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationPreferencesUseCase @Inject constructor(
    private val repository: NotificationPreferencesRepository,
) {
    operator fun invoke(): Flow<NotificationPreferences> = repository.observe()
}
