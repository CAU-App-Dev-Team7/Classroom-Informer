package com.example.classroominformer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classroominformer.ui.components.TopBlueHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 전체 알림 목록 화면 (네비게이션에서 사용하는 Screen 이름)
 *
 * ClassroomInformerApp.kt 에서:
 *
 *  NotificationsScreen(
 *      userName = userName,
 *      onBackClick = { navController.popBackStack() },
 *      onNotificationClick = { notification -> ... }
 *  )
 */
@Composable
fun NotificationsScreen(
    userName: String,
    onBackClick: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SharedPrefsNotificationRepository(context) }

    val notifications = remember(userName) {
        repository.getNotifications(userName)
            .sortedByDescending { it.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBlueHeader(
            title = "Notifications",
            showBackButton = true,
            onBackClick = onBackClick
        )

        Text(
            text = "Notification history",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        Divider(color = Color(0xFFE0E0E0))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You don't have any notifications yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(notifications) { item ->
                    NotificationListCard(
                        item = item,
                        onClick = { onNotificationClick(item) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * 하나의 알림을 리스트용 카드로 표현하는 UI
 */
@Composable
private fun NotificationListCard(
    item: NotificationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 안 읽은 알림이면 파란 점 표시
                if (!item.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = Color(0xFF3D8BFF),
                                shape = CircleShape
                            )
                    )
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = formatTimestamp(item.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

/**
 * 알림 한 개의 상세 화면
 *
 * ClassroomInformerApp.kt 에서:
 *
 *  NotificationDetailScreen(
 *      userName = userName,
 *      notificationId = notificationId,
 *      onBackClick = { navController.popBackStack() }
 *  )
 */
@Composable
fun NotificationDetailScreen(
    userName: String,
    notificationId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SharedPrefsNotificationRepository(context) }

    val notification = remember(userName, notificationId) {
        repository.getNotifications(userName)
            .firstOrNull { it.id == notificationId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBlueHeader(
            title = "Notification Detail",
            showBackButton = true,
            onBackClick = onBackClick
        )

        if (notification == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Notification not found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = Color(0xFFE0E0E0)
                )

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * 타임스탬프(ms)를 "yyyy-MM-dd HH:mm" 문자열로 변환
 */
private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return df.format(Date(timestamp))
}
