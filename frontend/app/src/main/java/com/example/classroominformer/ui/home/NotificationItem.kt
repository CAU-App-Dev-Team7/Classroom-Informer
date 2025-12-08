package com.example.classroominformer.ui.home

/**
 * Notification data model used for the notification history + alert cards.
 */
data class NotificationItem(
    val id: Long,
    val type: String = "favorite",      // "favorite", "system", "schedule", etc.
    val title: String,
    val message: String,
    val relatedRoomId: Long? = null,
    val isRead: Boolean = false,
    val timestamp: Long
)
