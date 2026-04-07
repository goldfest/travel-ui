package com.travelguide.route.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        OfflineRouteCacheEntity::class,
        OfflineRouteMapCacheEntity::class,
        OfflineRoutePoiCatalogEntity::class,
        RouteSyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RouteAppDatabase : RoomDatabase() {
    abstract fun routeCacheDao(): RouteCacheDao

    companion object {
        @Volatile
        private var INSTANCE: RouteAppDatabase? = null

        fun getInstance(context: Context): RouteAppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RouteAppDatabase::class.java,
                    "travelguide_routes.db"
                ).build().also { INSTANCE = it }
            }
    }
}
