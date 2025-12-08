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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfessorMainScreen(
    userName: String,
    onSearchClick: () -> Unit = {},
    onTimetableClick: () -> Unit = {},
    onFavouritesClick: () -> Unit = {},
    onReservationsClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val notificationRepo = remember(context) { SharedPrefsNotificationRepository(context) }

    var latestUnread by remember { mutableStateOf<NotificationItem?>(null) }
    var showAlert by remember { mutableStateOf(false) }

    // Load latest unread notification for this user
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
        // 이미 프로젝트에 있는 헤더 컴포저블이라고 가정
        MainHeaderArea(showBackButton = false)

        // 🔔 ALERT POPUP
        if (showAlert && latestUnread != null) {
            NotificationAlertCard(
                title = "Alerts",
                message = latestUnread!!.message,
                onCheckClick = {
                    val target = latestUnread
                    if (target != null) {
                        val updated = notificationRepo
                            .getNotifications(userName)
                            .map { if (it.id == target.id) it.copy(isRead = true) else it }

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
                            .map { if (it.id == target.id) it.copy(isRead = true) else it }

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

        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            // Row 1: Search - Timetable
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

            // Row 2: Favourites - Reservations - Map
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleMenuButton(
                    emoji = "⭐",
                    label = "Favourites",
                    onClick = onFavouritesClick
                )
                CircleMenuButton(
                    emoji = "⏱️",
                    label = "Reservations",
                    onClick = onReservationsClick
                )
                CircleMenuButton(
                    emoji = "📍",
                    label = "Map",
                    onClick = onMapClick
                )
            }

            // Row 3: Notifications - Logout
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

/**
 * Small alert card shown at top of ProfessorMainScreen when there is an unread notification.
 */
@Composable
fun NotificationAlertCard(
    title: String,
    message: String,
    onCheckClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF4E5)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                fontSize = 14.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCloseClick) {
                    Text("Close")
                }
                TextButton(onClick = onCheckClick) {
                    Text("Check")
                }
            }
        }
    }
}
