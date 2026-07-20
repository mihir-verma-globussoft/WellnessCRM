package com.crm.enhance_wellness.feature.notifications.domain.usecase

import com.crm.enhance_wellness.feature.notifications.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationReadUseCase @Inject constructor(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke(notificationId: String) = repository.markRead(notificationId)
    suspend fun markAll() = repository.markAllRead()
}
