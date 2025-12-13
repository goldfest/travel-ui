package com.travelguide.domain.models

data class Route(
    val id: Int,
    val name: String,
    val description: String? = null,
    val coverPhotoUrl: String? = null,
    val transportMode: String = "WALK", // WALK, PUBLIC_TRANSPORT, CAR, MIXED
    val isOptimized: Boolean = false,
    val optimizationMode: String? = null,
    val distanceKm: Float? = null,
    val durationMin: Int? = null,
    val startPoint: String? = null,
    val endPoint: String? = null,
    val isArchived: Boolean = false,
    val userId: Int,
    val cityId: Int,
    val points: List<RoutePoint> = emptyList(),
    val days: List<RouteDay> = emptyList()
) {
    fun transportModeText(): String = when (transportMode) {
        "WALK" -> "Пешком"
        "PUBLIC_TRANSPORT" -> "Общественный транспорт"
        "CAR" -> "На машине"
        "MIXED" -> "Смешанный"
        else -> "Неизвестно"
    }
}

data class RoutePoint(
    val id: Int,
    val orderIndex: Int,
    val poiId: Int,
    val poi: POI? = null,
    val routeDayId: Int? = null
)

data class RouteDay(
    val id: Int,
    val dayNumber: Int,
    val description: String? = null,
    val routeId: Int,
    val points: List<RoutePoint> = emptyList()
)