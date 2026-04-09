package com.travelguide.network.dto.notification

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponseDto(
    val id: Long,
    val type: String,
    val title: String,
    val description: String? = null,
    val scheduledAt: String? = null,
    val isRead: Boolean = false,
    val sentAt: String? = null,
    val readAt: String? = null,
    val routeId: Long? = null,
    val routeDayId: Long? = null,
    val poiId: Long? = null,
    val userId: Long,
    val eventKey: String? = null,
    val deliveryChannel: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class NotificationStatsResponseDto(
    val totalCount: Long = 0,
    val unreadCount: Long = 0,
    val routeRemindersCount: Long = 0,
    val reviewNotificationsCount: Long = 0,
    val moderationNotificationsCount: Long = 0,
    val poiUpdateNotificationsCount: Long = 0
)
