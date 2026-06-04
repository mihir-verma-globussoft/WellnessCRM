package com.globussoft.wellness.patient.feature.notifications.domain.repository

import com.globussoft.wellness.patient.feature.notifications.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationsAsFlow(): Flow<List<Notification>>
    suspend fun markRead(notificationId: String)
    suspend fun markAllRead()
    suspend fun deleteOldNotifications()
    suspend fun insert(notification: Notification)
}
