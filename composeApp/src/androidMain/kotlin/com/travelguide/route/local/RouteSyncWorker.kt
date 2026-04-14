package com.travelguide.route.local

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.russhwolf.settings.SharedPreferencesSettings
import com.travelguide.auth.TokenStorage
import com.travelguide.core.NetworkConfig
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteDay
import com.travelguide.domain.models.RouteMap
import com.travelguide.domain.models.RouteMapDay
import com.travelguide.domain.models.RouteMapPoint
import com.travelguide.domain.models.RoutePoint
import com.travelguide.domain.models.RoutePolyline
import com.travelguide.domain.models.RouteStatus
import com.travelguide.domain.models.TransportMode
import com.travelguide.network.HttpClientFactory
import com.travelguide.network.dto.route.CreateRouteDayRequestDto
import com.travelguide.network.dto.route.CreateRoutePointRequestDto
import com.travelguide.network.dto.route.CreateRouteRequestDto
import com.travelguide.network.dto.route.RouteDayResponseDto
import com.travelguide.network.dto.route.RoutePointResponseDto
import com.travelguide.network.dto.route.RouteResponseDto
import com.travelguide.network.dto.route.UpdateRouteRequestDto
import com.travelguide.network.route.RouteApi
import com.travelguide.route.toDomain as toDomainRouteMap
import com.travelguide.route.offline.AddRoutePointSyncPayload
import com.travelguide.route.offline.CreateRouteSyncPayload
import com.travelguide.route.offline.OptimizeRouteSyncPayload
import com.travelguide.route.offline.RemoveRoutePointSyncPayload
import com.travelguide.route.offline.ReorderRouteDaySyncPayload
import com.travelguide.route.offline.RouteOfflineGraphSnapshot
import com.travelguide.route.offline.RouteSyncOperationType
import com.travelguide.route.offline.UpdateRouteMetaSyncPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RouteSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val dao = RouteAppDatabase.getInstance(appContext).routeCacheDao()
    private val store = RouteLocalStoreImpl(dao)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val settings = SharedPreferencesSettings(
        appContext.getSharedPreferences("travelguide_settings", Context.MODE_PRIVATE)
    )
    private val tokenStorage = TokenStorage(settings)
    private val routeApi = RouteApi(
        HttpClientFactory().create(NetworkConfig.AUTH_API, tokenStorage),
        NetworkConfig.ROUTE_API
    )

    override suspend fun doWork(): Result {
        val ops = dao.getPendingSyncOperations()
        return runCatching {
            ops.forEach { op ->
                when (RouteSyncOperationType.valueOf(op.operationType)) {
                    RouteSyncOperationType.CREATE_ROUTE -> syncCreate(op)
                    RouteSyncOperationType.UPDATE_ROUTE_META -> syncUpdateMeta(op)
                    RouteSyncOperationType.ADD_POINT -> syncAddPoint(op)
                    RouteSyncOperationType.REMOVE_POINT -> syncRemovePoint(op)
                    RouteSyncOperationType.REORDER_DAY -> syncReorder(op)
                    RouteSyncOperationType.OPTIMIZE_ROUTE -> syncOptimize(op)
                }
                dao.deleteSyncOperation(op.id)
            }

            val fresh = routeApi.getRoutes(page = 0, size = 100).content.map { it.toDomain() }
            val archived = routeApi.getArchivedRoutes(page = 0, size = 100).content.map { it.toDomain() }
            store.saveRoutes(fresh + archived)

            dao.getOfflineDownloads().forEach { offline ->
                val route = (fresh + archived).firstOrNull { it.id == offline.routeId } ?: return@forEach
                refreshOfflineArtifacts(route)
            }

            Result.success()
        }.getOrElse {
            ops.firstOrNull()?.let { first -> dao.incrementAttempts(first.id) }
            Result.retry()
        }
    }

    private suspend fun syncCreate(op: RouteSyncQueueEntity) {
        val payload = json.decodeFromString(CreateRouteSyncPayload.serializer(), op.payloadJson)
        val response = routeApi.createRoute(
            CreateRouteRequestDto(
                name = payload.name,
                description = payload.description,
                cityId = payload.cityId.toLong(),
                transportMode = payload.transportMode.name,
                status = payload.status.name,
                autoOptimize = payload.autoOptimize,
                optimizationMode = payload.optimizationMode,
                days = payload.days.map { day ->
                    CreateRouteDayRequestDto(
                        dayNumber = day.dayNumber,
                        description = day.description,
                        points = day.points.map { point ->
                            CreateRoutePointRequestDto(
                                poiId = point.poiId.toLong(),
                                orderIndex = point.orderIndex,
                                estimatedVisitMinutes = point.estimatedVisitMinutes
                            )
                        }
                    )
                }
            )
        ).toDomain()

        store.deleteRoute(payload.localRouteId)
        store.deleteRouteMap(payload.localRouteId)
        store.saveRoute(response)
        store.saveRouteMap(fetchFreshMap(response))
        dao.rebindQueuedRouteId(payload.localRouteId, response.id)
    }

    private suspend fun syncUpdateMeta(op: RouteSyncQueueEntity) {
        val payload = json.decodeFromString(UpdateRouteMetaSyncPayload.serializer(), op.payloadJson)
        val response = routeApi.updateRoute(
            routeId = payload.routeId.toLong(),
            request = UpdateRouteRequestDto(
                name = payload.name,
                description = payload.description,
                transportMode = payload.transportMode.name
            )
        ).toDomain()
        store.saveRoute(response)
        store.saveRouteMap(fetchFreshMap(response))
    }

    private suspend fun syncAddPoint(op: RouteSyncQueueEntity) {
        val payload = json.decodeFromString(AddRoutePointSyncPayload.serializer(), op.payloadJson)
        val response = routeApi.addPointToRoute(
            routeId = payload.routeId.toLong(),
            poiId = payload.poiId.toLong(),
            dayNumber = payload.dayNumber,
            orderIndex = payload.orderIndex
        ).toDomain()
        store.saveRoute(response)
        store.saveRouteMap(fetchFreshMap(response))
    }

    private suspend fun syncRemovePoint(op: RouteSyncQueueEntity) {
        val payload = json.decodeFromString(RemoveRoutePointSyncPayload.serializer(), op.payloadJson)
        val response = routeApi.removePointFromRoute(
            routeId = payload.routeId.toLong(),
            routePointId = payload.routePointId.toLong()
        ).toDomain()
        store.saveRoute(response)
        store.saveRouteMap(fetchFreshMap(response))
    }

    private suspend fun syncReorder(op: RouteSyncQueueEntity) {
        val payload = json.decodeFromString(ReorderRouteDaySyncPayload.serializer(), op.payloadJson)
        val response = routeApi.reorderDayPoints(
            routeId = payload.routeId.toLong(),
            dayId = payload.dayId.toLong(),
            routePointIdsInOrder = payload.orderedPointIds.map { it.toLong() }
        ).toDomain()
        store.saveRoute(response)
        store.saveRouteMap(fetchFreshMap(response))
    }

    private suspend fun syncOptimize(op: RouteSyncQueueEntity) {
        val payload = json.decodeFromString(OptimizeRouteSyncPayload.serializer(), op.payloadJson)
        val response = routeApi.optimizeRoute(payload.routeId.toLong(), payload.request).toDomain()
        store.saveRoute(response)
        store.saveRouteMap(fetchFreshMap(response))
    }

    private suspend fun fetchFreshMap(route: Route): RouteMap {
        return runCatching { routeApi.getRouteMap(route.id.toLong()).toDomainRouteMap() }
            .getOrElse { route.toFallbackMap() }
    }

    private suspend fun refreshOfflineArtifacts(route: Route) {
        val routeMap = fetchFreshMap(route)
        store.saveRoute(route)
        store.saveRouteMap(routeMap)

        val graphJson = json.encodeToString(
            RouteOfflineGraphSnapshot(
                routeId = route.id,
                routeName = route.name,
                days = routeMap.days.map { day ->
                    com.travelguide.route.offline.RouteOfflineGraphDaySnapshot(
                        routeDayId = day.routeDayId,
                        dayNumber = day.dayNumber,
                        segments = day.segments.map { segment ->
                            com.travelguide.route.offline.RouteOfflineGraphSegmentSnapshot(
                                fromRoutePointId = segment.fromRoutePointId,
                                toRoutePointId = segment.toRoutePointId,
                                distanceKm = segment.distanceKm,
                                durationMin = segment.durationMin,
                                transportMode = segment.transportMode,
                                provider = segment.provider,
                                status = segment.status,
                                coordinates = segment.polyline?.coordinates.orEmpty()
                            )
                        }
                    )
                }
            )
        )

        runCatching { routeApi.downloadOfflineRoute(route.id.toLong()) }
            .onSuccess { archive -> store.saveOfflineArchive(route.id, archive) }

        store.markRouteOffline(route.id, routeMap, graphJson)
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
        transportMode = runCatching { TransportMode.valueOf(transportMode) }.getOrDefault(TransportMode.WALK),
        status = runCatching { RouteStatus.valueOf(status ?: "READY") }.getOrDefault(RouteStatus.READY),
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
        poiName = poiName,
        poiAddress = poiAddress,
        poiLatitude = poiLatitude,
        poiLongitude = poiLongitude,
        poiType = poiType,
        estimatedVisitMinutes = estimatedVisitMinutes,
        plannedArrivalAt = plannedArrivalAt,
        plannedDepartureAt = plannedDepartureAt
    )

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
                        source = "SYNC_CACHE",
                        coordinates = routePoints.mapNotNull { point ->
                            val lat = point.poiLatitude ?: point.poi?.latitude
                            val lng = point.poiLongitude ?: point.poi?.longitude
                            if (lat != null && lng != null) com.travelguide.domain.models.LatLng(lat, lng) else null
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
