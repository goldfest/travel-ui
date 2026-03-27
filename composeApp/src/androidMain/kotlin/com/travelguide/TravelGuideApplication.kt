package com.travelguide

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class TravelGuideApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
    }
}