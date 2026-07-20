package com.crm.enhance_wellness.feature.notifications.domain.usecase

import com.crm.enhance_wellness.feature.notifications.domain.model.Notification
import com.crm.enhance_wellness.feature.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository,
) {
    operator fun invoke(): Flow<List<Notification>> = repository.getNotificationsAsFlow()
}
