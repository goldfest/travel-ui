package com.travelguide.domain.models

data class Notification(
    val id: Int,
    val type: String,
    val title: String,
    val description: String,
    val scheduledAt: String? = null,
    val isRead: Boolean = false,
    val sentAt: String? = null,
    val readAt: String? = null,
    val routeId: Int? = null,
    val routeDayId: Int? = null,
    val poiId: Int? = null,
    val userId: Int,
    val eventKey: String? = null,
    val deliveryChannel: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun typeIcon(): String = when (type) {
        "route_created" -> "🗺️"
        "route_day_start" -> "🚶"
        "route_reminder" -> "⏰"
        "review" -> "📝"
        "poi_update" -> "🔄"
        "moderation" -> "✅"
        else -> "🔔"
    }

    fun typeLabel(): String = when (type) {
        "route_created" -> "Маршрут"
        "route_day_start" -> "Начало дня"
        "route_reminder" -> "Напоминание"
        "review" -> "Отзывы"
        "poi_update" -> "Обновление объекта"
        "moderation" -> "Модерация"
        else -> "Уведомление"
    }

    fun displayTime(): String = sentAt ?: scheduledAt ?: createdAt ?: ""
}

