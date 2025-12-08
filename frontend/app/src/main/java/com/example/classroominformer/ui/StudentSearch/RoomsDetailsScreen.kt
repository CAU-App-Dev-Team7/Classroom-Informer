package com.example.classroominformer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.classroominformer.ui.components.TopBlueHeader

@Composable
fun RoomDetailScreen(
    roomId: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // 🔵 Header (with back button)
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = true,
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🟦 Room Name
        Text(
            text = roomId,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🟦 Two grey placeholder boxes (matching professor UI)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color(0xFFE0E0E0))
            )
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color(0xFFE0E0E0))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 📝 Room details
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Room Information:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Text("• Capacity: 60 students")
            Text("• Projector: Available")
            Text("• Air conditioning: Yes")
            Text("• Type: Lecture Room / Lab / Seminar (varies)")
        }

        Spacer(modifier = Modifier.weight(1f))

        // No "Reserve" button for students — they only view rooms
        // So we leave the bottom empty for cleanliness
    }
}
