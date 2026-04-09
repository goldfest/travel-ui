package com.travelguide.network.dto.route

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RoutePageResponseDto(
    val content: List<RouteResponseDto> = emptyList(),
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 0,
    val number: Int = 0
)

@Serializable
data class RouteResponseDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val coverPhotoUrl: String? = null,
    val transportMode: String,
    val status: String? = null,
    val isOptimized: Boolean = false,
    val optimizationMode: String? = null,
    val distanceKm: Double? = null,
    val durationMin: Int? = null,
    val startPoint: String? = null,
    val endPoint: String? = null,
    val userId: Long,
    val cityId: Long,
    val days: List<RouteDayResponseDto> = emptyList(),
    val points: List<RoutePointResponseDto> = emptyList(),
    val daysCount: Int? = null,
    val totalPoints: Int? = null,
    val warnings: List<String> = emptyList(),
    val additionalProperties: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class RouteDayResponseDto(
    val id: Long,
    val dayNumber: Int,
    val description: String? = null,
    val routeId: Long,
    val routeDate: String? = null,
    val plannedStart: String? = null,
    val plannedEnd: String? = null,
    val points: List<RoutePointResponseDto> = emptyList()
)

@Serializable
data class RoutePointResponseDto(
    val id: Long,
    val orderIndex: Int,
    val poiId: Long,
    val poiName: String? = null,
    val poiAddress: String? = null,
    val poiLatitude: Double? = null,
    val poiLongitude: Double? = null,
    val poiType: String? = null,
    val estimatedVisitMinutes: Int = 60,
    val plannedArrivalAt: String? = null,
    val plannedDepartureAt: String? = null
)

@Serializable
data class CreateRouteRequestDto(
    val name: String,
    val description: String? = null,
    val cityId: Long,
    val transportMode: String,
    val status: String? = null,
    val autoOptimize: Boolean = false,
    val optimizationMode: String? = null,
    val days: List<CreateRouteDayRequestDto> = emptyList()
)

@Serializable
data class CreateRouteDayRequestDto(
    val dayNumber: Int,
    val description: String? = null,
    val points: List<CreateRoutePointRequestDto> = emptyList()
)

@Serializable
data class CreateRoutePointRequestDto(
    val poiId: Long,
    val orderIndex: Int,
    val estimatedVisitMinutes: Int? = null
)

@Serializable
data class ReorderRouteDayPointsRequestDto(
    val routePointIdsInOrder: List<Long>
)

@Serializable
data class GenerateRouteRequestDto(
    val cityId: Long,
    val interests: List<String> = emptyList(),
    val daysCount: Int = 1,
    val transportMode: String = "WALK",
    val optimize: Boolean = true
)

@Serializable
data class RouteOptimizationDayRequestDto(
    val routeDayId: Long,
    val routeDate: String? = null,
    val dayStartTime: String? = null,
    val dayEndTime: String? = null
)

@Serializable
data class RouteOptimizationRequestDto(
    val optimizationMode: String = "TIME_WINDOW",
    val daySettings: List<RouteOptimizationDayRequestDto> = emptyList(),
    val visitMinutesByRoutePointId: Map<Long, Int> = emptyMap()
)

@Serializable
data class RouteOptimizationSummaryDto(
    val mode: String? = null,
    val scheduledPointsCount: Int = 0,
    val unscheduledPointsCount: Int = 0,
    val scheduledPointIds: List<Long> = emptyList(),
    val unscheduledPoints: List<RouteUnscheduledPointDto> = emptyList()
)

@Serializable
data class RouteUnscheduledPointDto(
    val routePointId: Long,
    val poiId: Long? = null,
    val poiName: String? = null,
    val routeDayId: Long? = null,
    val dayNumber: Int? = null,
    val routeDate: String? = null,
    val reasonCode: String? = null,
    val reason: String? = null
)

@Serializable
data class RouteMapDayResponseDto(
    val routeDayId: Long,
    val dayNumber: Int,
    val polyline: RoutePolylineResponseDto? = null,
    val points: List<RouteMapPointResponseDto> = emptyList(),
    val segments: List<RouteSegmentResponseDto> = emptyList()
)

@Serializable
data class RoutePolylineResponseDto(
    val source: String? = null,
    val coordinates: List<LatLngDto> = emptyList()
)

@Serializable
data class RouteMapPointResponseDto(
    val routePointId: Long,
    val poiId: Long,
    val orderIndex: Int,
    val poiName: String? = null,
    val poiAddress: String? = null,
    val poiType: String? = null,
    val latitude: Double,
    val longitude: Double,
    val markerType: String? = null,
    val estimatedVisitMinutes: Int? = null
)

@Serializable
data class RouteSegmentResponseDto(
    val fromRoutePointId: Long,
    val toRoutePointId: Long,
    val distanceKm: Double? = null,
    val durationMin: Int? = null,
    val transportMode: String? = null,
    val provider: String? = null,
    val status: String? = null,
    val polyline: RoutePolylineResponseDto? = null
)

@Serializable
data class CityGraphStatusDto(
    val cityId: Long? = null,
    val ready: Boolean = false,
    val activeVersionId: Long? = null,
    val status: String? = null
)