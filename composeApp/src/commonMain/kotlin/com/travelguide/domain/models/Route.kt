package com.travelguide.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val id: Int,
    val name: String,
    val description: String? = null,
    val coverPhotoUrl: String? = null,
    val transportMode: TransportMode,
    val status: RouteStatus = RouteStatus.DRAFT,
    val isOptimized: Boolean = false,
    val optimizationMode: String? = null,
    val distanceKm: Double? = null,
    val durationMin: Int? = null,
    val startPoint: String? = null,
    val endPoint: String? = null,
    val userId: Int,
    val cityId: Int,
    val points: List<RoutePoint> = emptyList(),
    val days: List<RouteDay> = emptyList(),
    val warnings: List<String> = emptyList(),
    val optimizationSummary: RouteOptimizationSummary? = null
) {
    fun transportModeText(): String = transportMode.label()
    val isArchived: Boolean get() = status == RouteStatus.ARCHIVED
}