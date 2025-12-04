package com.example.classroominformer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun StudentMainScreen(
    onSearchClick: () -> Unit = {},
    onTimetableClick: () -> Unit = {},
    onFavouritesClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainHeaderArea()

        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
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
                        .padding(end = 16.dp)
                )
                CircleMenuButton(
                    emoji = "📅",
                    label = "TimeTable",
                    modifier = Modifier.weight(1f)
                )
            }

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
                        .padding(end = 16.dp)
                )
                CircleMenuButton(
                    emoji = "📍",
                    label = "Map",
                    modifier = Modifier.weight(1f)
                )
            }

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
                        .padding(end = 16.dp)
                )
                CircleMenuButton(
                    emoji = "🚪",
                    label = "Logout",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
