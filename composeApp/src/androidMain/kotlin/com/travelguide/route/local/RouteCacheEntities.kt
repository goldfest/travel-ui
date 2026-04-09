package com.travelguide.route.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_route_cache")
data class OfflineRouteCacheEntity(
    @PrimaryKey val routeId: Int,
    val cityId: Int,
    val archived: Boolean,
    val updatedAtEpochMs: Long,
    val routeJson: String
)

@Entity(tableName = "offline_route_map_cache")
data class OfflineRouteMapCacheEntity(
    @PrimaryKey val routeId: Int,
    val updatedAtEpochMs: Long,
    val routeMapJson: String
)

@Entity(tableName = "offline_route_poi_catalog")
data class OfflineRoutePoiCatalogEntity(
    @PrimaryKey val cityId: Int,
    val updatedAtEpochMs: Long,
    val poisJson: String
)

@Entity(tableName = "offline_route_download")
data class OfflineRouteDownloadEntity(
    @PrimaryKey val routeId: Int,
    val downloadedAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val hasTiles: Boolean,
    val hasArchive: Boolean,
    val tileMinZoom: Int,
    val tileMaxZoom: Int,
    val graphVersion: Int = 1
)

@Entity(tableName = "offline_route_graph_cache")
data class OfflineRouteGraphCacheEntity(
    @PrimaryKey val routeId: Int,
    val updatedAtEpochMs: Long,
    val graphJson: String
)

@Entity(tableName = "offline_route_archive_cache")
data class OfflineRouteArchiveCacheEntity(
    @PrimaryKey val routeId: Int,
    val updatedAtEpochMs: Long,
    val archiveBytes: ByteArray
)

@Entity(tableName = "route_sync_queue")
data class RouteSyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val routeId: Int?,
    val operationType: String,
    val payloadJson: String,
    val createdAtEpochMs: Long,
    val attempts: Int = 0
)
