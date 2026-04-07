package com.travelguide

import android.app.Application
import org.osmdroid.config.Configuration

class TravelGuideApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName
    }
}