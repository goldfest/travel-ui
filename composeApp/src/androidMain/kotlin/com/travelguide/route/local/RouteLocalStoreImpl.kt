package com.travelguide.route.local

import com.travelguide.domain.models.POI
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteMap
import com.travelguide.route.offline.PendingRouteSyncOperation
import com.travelguide.route.offline.RouteLocalStore

class RouteLocalStoreImpl(
    private val dao: RouteCacheDao
) : RouteLocalStore {

    override suspend fun getRoutes(archived: Boolean): List<Route> {
        return dao.getAllRoutes()
            .map { RouteJson.decodeRoute(it.routeJson) }
            .filter { it.isArchived == archived }
            .sortedByDescending { it.id }
    }

    override suspend fun saveRoutes(routes: List<Route>) {
        routes.forEach { saveRoute(it) }
    }

    override suspend fun getRoute(routeId: Int): Route? {
        return dao.getRoute(routeId)?.let { RouteJson.decodeRoute(it.routeJson) }
    }

    override suspend fun saveRoute(route: Route) {
        dao.upsertRoute(
            OfflineRouteCacheEntity(
                routeId = route.id,
                cityId = route.cityId,
                archived = route.isArchived,
                updatedAtEpochMs = System.currentTimeMillis(),
                routeJson = RouteJson.encodeRoute(route)
            )
        )
    }

    override suspend fun deleteRoute(routeId: Int) {
        dao.deleteRoute(routeId)
    }

    override suspend fun getRouteMap(routeId: Int): RouteMap? {
        return dao.getRouteMap(routeId)?.let { RouteJson.decodeRouteMap(it.routeMapJson) }
    }

    override suspend fun saveRouteMap(routeMap: RouteMap) {
        dao.upsertRouteMap(
            OfflineRouteMapCacheEntity(
                routeId = routeMap.routeId,
                updatedAtEpochMs = System.currentTimeMillis(),
                routeMapJson = RouteJson.encodeRouteMap(routeMap)
            )
        )
    }

    override suspend fun deleteRouteMap(routeId: Int) {
        dao.deleteRouteMap(routeId)
    }

    override suspend fun getPoisByCity(cityId: Int): List<POI> {
        return dao.getPoiCatalog(cityId)?.let { RouteJson.decodePois(it.poisJson) }.orEmpty()
    }

    override suspend fun savePoisByCity(cityId: Int, pois: List<POI>) {
        dao.upsertPoiCatalog(
            OfflineRoutePoiCatalogEntity(
                cityId = cityId,
                updatedAtEpochMs = System.currentTimeMillis(),
                poisJson = RouteJson.encodePois(pois)
            )
        )
    }

    override suspend fun findPoiById(poiId: Int): POI? {
        return dao.getAllRoutes()
            .asSequence()
            .map { RouteJson.decodeRoute(it.routeJson) }
            .flatMap { route ->
                route.days.asSequence().flatMap { day ->
                    day.points.asSequence().mapNotNull { point -> point.poi }
                }
            }
            .firstOrNull { it.id == poiId }
    }

    override suspend fun enqueue(operation: PendingRouteSyncOperation) {
        dao.enqueue(
            RouteSyncQueueEntity(
                routeId = operation.routeId,
                operationType = operation.operationType.name,
                payloadJson = operation.payloadJson,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    override suspend fun replacePendingCreate(operation: PendingRouteSyncOperation) {
        val existing = dao.findOperation(operation.routeId, operation.operationType.name)
        if (existing != null) {
            dao.enqueue(
                existing.copy(
                    payloadJson = operation.payloadJson,
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
        } else {
            enqueue(operation)
        }
    }
}
