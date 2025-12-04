package com.example.classroominformer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfessorMainScreen(
    onSearchClick: () -> Unit = {},
    onTimetableClick: () -> Unit = {},
    onFavouritesClick: () -> Unit = {},
    onReservationsClick: () -> Unit = {},
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
        MainHeaderArea(showBackButton = false)

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
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )
                CircleMenuButton(
                    emoji = "📅",
                    label = "TimeTable",
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Favourites - Reservations (center) - Map
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleMenuButton(
                    emoji = "⭐",
                    label = "Favourites"
                )
                CircleMenuButton(
                    emoji = "⏱️",
                    label = "Reservations"
                )
                CircleMenuButton(
                    emoji = "📍",
                    label = "Map"
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
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
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
