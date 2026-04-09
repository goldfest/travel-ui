package com.travelguide.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.travelguide.R
import com.travelguide.domain.models.Notification

object NotificationCenter {
    const val CHANNEL_ID = "travelguide_route_notifications"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Уведомления маршрутов",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Создание маршрута и напоминания о начале маршрута"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun show(context: Context, notification: Notification) {
        ensureChannel(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.description))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(notification.id, builder.build())
    }
}
