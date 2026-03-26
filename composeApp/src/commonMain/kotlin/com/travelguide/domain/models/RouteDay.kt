package com.travelguide.domain.models

data class RouteDay(
    val id: Int,
    val dayNumber: Int,
    val description: String? = null,
    val routeId: Int,
    val plannedStart: String? = null,
    val plannedEnd: String? = null,
    val points: List<RoutePoint> = emptyList()
)