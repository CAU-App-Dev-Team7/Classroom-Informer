package com.example.classroominformer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.classroominformer.ui.components.TopBlueHeader   // ✅ FIXED IMPORT

@Composable
fun SeminarRoomDetailScreen(
    roomId: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {

        // ✅ TOP HEADER
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = true,
            onBackClick = onBack
        )

        // ✅ ROOM TITLE
        Text(
            text = roomId,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(start = 24.dp, top = 16.dp, bottom = 12.dp)
        )

        // ✅ IMAGES ROW (PLACEHOLDERS)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color.LightGray)
            )
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color.LightGray)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ DETAILS SECTION
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Other Details:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("• Capacity: 45 students")
            Text("• Projector: Available")
            Text("• Air conditioning: Yes")
        }

        // ✅ SMALL SPACER (NO WEIGHT)
        Spacer(modifier = Modifier.height(30.dp))

        // ✅ RESERVE BUTTON
        Button(
            onClick = { /* TODO: Hook to reservation logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
        ) {
            Text("Reserve", color = Color.White)
        }
    }
}
