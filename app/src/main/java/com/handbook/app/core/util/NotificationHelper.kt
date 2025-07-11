package com.handbook.app.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.handbook.app.R

object NotificationHelper {

    fun createNotificationChannels(context: Context) {
        createGeneralNotificationChannels(context)
    }

    private fun createGeneralNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
        val channelId = context.getString(R.string.general_notifications_channel_id)
        val channelName = context.getString(R.string.title_general_notifications)
        val channelDesc = context.getString(R.string.desc_general_notifications)
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.description = channelDesc

        notificationManager.createNotificationChannel(channel)
    }
}