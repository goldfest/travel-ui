package com.travelguide.route.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM offline_route_download")
    suspend fun getOfflineDownloads(): List<OfflineRouteDownloadEntity>

    @Query("SELECT * FROM offline_route_download WHERE routeId = :routeId")
    suspend fun getOfflineDownload(routeId: Int): OfflineRouteDownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOfflineDownload(entity: OfflineRouteDownloadEntity)

    @Query("DELETE FROM offline_route_download WHERE routeId = :routeId")
    suspend fun deleteOfflineDownload(routeId: Int)

    @Query("SELECT * FROM offline_route_graph_cache WHERE routeId = :routeId")
    suspend fun getOfflineGraph(routeId: Int): OfflineRouteGraphCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOfflineGraph(entity: OfflineRouteGraphCacheEntity)

    @Query("DELETE FROM offline_route_graph_cache WHERE routeId = :routeId")
    suspend fun deleteOfflineGraph(routeId: Int)

    @Query("SELECT * FROM offline_route_archive_cache WHERE routeId = :routeId")
    suspend fun getOfflineArchive(routeId: Int): OfflineRouteArchiveCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOfflineArchive(entity: OfflineRouteArchiveCacheEntity)

    @Query("DELETE FROM offline_route_archive_cache WHERE routeId = :routeId")
    suspend fun deleteOfflineArchive(routeId: Int)


    @Query("SELECT * FROM route_editor_draft_cache WHERE draftKey = :draftKey")
    suspend fun getEditorDraft(draftKey: String): RouteEditorDraftCacheEntity?

    @Query("SELECT * FROM route_editor_draft_cache ORDER BY updatedAtEpochMs DESC")
    suspend fun getAllEditorDrafts(): List<RouteEditorDraftCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEditorDraft(entity: RouteEditorDraftCacheEntity)

    @Query("DELETE FROM route_editor_draft_cache WHERE draftKey = :draftKey")
    suspend fun deleteEditorDraft(draftKey: String)

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

    @Query("SELECT EXISTS(SELECT 1 FROM route_sync_queue WHERE routeId = :routeId)")
    suspend fun hasPendingSync(routeId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM route_sync_queue WHERE routeId = :routeId)")
    fun observeHasPendingSync(routeId: Int): Flow<Boolean>
    @Query("SELECT EXISTS(SELECT 1 FROM route_sync_queue)")
    fun observeAnyPendingSync(): Flow<Boolean>
}
