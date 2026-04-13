package com.travelguide.route.offline

import com.travelguide.domain.models.POI
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteMap
import kotlinx.coroutines.flow.Flow

interface RouteLocalStore {
    suspend fun getRoutes(archived: Boolean): List<Route>
    suspend fun getOfflineRoutes(): List<Route>
    suspend fun saveRoutes(routes: List<Route>)
    suspend fun getRoute(routeId: Int): Route?
    suspend fun saveRoute(route: Route)
    suspend fun deleteRoute(routeId: Int)

    suspend fun getRouteMap(routeId: Int): RouteMap?
    suspend fun saveRouteMap(routeMap: RouteMap)
    suspend fun deleteRouteMap(routeId: Int)

    suspend fun getPoisByCity(cityId: Int): List<POI>
    suspend fun savePoisByCity(cityId: Int, pois: List<POI>)
    suspend fun findPoiById(poiId: Int): POI?

    suspend fun markRouteOffline(routeId: Int, routeMap: RouteMap, graphJson: String? = null)
    suspend fun unmarkRouteOffline(routeId: Int)
    suspend fun isRouteOffline(routeId: Int): Boolean
    suspend fun getOfflineRouteGraph(routeId: Int): String?
    suspend fun getOfflineArchive(routeId: Int): ByteArray?
    suspend fun saveOfflineArchive(routeId: Int, archiveBytes: ByteArray)

    suspend fun enqueue(operation: PendingRouteSyncOperation)
    suspend fun replacePendingCreate(operation: PendingRouteSyncOperation)
    suspend fun hasPendingSync(routeId: Int): Boolean
    fun observePendingSync(routeId: Int): Flow<Boolean>
}
