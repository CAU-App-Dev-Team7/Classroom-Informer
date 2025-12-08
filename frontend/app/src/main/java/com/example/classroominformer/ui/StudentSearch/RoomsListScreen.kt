package com.example.classroominformer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.classroominformer.ui.components.TopBlueHeader

@Composable
fun RoomsListScreen(
    onBack: () -> Unit,                      // ⭐ ADDED THIS
    onRoomClick: (String) -> Unit           // ⭐ Existing
) {
    val rooms = listOf(
        "310-728" to "Lecture Room",
        "310-729" to "Computer Lab",
        "310-701" to "Design Studio",
        "310-703" to "Conference Room",
        "310-700" to "Special Event Space",
        "310-705" to "Drafting Labs",
        "310-706" to "Specialty Lab",
        "310-707" to "Student Study Space",
        "310-708" to "Study Hall",
        "310-709" to "Sports Hall"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        // 🔵 TOP HEADER WITH BACK BUTTON (same as all screens)
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = true,
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Available Rooms",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            items(rooms) { (roomNumber, roomType) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { onRoomClick(roomNumber) },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$roomNumber — $roomType")
                    androidx.compose.material3.Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

