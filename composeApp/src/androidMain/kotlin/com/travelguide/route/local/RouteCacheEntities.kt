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

@Entity(tableName = "route_sync_queue")
data class RouteSyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val routeId: Int?,
    val operationType: String,
    val payloadJson: String,
    val createdAtEpochMs: Long,
    val attempts: Int = 0
)
