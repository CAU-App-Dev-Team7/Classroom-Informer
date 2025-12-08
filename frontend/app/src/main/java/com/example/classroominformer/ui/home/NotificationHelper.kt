package com.example.classroominformer.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.classroominformer.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "favorite_room_channel"
        private const val CHANNEL_NAME = "Favorite Room Alerts"
        private const val CHANNEL_DESC = "Notifies when your favourite room becomes free"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createChannelIfNeeded()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = CHANNEL_DESC
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    /** Check POST_NOTIFICATIONS permission on Android 13+ */
    private fun hasPostNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Show alert like: "310-210 (Lecture Room) is now free!"
     *
     * You can call:
     *  NotificationHelper(context).notifyFavoriteRoomAvailable(userName, "310-210 (Lecture Room)")
     */
    @SuppressLint("MissingPermission")
    fun notifyFavoriteRoomAvailable(
        userName: String,
        roomName: String        // ✅ just a String, no FavouriteRoom class needed
    ) {
        // If permission not granted (Android 13+), do nothing
        if (!hasPostNotificationPermission()) return

        val message = "$roomName is now free for you!"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            // ✅ use launcher icon so you don't need ic_notification
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Favorite Room Available")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID, builder.build())
    }
}
