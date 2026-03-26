package com.travelguide.domain.models

data class RoutePoint(
    val id: Int,
    val orderIndex: Int,
    val poiId: Int,
    val poi: POI? = null,
    val poiName: String? = poi?.name,
    val poiAddress: String? = poi?.address,
    val poiLatitude: Double? = poi?.latitude,
    val poiLongitude: Double? = poi?.longitude,
    val poiType: String? = poi?.poiType?.name,
    val estimatedVisitMinutes: Int = 60,
    val plannedArrivalAt: String? = null,
    val plannedDepartureAt: String? = null
)