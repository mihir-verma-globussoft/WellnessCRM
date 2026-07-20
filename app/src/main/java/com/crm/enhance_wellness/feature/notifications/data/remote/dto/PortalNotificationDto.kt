package com.crm.enhance_wellness.feature.notifications.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Live backend shape (GET /portal/me/notifications):
// { notifications:[{ id:Int, type, title, message, link, isRead, readAt, createdAt }],
//   unreadCount, count }
@JsonClass(generateAdapter = true)
data class PortalNotificationDto(
    val id: Int,
    val type: String = "info",
    val title: String = "",
    @Json(name = "message") val body: String? = null,
    @Json(name = "link") val screen: String? = null,
    val entityId: Int? = null,
    val isRead: Boolean = false,
    @Json(name = "createdAt") val receivedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class PortalNotificationsResponseDto(
    val notifications: List<PortalNotificationDto>,
    val unreadCount: Int = 0,
    val count: Int = 0,
)
