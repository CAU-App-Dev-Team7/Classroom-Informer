package com.example.classroominformer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.classroominformer.ui.components.TopBlueHeader

@Composable
fun SeminarRoomsListScreen(
    onBack: () -> Unit,
    onRoomClick: (String) -> Unit
) {
    val seminarRooms = listOf(
        "310-701 – Seminar Room",
        "310-702 – Seminar Room",
        "310-703 – Seminar Room",
        "310-704 – Seminar Room",
        "310-705 – Seminar Room"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        // ⭐ SAME BLUE HEADER AS ALL OTHER SCREENS
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = true,
            onBackClick = onBack
        )

        LazyColumn(modifier = Modifier.padding(16.dp)) {
            itemsIndexed(seminarRooms) { _, room ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { onRoomClick(room) },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = room,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
}



