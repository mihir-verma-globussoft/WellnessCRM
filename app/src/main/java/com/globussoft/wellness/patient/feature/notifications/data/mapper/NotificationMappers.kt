package com.globussoft.wellness.patient.feature.notifications.data.mapper

import com.globussoft.wellness.patient.feature.notifications.data.local.entity.CachedNotification
import com.globussoft.wellness.patient.feature.notifications.domain.model.Notification

fun CachedNotification.toDomain() = Notification(
    id = id,
    type = type,
    title = title,
    body = body,
    screen = screen,
    entityId = entityId,
    isRead = isRead,
    receivedAt = receivedAt,
)

fun Notification.toEntity() = CachedNotification(
    id = id,
    type = type,
    title = title,
    body = body,
    screen = screen,
    entityId = entityId,
    isRead = isRead,
    receivedAt = receivedAt,
)
