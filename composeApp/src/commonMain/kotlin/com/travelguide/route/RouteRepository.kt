package com.travelguide.route

import com.travelguide.domain.models.LatLng
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteDay
import com.travelguide.domain.models.RouteMap
import com.travelguide.domain.models.RouteMapDay
import com.travelguide.domain.models.RouteMapPoint
import com.travelguide.domain.models.RoutePoint
import com.travelguide.domain.models.RoutePolyline
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
import com.travelguide.route.offline.AddRoutePointSyncPayload
import com.travelguide.route.offline.CreateRouteSyncPayload
import com.travelguide.route.offline.OptimizeRouteSyncPayload
import com.travelguide.route.offline.PendingRouteSyncOperation
import com.travelguide.route.offline.RemoveRoutePointSyncPayload
import com.travelguide.route.offline.ReorderRouteDaySyncPayload
import com.travelguide.route.offline.RouteLocalStore
import com.travelguide.route.offline.RouteSyncOperationType
import com.travelguide.route.offline.UpdateRouteMetaSyncPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RouteRepository(
    private val api: RouteApi,
    private val poiRepository: PoiRepository,
    private val localStore: RouteLocalStore? = null,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) {
    private var nextLocalRouteId = -1
    private var nextLocalDayId = -1
    private var nextLocalPointId = -1

    suspend fun getRoutes(archived: Boolean = false): List<Route> {
        return runCatching {
            val page = if (archived) api.getArchivedRoutes() else api.getRoutes()
            val routes = page.content.map { it.toDomain() }
            localStore?.saveRoutes(routes)
            routes
        }.getOrElse { error ->
            val cached = localStore?.getRoutes(archived).orEmpty()
            if (cached.isNotEmpty()) cached else throw error
        }
    }

    suspend fun getRoutesByCity(cityId: Int): List<Route> {
        return runCatching {
            api.getRoutesByCity(cityId.toLong()).map { it.toDomain() }
        }.getOrElse { error ->
            val cached = localStore?.getRoutes(archived = false)
                ?.filter { it.cityId == cityId }
                .orEmpty()
            if (cached.isNotEmpty()) cached else throw error
        }
    }

    suspend fun getRouteById(id: Int): Route {
        return runCatching {
            api.getRouteById(id.toLong()).toDomain().also { localStore?.saveRoute(it) }
        }.getOrElse { error ->
            localStore?.getRoute(id) ?: throw error
        }
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

        return runCatching {
            api.createRoute(request).toDomain().also { route ->
                localStore?.saveRoute(route)
                localStore?.saveRouteMap(route.toFallbackMap())
            }
        }.getOrElse { error ->
            val store = localStore ?: throw error
            val localRoute = buildLocalRoute(
                routeId = nextLocalRouteId--,
                cityId = cityId,
                name = name,
                description = description,
                transportMode = transportMode,
                days = days,
                status = status,
                autoOptimize = autoOptimize
            )
            store.saveRoute(localRoute)
            store.saveRouteMap(localRoute.toFallbackMap())
            store.replacePendingCreate(
                PendingRouteSyncOperation(
                    routeId = localRoute.id,
                    operationType = RouteSyncOperationType.CREATE_ROUTE,
                    payloadJson = json.encodeToString(
                        CreateRouteSyncPayload.fromEditor(
                            localRouteId = localRoute.id,
                            cityId = cityId,
                            name = name,
                            description = description,
                            transportMode = transportMode,
                            status = status,
                            autoOptimize = autoOptimize,
                            optimizationMode = if (autoOptimize) "distance" else null,
                            days = days
                        )
                    )
                )
            )
            localRoute
        }
    }

    suspend fun updateRouteMeta(
        routeId: Int,
        name: String,
        description: String?,
        transportMode: TransportMode
    ): Route {
        return runCatching {
            api.updateRoute(
                routeId = routeId.toLong(),
                request = UpdateRouteRequestDto(
                    name = name,
                    description = description,
                    transportMode = transportMode.name
                )
            ).toDomain().also {
                localStore?.saveRoute(it)
                localStore?.saveRouteMap(it.toFallbackMap())
            }
        }.getOrElse { error ->
            val store = localStore ?: throw error
            val updated = (store.getRoute(routeId) ?: throw error).copy(
                name = name,
                description = description,
                transportMode = transportMode
            )
            saveOfflineMutation(
                updatedRoute = updated,
                operation = PendingRouteSyncOperation(
                    routeId = routeId,
                    operationType = RouteSyncOperationType.UPDATE_ROUTE_META,
                    payloadJson = json.encodeToString(
                        UpdateRouteMetaSyncPayload(routeId, name, description, transportMode)
                    )
                )
            )
            updated
        }
    }

    suspend fun addPoiToRoute(
        routeId: Int,
        poiId: Int,
        dayNumber: Int,
        orderIndex: Int? = null
    ): Route {
        return runCatching {
            api.addPointToRoute(
                routeId = routeId.toLong(),
                poiId = poiId.toLong(),
                dayNumber = dayNumber,
                orderIndex = orderIndex
            ).toDomain().also {
                localStore?.saveRoute(it)
                localStore?.saveRouteMap(it.toFallbackMap())
            }
        }.getOrElse { error ->
            val store = localStore ?: throw error
            val poi = runCatching { poiRepository.getPoiById(poiId) }.getOrNull()
                ?: store.findPoiById(poiId)
                ?: throw error
            val updated = (store.getRoute(routeId) ?: throw error).addPointToDay(
                poi = poi,
                dayNumber = dayNumber,
                orderIndex = orderIndex,
                nextPointId = nextLocalPointId--
            )
            saveOfflineMutation(
                updatedRoute = updated,
                operation = PendingRouteSyncOperation(
                    routeId = routeId,
                    operationType = RouteSyncOperationType.ADD_POINT,
                    payloadJson = json.encodeToString(
                        AddRoutePointSyncPayload(routeId, poiId, dayNumber, orderIndex)
                    )
                )
            )
            updated
        }
    }

    suspend fun removePointFromRoute(
        routeId: Int,
        routePointId: Int
    ): Route {
        return runCatching {
            api.removePointFromRoute(
                routeId = routeId.toLong(),
                routePointId = routePointId.toLong()
            ).toDomain().also {
                localStore?.saveRoute(it)
                localStore?.saveRouteMap(it.toFallbackMap())
            }
        }.getOrElse { error ->
            val store = localStore ?: throw error
            val updated = (store.getRoute(routeId) ?: throw error).removePoint(routePointId)
            saveOfflineMutation(
                updatedRoute = updated,
                operation = PendingRouteSyncOperation(
                    routeId = routeId,
                    operationType = RouteSyncOperationType.REMOVE_POINT,
                    payloadJson = json.encodeToString(
                        RemoveRoutePointSyncPayload(routeId, routePointId)
                    )
                )
            )
            updated
        }
    }

    suspend fun reorderDayPoints(
        routeId: Int,
        dayId: Int,
        orderedPointIds: List<Int>
    ): Route {
        return runCatching {
            api.reorderDayPoints(
                routeId = routeId.toLong(),
                dayId = dayId.toLong(),
                routePointIdsInOrder = orderedPointIds.map { it.toLong() }
            ).toDomain().also {
                localStore?.saveRoute(it)
                localStore?.saveRouteMap(it.toFallbackMap())
            }
        }.getOrElse { error ->
            val store = localStore ?: throw error
            val updated = (store.getRoute(routeId) ?: throw error).reorderDay(dayId, orderedPointIds)
            saveOfflineMutation(
                updatedRoute = updated,
                operation = PendingRouteSyncOperation(
                    routeId = routeId,
                    operationType = RouteSyncOperationType.REORDER_DAY,
                    payloadJson = json.encodeToString(
                        ReorderRouteDaySyncPayload(routeId, dayId, orderedPointIds)
                    )
                )
            )
            updated
        }
    }

    suspend fun optimizeRoute(routeId: Int, mode: String = "distance"): Route {
        return runCatching {
            api.optimizeRoute(routeId.toLong(), mode).toDomain().also {
                localStore?.saveRoute(it)
                localStore?.saveRouteMap(it.toFallbackMap())
            }
        }.getOrElse { error ->
            val store = localStore ?: throw error
            val updated = (store.getRoute(routeId) ?: throw error).copy(
                isOptimized = true,
                optimizationMode = mode
            )
            saveOfflineMutation(
                updatedRoute = updated,
                operation = PendingRouteSyncOperation(
                    routeId = routeId,
                    operationType = RouteSyncOperationType.OPTIMIZE_ROUTE,
                    payloadJson = json.encodeToString(OptimizeRouteSyncPayload(routeId, mode))
                )
            )
            updated
        }
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
        return api.generateRoute(request).toDomain().also {
            localStore?.saveRoute(it)
            localStore?.saveRouteMap(it.toFallbackMap())
        }
    }

    suspend fun getPoisForRouteCreation(cityId: Int): List<POI> {
        return runCatching {
            poiRepository.getPoisByCity(cityId).also { localStore?.savePoisByCity(cityId, it) }
        }.getOrElse { error ->
            val cached = localStore?.getPoisByCity(cityId).orEmpty()
            if (cached.isNotEmpty()) cached else throw error
        }
    }

    suspend fun getPoiById(id: Int): POI {
        return runCatching {
            poiRepository.getPoiById(id)
        }.getOrElse { error ->
            localStore?.findPoiById(id) ?: throw error
        }
    }

    suspend fun getRouteMap(routeId: Int): RouteMap {
        return runCatching {
            api.getRouteMap(routeId.toLong()).toDomain().also { localStore?.saveRouteMap(it) }
        }.getOrElse { error ->
            localStore?.getRouteMap(routeId)
                ?: localStore?.getRoute(routeId)?.toFallbackMap()
                ?: throw error
        }
    }

    private suspend fun saveOfflineMutation(
        updatedRoute: Route,
        operation: PendingRouteSyncOperation
    ) {
        val store = localStore ?: return
        store.saveRoute(updatedRoute)
        store.saveRouteMap(updatedRoute.toFallbackMap())

        if (updatedRoute.id < 0) {
            store.replacePendingCreate(
                PendingRouteSyncOperation(
                    routeId = updatedRoute.id,
                    operationType = RouteSyncOperationType.CREATE_ROUTE,
                    payloadJson = json.encodeToString(CreateRouteSyncPayload.fromRoute(updatedRoute))
                )
            )
        } else {
            store.enqueue(operation)
        }
    }

    private fun buildLocalRoute(
        routeId: Int,
        cityId: Int,
        name: String,
        description: String?,
        transportMode: TransportMode,
        days: List<EditableRouteDayUi>,
        status: RouteStatus,
        autoOptimize: Boolean
    ): Route {
        val routeDays = days
            .filter { it.points.isNotEmpty() }
            .map { day ->
                val dayId = nextLocalDayId--
                RouteDay(
                    id = dayId,
                    dayNumber = day.dayNumber,
                    description = day.description.takeIf { it.isNotBlank() },
                    routeId = routeId,
                    points = day.points.mapIndexed { index, point ->
                        RoutePoint(
                            id = nextLocalPointId--,
                            orderIndex = index + 1,
                            poiId = point.poi.id,
                            poi = point.poi,
                            estimatedVisitMinutes = point.estimatedVisitMinutes
                        )
                    }
                )
            }

        return Route(
            id = routeId,
            name = name,
            description = description,
            transportMode = transportMode,
            status = status,
            isOptimized = autoOptimize,
            optimizationMode = if (autoOptimize) "distance" else null,
            userId = 0,
            cityId = cityId,
            points = routeDays.flatMap { it.points },
            days = routeDays
        )
    }
}

private fun RouteResponseDto.toDomain(): Route {
    val domainDays = days.map { it.toDomain() }
    val flatPoints = if (points.isNotEmpty()) points.map { it.toDomain() } else domainDays.flatMap { it.points }

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

private fun RouteDayResponseDto.toDomain(): RouteDay =
    RouteDay(
        id = id.toInt(),
        dayNumber = dayNumber,
        description = description,
        routeId = routeId.toInt(),
        plannedStart = plannedStart,
        plannedEnd = plannedEnd,
        points = points.map { it.toDomain() }
    )

private fun RoutePointResponseDto.toDomain(): RoutePoint =
    RoutePoint(
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

private fun String.toTransportMode(): TransportMode =
    runCatching { TransportMode.valueOf(this) }.getOrDefault(TransportMode.WALK)

private fun String?.toRouteStatus(): RouteStatus =
    runCatching { RouteStatus.valueOf(this ?: "READY") }.getOrDefault(RouteStatus.READY)

private fun Route.toFallbackMap(): RouteMap =
    RouteMap(
        routeId = id,
        routeName = name,
        description = description,
        transportMode = transportMode.name,
        totalDistanceKm = distanceKm,
        totalDurationMin = durationMin,
        days = days.sortedBy { it.dayNumber }.map { day ->
            val routePoints = day.points.sortedBy { it.orderIndex }
            RouteMapDay(
                routeDayId = day.id,
                dayNumber = day.dayNumber,
                polyline = if (routePoints.size >= 2) {
                    RoutePolyline(
                        source = "OFFLINE_CACHE",
                        coordinates = routePoints.mapNotNull { point ->
                            val lat = point.poiLatitude ?: point.poi?.latitude
                            val lng = point.poiLongitude ?: point.poi?.longitude
                            if (lat != null && lng != null) LatLng(lat, lng) else null
                        }
                    )
                } else null,
                points = routePoints.mapIndexed { index, point ->
                    RouteMapPoint(
                        routePointId = point.id,
                        poiId = point.poiId,
                        orderIndex = point.orderIndex,
                        poiName = point.poiName ?: point.poi?.name,
                        poiAddress = point.poiAddress ?: point.poi?.address,
                        poiType = point.poiType ?: point.poi?.poiType?.name,
                        latitude = point.poiLatitude ?: point.poi?.latitude ?: 0.0,
                        longitude = point.poiLongitude ?: point.poi?.longitude ?: 0.0,
                        markerType = when (index) {
                            0 -> "START"
                            routePoints.lastIndex -> "END"
                            else -> "WAYPOINT"
                        },
                        estimatedVisitMinutes = point.estimatedVisitMinutes
                    )
                }
            )
        }
    )

private fun Route.addPointToDay(
    poi: POI,
    dayNumber: Int,
    orderIndex: Int?,
    nextPointId: Int
): Route {
    val updatedDays = days.map { day ->
        if (day.dayNumber != dayNumber) return@map day
        val mutable = day.points.sortedBy { it.orderIndex }.toMutableList()
        val insertIndex = (orderIndex?.minus(1) ?: mutable.size).coerceIn(0, mutable.size)
        mutable.add(
            insertIndex,
            RoutePoint(
                id = nextPointId,
                orderIndex = insertIndex + 1,
                poiId = poi.id,
                poi = poi
            )
        )
        day.copy(points = mutable.mapIndexed { idx, point -> point.copy(orderIndex = idx + 1) })
    }
    return copy(days = updatedDays, points = updatedDays.flatMap { it.points })
}

private fun Route.removePoint(routePointId: Int): Route {
    val updatedDays = days.map { day ->
        val filtered = day.points.filterNot { it.id == routePointId }
            .mapIndexed { index, point -> point.copy(orderIndex = index + 1) }
        day.copy(points = filtered)
    }
    return copy(days = updatedDays, points = updatedDays.flatMap { it.points })
}

private fun Route.reorderDay(dayId: Int, orderedPointIds: List<Int>): Route {
    val updatedDays = days.map { day ->
        if (day.id != dayId) return@map day
        val pointMap = day.points.associateBy { it.id }
        val reordered = orderedPointIds.mapNotNull { pointMap[it] }
            .mapIndexed { index, point -> point.copy(orderIndex = index + 1) }
        day.copy(points = reordered)
    }
    return copy(days = updatedDays, points = updatedDays.flatMap { it.points })
}
