package com.travelguide.route

import android.content.Context
import com.travelguide.domain.models.MapViewport

class OfflineRouteTilePrefetcher(
    @Suppress("UNUSED_PARAMETER") private val context: Context
) {
    suspend fun prefetch(
        @Suppress("UNUSED_PARAMETER") viewport: MapViewport?,
        @Suppress("UNUSED_PARAMETER") minZoom: Int = 12,
        @Suppress("UNUSED_PARAMETER") maxZoom: Int = 16
    ) {
        return
    }
}
