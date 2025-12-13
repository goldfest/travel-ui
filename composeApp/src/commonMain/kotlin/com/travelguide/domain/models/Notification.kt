package com.travelguide.domain.models

data class Notification(
    val id: Int,
    val type: String, // review, poi_update, route_reminder, moderation
    val title: String,
    val description: String,
    val isRead: Boolean = false,
    val sentAt: String = "",
    val readAt: String? = null,
    val routeId: Int? = null,
    val poiId: Int? = null,
    val userId: Int
) {
    fun typeIcon(): String = when (type) {
        "review" -> "📝"
        "poi_update" -> "🔄"
        "route_reminder" -> "⏰"
        "moderation" -> "✅"
        else -> "🔔"
    }
}