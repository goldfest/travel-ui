package com.travelguide.network.dto.route

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val warnings: List<String> = emptyList()
)

@Serializable
data class RouteDayResponseDto(
    val id: Long,
    val dayNumber: Int,
    val description: String? = null,
    val routeId: Long,
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
    val startDate: String? = null,
    val status: String = "READY",
    val autoOptimize: Boolean = false,
    val optimizationMode: String? = null,
    val days: List<CreateRouteDayRequestDto>
)

@Serializable
data class CreateRouteDayRequestDto(
    val dayNumber: Int,
    val description: String? = null,
    val plannedStart: String? = null,
    val plannedEnd: String? = null,
    val points: List<CreateRoutePointRequestDto>
)

@Serializable
data class CreateRoutePointRequestDto(
    val poiId: Long,
    val orderIndex: Int,
    val estimatedVisitMinutes: Int = 60,
    val plannedArrival: String? = null,
    val plannedDeparture: String? = null
)

@Serializable
data class ReorderRouteDayPointsRequestDto(
    @SerialName("routePointIdsInOrder")
    val routePointIdsInOrder: List<Long>
)


@Serializable
data class RouteOptimizationRequestDto(
    val optimizationMode: String = "TIME_WINDOW",
    val dayStartTime: String? = null,
    val dayEndTime: String? = null,
    val maxTotalMinutesPerDay: Int? = null,
    val maxPointsPerDay: Int? = null,
    val maxTravelMinutesBetweenPoints: Int? = null,
    val allowDroppingPoints: Boolean = true,
    val keepFirstAndLast: Boolean = true,
    val orderedRoutePointIds: List<Long> = emptyList(),
    val considerOpeningHours: Boolean = false,
    val considerLunchBreak: Boolean = false
)

@Serializable
data class GenerateRouteRequestDto(
    val cityId: Long,
    val daysCount: Int = 1,
    val interests: List<String> = emptyList(),
    val transportMode: String = "WALK",
    val optimize: Boolean = true
)