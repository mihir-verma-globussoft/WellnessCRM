package com.globussoft.wellness.patient.feature.notifications.presentation.state

import com.globussoft.wellness.patient.feature.notifications.domain.model.Notification

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = true,
)

sealed class NotificationsUiEvent {
    data class MarkRead(val notificationId: String) : NotificationsUiEvent()
    object MarkAllRead : NotificationsUiEvent()
    data class TapNotification(val notification: Notification) : NotificationsUiEvent()
    object NavigateBack : NotificationsUiEvent()
}
