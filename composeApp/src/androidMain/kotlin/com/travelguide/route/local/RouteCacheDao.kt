package com.travelguide.route.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RouteCacheDao {
    @Query("SELECT * FROM offline_route_cache")
    suspend fun getAllRoutes(): List<OfflineRouteCacheEntity>

    @Query("SELECT * FROM offline_route_cache WHERE routeId = :routeId")
    suspend fun getRoute(routeId: Int): OfflineRouteCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoute(entity: OfflineRouteCacheEntity)

    @Query("DELETE FROM offline_route_cache WHERE routeId = :routeId")
    suspend fun deleteRoute(routeId: Int)

    @Query("SELECT * FROM offline_route_map_cache WHERE routeId = :routeId")
    suspend fun getRouteMap(routeId: Int): OfflineRouteMapCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRouteMap(entity: OfflineRouteMapCacheEntity)

    @Query("DELETE FROM offline_route_map_cache WHERE routeId = :routeId")
    suspend fun deleteRouteMap(routeId: Int)

    @Query("SELECT * FROM offline_route_poi_catalog WHERE cityId = :cityId")
    suspend fun getPoiCatalog(cityId: Int): OfflineRoutePoiCatalogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPoiCatalog(entity: OfflineRoutePoiCatalogEntity)

    @Query("SELECT * FROM route_sync_queue ORDER BY id ASC")
    suspend fun getPendingSyncOperations(): List<RouteSyncQueueEntity>

    @Query("SELECT * FROM route_sync_queue WHERE routeId = :routeId AND operationType = :operationType LIMIT 1")
    suspend fun findOperation(routeId: Int?, operationType: String): RouteSyncQueueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(entity: RouteSyncQueueEntity)

    @Query("DELETE FROM route_sync_queue WHERE id = :id")
    suspend fun deleteSyncOperation(id: Long)

    @Query("UPDATE route_sync_queue SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: Long)

    @Query("UPDATE route_sync_queue SET routeId = :newRouteId WHERE routeId = :oldRouteId")
    suspend fun rebindQueuedRouteId(oldRouteId: Int, newRouteId: Int)
}
