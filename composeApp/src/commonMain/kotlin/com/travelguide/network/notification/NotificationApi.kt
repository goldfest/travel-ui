package com.travelguide.network.notification

import com.travelguide.network.dto.common.PageResponseDto
import com.travelguide.network.dto.notification.NotificationResponseDto
import com.travelguide.network.dto.notification.NotificationStatsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.parameter

class NotificationApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getNotifications(
        page: Int = 0,
        size: Int = 50,
        sort: String = "createdAt,desc"
    ): PageResponseDto<NotificationResponseDto> {
        return client.get("$baseUrl/v1/notifications") {
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
        }.body()
    }

    suspend fun getUnreadNotifications(page: Int = 0, size: Int = 50): PageResponseDto<NotificationResponseDto> {
        return client.get("$baseUrl/v1/notifications/filter") {
            parameter("page", page)
            parameter("size", size)
            parameter("isRead", false)
        }.body()
    }

    suspend fun getStats(): NotificationStatsResponseDto {
        return client.get("$baseUrl/v1/notifications/stats").body()
    }

    suspend fun markAsRead(notificationId: Long): NotificationResponseDto {
        return client.patch("$baseUrl/v1/notifications/$notificationId/read").body()
    }

    suspend fun markAllAsRead() {
        client.patch("$baseUrl/v1/notifications/read-all").body<Unit>()
    }

    suspend fun deleteNotification(notificationId: Long) {
        client.delete("$baseUrl/v1/notifications/$notificationId").body<Unit>()
    }

    suspend fun deleteAllNotifications() {
        client.delete("$baseUrl/v1/notifications").body<Unit>()
    }
}
