package com.travelguide.route.offline

import com.travelguide.domain.models.LatLng
import kotlinx.serialization.Serializable

@Serializable
data class RouteOfflineGraphSnapshot(
    val routeId: Int,
    val routeName: String,
    val days: List<RouteOfflineGraphDaySnapshot> = emptyList()
)

@Serializable
data class RouteOfflineGraphDaySnapshot(
    val routeDayId: Int,
    val dayNumber: Int,
    val segments: List<RouteOfflineGraphSegmentSnapshot> = emptyList()
)

@Serializable
data class RouteOfflineGraphSegmentSnapshot(
    val fromRoutePointId: Int,
    val toRoutePointId: Int,
    val distanceKm: Double,
    val durationMin: Int,
    val transportMode: String,
    val provider: String? = null,
    val status: String? = null,
    val coordinates: List<LatLng> = emptyList()
)
