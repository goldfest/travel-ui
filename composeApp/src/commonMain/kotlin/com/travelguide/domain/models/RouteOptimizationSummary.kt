package com.travelguide.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class RouteOptimizationSummary(
    val mode: String? = null,
    val scheduledPointsCount: Int = 0,
    val unscheduledPointsCount: Int = 0,
    val scheduledPointIds: List<Int> = emptyList(),
    val unscheduledPoints: List<RouteUnscheduledPoint> = emptyList()
)

@Serializable
data class RouteUnscheduledPoint(
    val routePointId: Int,
    val poiId: Int? = null,
    val poiName: String? = null,
    val routeDayId: Int? = null,
    val dayNumber: Int? = null,
    val routeDate: String? = null,
    val reasonCode: String? = null,
    val reason: String? = null,
    val currentVisitMinutes: Int? = null,
    val suggestions: List<RouteOptimizationSuggestion> = emptyList()
)

@Serializable
data class RouteOptimizationSuggestion(
    val type: String? = null,
    val title: String? = null,
    val description: String? = null,
    val routeDayId: Int? = null,
    val dayNumber: Int? = null,
    val routeDate: String? = null,
    val availableMinutes: Int? = null,
    val routePointId: Int? = null,
    val recommendedVisitMinutes: Int? = null
)