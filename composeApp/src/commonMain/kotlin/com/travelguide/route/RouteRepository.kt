package com.travelguide.route

import com.travelguide.domain.models.POI
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteDay
import com.travelguide.domain.models.RouteMap
import com.travelguide.domain.models.RoutePoint
import com.travelguide.domain.models.RouteStatus
import com.travelguide.domain.models.TransportMode
import com.travelguide.network.dto.route.CreateRouteDayRequestDto
import com.travelguide.network.dto.route.CreateRoutePointRequestDto
import com.travelguide.network.dto.route.CreateRouteRequestDto
import com.travelguide.network.dto.route.GenerateRouteRequestDto
import com.travelguide.network.dto.route.RouteDayResponseDto
import com.travelguide.network.dto.route.RoutePointResponseDto
import com.travelguide.network.dto.route.RouteResponseDto
import com.travelguide.network.dto.route.UpdateRouteRequestDto
import com.travelguide.network.route.RouteApi
import com.travelguide.poi.PoiRepository

class RouteRepository(
    private val api: RouteApi,
    private val poiRepository: PoiRepository
) {
    suspend fun getRoutes(archived: Boolean = false): List<Route> {
        val page = if (archived) api.getArchivedRoutes() else api.getRoutes()
        return page.content.map { it.toDomain() }
    }

    suspend fun getRoutesByCity(cityId: Int): List<Route> {
        return api.getRoutesByCity(cityId.toLong()).map { it.toDomain() }
    }

    suspend fun getRouteById(id: Int): Route {
        return api.getRouteById(id.toLong()).toDomain()
    }

    suspend fun createRoute(
        cityId: Int,
        name: String,
        description: String?,
        transportMode: TransportMode,
        days: List<EditableRouteDayUi>,
        status: RouteStatus = RouteStatus.READY,
        autoOptimize: Boolean = false
    ): Route {
        val requestDays = days
            .filter { it.points.isNotEmpty() }
            .map { day ->
                CreateRouteDayRequestDto(
                    dayNumber = day.dayNumber,
                    description = day.description.ifBlank { null },
                    points = day.points.mapIndexed { index, point ->
                        CreateRoutePointRequestDto(
                            poiId = point.poi.id.toLong(),
                            orderIndex = index + 1,
                            estimatedVisitMinutes = point.estimatedVisitMinutes
                        )
                    }
                )
            }

        val request = CreateRouteRequestDto(
            name = name,
            description = description?.takeIf { it.isNotBlank() },
            cityId = cityId.toLong(),
            transportMode = transportMode.name,
            status = status.name,
            autoOptimize = autoOptimize,
            optimizationMode = if (autoOptimize) "distance" else null,
            days = requestDays
        )

        return api.createRoute(request).toDomain()
    }

    suspend fun updateRouteMeta(
        routeId: Int,
        name: String,
        description: String?,
        transportMode: TransportMode
    ): Route {
        return api.updateRoute(
            routeId = routeId.toLong(),
            request = UpdateRouteRequestDto(
                name = name,
                description = description,
                transportMode = transportMode.name
            )
        ).toDomain()
    }

    suspend fun addPoiToRoute(
        routeId: Int,
        poiId: Int,
        dayNumber: Int,
        orderIndex: Int? = null
    ): Route {
        return api.addPointToRoute(
            routeId = routeId.toLong(),
            poiId = poiId.toLong(),
            dayNumber = dayNumber,
            orderIndex = orderIndex
        ).toDomain()
    }

    suspend fun removePointFromRoute(
        routeId: Int,
        routePointId: Int
    ): Route {
        return api.removePointFromRoute(
            routeId = routeId.toLong(),
            routePointId = routePointId.toLong()
        ).toDomain()
    }

    suspend fun reorderDayPoints(
        routeId: Int,
        dayId: Int,
        orderedPointIds: List<Int>
    ): Route {
        return api.reorderDayPoints(
            routeId = routeId.toLong(),
            dayId = dayId.toLong(),
            routePointIdsInOrder = orderedPointIds.map { it.toLong() }
        ).toDomain()
    }

    suspend fun optimizeRoute(routeId: Int, mode: String = "distance"): Route {
        return api.optimizeRoute(routeId.toLong(), mode).toDomain()
    }

    suspend fun generateRouteByInterests(
        cityId: Int,
        interests: List<String>,
        daysCount: Int = 1,
        transportMode: TransportMode = TransportMode.WALK
    ): Route {
        val request = GenerateRouteRequestDto(
            cityId = cityId.toLong(),
            interests = interests,
            daysCount = daysCount,
            transportMode = transportMode.name,
            optimize = true
        )
        return api.generateRoute(request).toDomain()
    }

    suspend fun getPoisForRouteCreation(cityId: Int): List<POI> {
        return poiRepository.getPoisByCity(cityId)
    }

    suspend fun getPoiById(id: Int): POI {
        return poiRepository.getPoiById(id)
    }

    suspend fun getRouteMap(routeId: Int): RouteMap {
        return api.getRouteMap(routeId.toLong()).toDomain()
    }
}

private fun RouteResponseDto.toDomain(): Route {
    val domainDays = days.map { it.toDomain() }
    val flatPoints = if (points.isNotEmpty()) {
        points.map { it.toDomain() }
    } else {
        domainDays.flatMap { it.points }
    }

    return Route(
        id = id.toInt(),
        name = name,
        description = description,
        coverPhotoUrl = coverPhotoUrl,
        transportMode = transportMode.toTransportMode(),
        status = status.toRouteStatus(),
        isOptimized = isOptimized,
        optimizationMode = optimizationMode,
        distanceKm = distanceKm,
        durationMin = durationMin,
        startPoint = startPoint,
        endPoint = endPoint,
        userId = userId.toInt(),
        cityId = cityId.toInt(),
        points = flatPoints,
        days = domainDays,
        warnings = warnings
    )
}

private fun RouteDayResponseDto.toDomain(): RouteDay {
    return RouteDay(
        id = id.toInt(),
        dayNumber = dayNumber,
        description = description,
        routeId = routeId.toInt(),
        plannedStart = plannedStart,
        plannedEnd = plannedEnd,
        points = points.map { it.toDomain() }
    )
}

private fun RoutePointResponseDto.toDomain(): RoutePoint {
    return RoutePoint(
        id = id.toInt(),
        orderIndex = orderIndex,
        poiId = poiId.toInt(),
        poi = null,
        poiName = poiName,
        poiAddress = poiAddress,
        poiLatitude = poiLatitude,
        poiLongitude = poiLongitude,
        poiType = poiType,
        estimatedVisitMinutes = estimatedVisitMinutes,
        plannedArrivalAt = plannedArrivalAt,
        plannedDepartureAt = plannedDepartureAt
    )
}

private fun String.toTransportMode(): TransportMode {
    return runCatching { TransportMode.valueOf(this) }.getOrDefault(TransportMode.WALK)
}

private fun String?.toRouteStatus(): RouteStatus {
    return runCatching { RouteStatus.valueOf(this ?: "READY") }.getOrDefault(RouteStatus.READY)
}