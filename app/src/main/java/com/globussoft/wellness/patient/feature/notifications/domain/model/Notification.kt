package com.globussoft.wellness.patient.feature.notifications.domain.model

data class Notification(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val screen: String?,
    val entityId: String?,
    val isRead: Boolean,
    val receivedAt: Long,
)
