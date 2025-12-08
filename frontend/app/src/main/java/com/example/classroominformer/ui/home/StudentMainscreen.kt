package com.example.classroominformer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun StudentMainScreen(
    userName: String,
    onSearchClick: () -> Unit = {},
    onTimetableClick: () -> Unit = {},
    onFavouritesClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val notificationRepo = remember(context) { SharedPrefsNotificationRepository(context) }

    var latestUnread by remember { mutableStateOf<NotificationItem?>(null) }
    var showAlert by remember { mutableStateOf(false) }

    // Load notifications for this user when screen opens
    LaunchedEffect(userName) {
        val all = notificationRepo.getNotifications(userName)
        latestUnread = all.firstOrNull { !it.isRead }
        showAlert = latestUnread != null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainHeaderArea()

        // -------- top alert card ----------
        if (showAlert && latestUnread != null) {
            NotificationAlertCard(
                title = "Alerts",
                message = latestUnread!!.message,
                onCheckClick = {
                    val target = latestUnread
                    if (target != null) {
                        val updated = notificationRepo
                            .getNotifications(userName)
                            .map { n ->
                                if (n.id == target.id) n.copy(isRead = true) else n
                            }
                        notificationRepo.saveNotifications(userName, updated)
                    }
                    showAlert = false
                    onNotificationsClick()
                },
                onCloseClick = {
                    val target = latestUnread
                    if (target != null) {
                        val updated = notificationRepo
                            .getNotifications(userName)
                            .map { n ->
                                if (n.id == target.id) n.copy(isRead = true) else n
                            }
                        notificationRepo.saveNotifications(userName, updated)
                    }
                    showAlert = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // ---------- main menu ----------
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            // Row 1: Search / Timetable
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleMenuButton(
                    emoji = "🔍",
                    label = "Search",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    onClick = onSearchClick
                )
                CircleMenuButton(
                    emoji = "📅",
                    label = "TimeTable",
                    modifier = Modifier.weight(1f),
                    onClick = onTimetableClick
                )
            }

            // Row 2: Favourites / Map
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleMenuButton(
                    emoji = "⭐",
                    label = "Favourites",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    onClick = onFavouritesClick
                )
                CircleMenuButton(
                    emoji = "📍",
                    label = "Map",
                    modifier = Modifier.weight(1f),
                    onClick = onMapClick
                )
            }

            // Row 3: Notifications / Logout
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleMenuButton(
                    emoji = "🔔",
                    label = "Notifications",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    onClick = onNotificationsClick
                )
                CircleMenuButton(
                    emoji = "🚪",
                    label = "Logout",
                    modifier = Modifier.weight(1f),
                    onClick = onLogoutClick
                )
            }
        }
    }
}
