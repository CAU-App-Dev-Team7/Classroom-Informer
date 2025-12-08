package com.example.classroominformer.ui.home

import android.content.Context

/**
 * Stores notifications for each user using SharedPreferences.
 *
 * Encoded format (per item):
 *   id|type|title|message|relatedRoomId|isRead|timestamp;
 */
interface NotificationRepository {
    fun getNotifications(userName: String): List<NotificationItem>
    fun addNotification(userName: String, item: NotificationItem)
    fun saveNotifications(userName: String, items: List<NotificationItem>)
}

class SharedPrefsNotificationRepository(
    private val context: Context
) : NotificationRepository {

    companion object {
        private const val PREF_NAME = "notifications_prefs"
    }

    override fun getNotifications(userName: String): List<NotificationItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("notifications_$userName", null) ?: return emptyList()

        return raw.split(";")
            .filter { it.isNotBlank() }
            .mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size != 7) return@mapNotNull null

                val id = parts[0].toLongOrNull() ?: return@mapNotNull null
                val type = parts[1]
                val title = parts[2]
                val message = parts[3]
                val relatedRoomId = parts[4].toLongOrNull()
                val isRead = parts[5].toBooleanStrictOrNull() ?: false
                val timestamp = parts[6].toLongOrNull() ?: 0L

                NotificationItem(
                    id = id,
                    type = type,
                    title = title,
                    message = message,
                    relatedRoomId = relatedRoomId,
                    isRead = isRead,
                    timestamp = timestamp
                )
            }
    }

    override fun addNotification(userName: String, item: NotificationItem) {
        val all = getNotifications(userName)
        val newList = listOf(item) + all
        saveNotifications(userName, newList)
    }

    override fun saveNotifications(userName: String, items: List<NotificationItem>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val encoded = items.joinToString(";") { item ->
            "${item.id}|" +
                    "${item.type.replace("|", " ")}|" +
                    "${item.title.replace("|", " ")}|" +
                    "${item.message.replace("|", " ")}|" +
                    "${item.relatedRoomId ?: ""}|" +
                    "${item.isRead}|" +
                    "${item.timestamp}"
        }

        prefs.edit()
            .putString("notifications_$userName", encoded)
            .apply()
    }
}
