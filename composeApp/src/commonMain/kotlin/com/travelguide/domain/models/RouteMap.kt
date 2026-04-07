package com.travelguide.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class RouteMap(
    val routeId: Int,
    val routeName: String,
    val description: String? = null,
    val transportMode: String,
    val totalDistanceKm: Double? = null,
    val totalDurationMin: Int? = null,
    val viewport: MapViewport? = null,
    val days: List<RouteMapDay> = emptyList()
)

@Serializable
data class RouteMapDay(
    val routeDayId: Int,
    val dayNumber: Int,
    val polyline: RoutePolyline? = null,
    val points: List<RouteMapPoint> = emptyList(),
    val segments: List<RouteSegment> = emptyList()
)

@Serializable
data class RouteMapPoint(
    val routePointId: Int,
    val poiId: Int,
    val orderIndex: Int,
    val poiName: String? = null,
    val poiAddress: String? = null,
    val poiType: String? = null,
    val latitude: Double,
    val longitude: Double,
    val markerType: String,
    val estimatedVisitMinutes: Int? = null
)

@Serializable
data class RoutePolyline(
    val source: String,
    val coordinates: List<LatLng> = emptyList()
)

@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class RouteSegment(
    val fromRoutePointId: Int,
    val toRoutePointId: Int,
    val distanceKm: Double,
    val durationMin: Int,
    val transportMode: String,
    val polyline: RoutePolyline? = null,
    val provider: String? = null,
    val status: String? = null
)

@Serializable
data class MapViewport(
    val minLat: Double? = null,
    val minLng: Double? = null,
    val maxLat: Double? = null,
    val maxLng: Double? = null,
    val centerLat: Double? = null,
    val centerLng: Double? = null
)
