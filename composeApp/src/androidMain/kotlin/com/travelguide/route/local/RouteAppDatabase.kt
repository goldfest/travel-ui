package com.travelguide.route.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        OfflineRouteCacheEntity::class,
        OfflineRouteMapCacheEntity::class,
        OfflineRoutePoiCatalogEntity::class,
        OfflineRouteDownloadEntity::class,
        OfflineRouteGraphCacheEntity::class,
        OfflineRouteArchiveCacheEntity::class,
        RouteSyncQueueEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RouteAppDatabase : RoomDatabase() {
    abstract fun routeCacheDao(): RouteCacheDao

    companion object {
        @Volatile
        private var INSTANCE: RouteAppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS offline_route_download (
                        routeId INTEGER NOT NULL PRIMARY KEY,
                        downloadedAtEpochMs INTEGER NOT NULL,
                        updatedAtEpochMs INTEGER NOT NULL,
                        hasTiles INTEGER NOT NULL,
                        hasArchive INTEGER NOT NULL,
                        tileMinZoom INTEGER NOT NULL,
                        tileMaxZoom INTEGER NOT NULL,
                        graphVersion INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS offline_route_graph_cache (
                        routeId INTEGER NOT NULL PRIMARY KEY,
                        updatedAtEpochMs INTEGER NOT NULL,
                        graphJson TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS offline_route_archive_cache (
                        routeId INTEGER NOT NULL PRIMARY KEY,
                        updatedAtEpochMs INTEGER NOT NULL,
                        archiveBytes BLOB NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): RouteAppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RouteAppDatabase::class.java,
                    "travelguide_routes.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
