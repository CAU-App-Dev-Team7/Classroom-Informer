package com.example.classroominformer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classroominformer.ui.components.TopBlueHeader

// 즐겨찾기 방 정보만 따로 모델로 둬도 괜찮음
data class FavouriteRoom(
    val id: Long,
    val roomName: String,
    val building: String,
    val floor: String,
    val nextFreeSlot: String,
    val isFreeNow: Boolean
)

@Composable
fun FavouritesScreen(
    userName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    val notificationRepo = remember { SharedPrefsNotificationRepository(context) }

    val favouriteRooms = remember {
        listOf(
            FavouriteRoom(
                id = 1L,
                roomName = "310-210 (Lecture Room)",
                building = "Building 310",
                floor = "2nd floor",
                nextFreeSlot = "15:00 - 17:00",
                isFreeNow = true
            ),
            FavouriteRoom(
                id = 2L,
                roomName = "310-220 (Lecture Room)",
                building = "Building 310",
                floor = "2nd floor",
                nextFreeSlot = "17:00 - 19:00",
                isFreeNow = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = true,
            onBackClick = onBackClick
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favourites",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            items(favouriteRooms) { room ->
                FavouriteRoomCard(
                    room = room,
                    onCheckClick = {
                        if (room.isFreeNow) {
                            val message =
                                "${room.roomName} is now available (${room.nextFreeSlot})."

                            // 1) 히스토리에 저장
                            val item = NotificationItem(
                                id = System.currentTimeMillis(),
                                title = "Favorite room became free",
                                message = message,
                                timestamp = System.currentTimeMillis()
                            )
                            notificationRepo.addNotification(userName, item)

                            // 2) 시스템 알림 발송
                            notificationHelper.notifyFavoriteRoomAvailable(
                                userName = userName,
                                roomName = room.roomName
                            )
                        }
                    },
                    onRemoveClick = {
                        // TODO: backend 에서 즐겨찾기 제거 연동
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun FavouriteRoomCard(
    room: FavouriteRoom,
    onCheckClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = room.roomName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "${room.building} • ${room.floor}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Next free slot ${room.nextFreeSlot}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onRemoveClick) {
                    Text("Remove from favourites")
                }

                Card(
                    modifier = Modifier
                        .widthIn(min = 90.dp)
                        .clickable(onClick = onCheckClick),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (room.isFreeNow)
                            Color(0xFF3D8BFF) else Color(0xFFB0B5C0)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Check !",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
