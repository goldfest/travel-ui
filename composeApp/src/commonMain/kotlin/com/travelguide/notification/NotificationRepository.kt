package com.travelguide.notification

import com.travelguide.domain.models.Notification
import com.travelguide.network.dto.notification.NotificationResponseDto
import com.travelguide.network.dto.notification.NotificationStatsResponseDto
import com.travelguide.network.notification.NotificationApi

class NotificationRepository(
    private val api: NotificationApi
) {
    suspend fun getNotifications(page: Int = 0, size: Int = 50): List<Notification> {
        return api.getNotifications(page = page, size = size).content.map { it.toDomain() }
    }

    suspend fun getUnreadNotifications(page: Int = 0, size: Int = 50): List<Notification> {
        return api.getUnreadNotifications(page = page, size = size).content.map { it.toDomain() }
    }

    suspend fun getUnreadCount(): Int = api.getStats().unreadCount.toInt()

    suspend fun markAsRead(notificationId: Int): Notification {
        return api.markAsRead(notificationId.toLong()).toDomain()
    }

    suspend fun markAllAsRead() {
        api.markAllAsRead()
    }

    suspend fun deleteNotification(notificationId: Int) {
        api.deleteNotification(notificationId.toLong())
    }

    suspend fun deleteAllNotifications() {
        api.deleteAllNotifications()
    }
}

private fun NotificationResponseDto.toDomain(): Notification = Notification(
    id = id.toInt(),
    type = type,
    title = title,
    description = description.orEmpty(),
    scheduledAt = scheduledAt,
    isRead = isRead,
    sentAt = sentAt,
    readAt = readAt,
    routeId = routeId?.toInt(),
    routeDayId = routeDayId?.toInt(),
    poiId = poiId?.toInt(),
    userId = userId.toInt(),
    eventKey = eventKey,
    deliveryChannel = deliveryChannel,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)
