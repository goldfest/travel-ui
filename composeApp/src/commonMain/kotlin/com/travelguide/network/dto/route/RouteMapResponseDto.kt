package com.travelguide.network.dto.route

import kotlinx.serialization.Serializable

@Serializable
data class RouteMapResponseDto(
    val routeId: Long,
    val routeName: String,
    val description: String? = null,
    val transportMode: String,
    val totalDistanceKm: Double? = null,
    val totalDurationMin: Int? = null,
    val viewport: MapViewportDto? = null,
    val days: List<RouteMapDayDto> = emptyList()
)

@Serializable
data class RouteMapDayDto(
    val routeDayId: Long,
    val dayNumber: Int,
    val polyline: RoutePolylineDto? = null,
    val points: List<RouteMapPointDto> = emptyList(),
    val segments: List<RouteSegmentDto> = emptyList()
)

@Serializable
data class RouteMapPointDto(
    val routePointId: Long,
    val poiId: Long,
    val orderIndex: Int,
    val poiName: String? = null,
    val poiAddress: String? = null,
    val poiType: String? = null,
    val latitude: Double,
    val longitude: Double,
    val markerType: String,
    val estimatedVisitMinutes: Int? = null,
    val plannedArrivalAt: String? = null,
    val plannedDepartureAt: String? = null
)

@Serializable
data class RoutePolylineDto(
    val source: String,
    val coordinates: List<LatLngDto> = emptyList()
)

@Serializable
data class LatLngDto(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class RouteSegmentDto(
    val fromRoutePointId: Long,
    val toRoutePointId: Long,
    val distanceKm: Double,
    val durationMin: Int,
    val transportMode: String,
    val polyline: RoutePolylineDto? = null,
    val provider: String? = null,
    val status: String? = null
)

@Serializable
data class MapViewportDto(
    val minLat: Double? = null,
    val minLng: Double? = null,
    val maxLat: Double? = null,
    val maxLng: Double? = null,
    val centerLat: Double? = null,
    val centerLng: Double? = null
)