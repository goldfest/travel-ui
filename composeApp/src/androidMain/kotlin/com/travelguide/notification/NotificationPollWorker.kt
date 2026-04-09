package com.travelguide.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.russhwolf.settings.SharedPreferencesSettings
import com.travelguide.auth.TokenStorage
import com.travelguide.network.HttpClientFactory
import com.travelguide.network.notification.NotificationApi

class NotificationPollWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val settings = SharedPreferencesSettings(
        appContext.getSharedPreferences("travelguide_settings", Context.MODE_PRIVATE)
    )
    private val tokenStorage = TokenStorage(settings)
    private val api = NotificationApi(
        HttpClientFactory().create("http://192.168.1.9:8084/api", tokenStorage),
        "http://192.168.1.9:8086/api/notifications"
    )
    private val repository = NotificationRepository(api)

    override suspend fun doWork(): Result {
        return runCatching {
            NotificationCenter.ensureChannel(applicationContext)
            val shownKey = "notification_worker_last_seen_id"
            val lastSeenId = settings.getInt(shownKey, 0)
            val unread = repository.getUnreadNotifications(size = 20)
            var maxId = lastSeenId
            unread.filter { it.id > lastSeenId && !it.isRead }.sortedBy { it.id }.forEach {
                NotificationCenter.show(applicationContext, it)
                if (it.id > maxId) maxId = it.id
            }
            settings.putInt(shownKey, maxId)
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
