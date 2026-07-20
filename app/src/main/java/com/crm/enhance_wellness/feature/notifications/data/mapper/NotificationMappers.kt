package com.crm.enhance_wellness.feature.notifications.data.mapper

import com.crm.enhance_wellness.feature.notifications.data.local.entity.CachedNotification
import com.crm.enhance_wellness.feature.notifications.data.remote.dto.PortalNotificationDto
import com.crm.enhance_wellness.feature.notifications.domain.model.Notification
import java.time.Instant

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

fun PortalNotificationDto.toDomain() = Notification(
    id = id.toString(),
    type = type,
    title = title,
    body = body ?: "",
    screen = screen,
    entityId = entityId?.toString(),
    isRead = isRead,
    receivedAt = runCatching { Instant.parse(receivedAt).toEpochMilli() }.getOrDefault(0L),
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
