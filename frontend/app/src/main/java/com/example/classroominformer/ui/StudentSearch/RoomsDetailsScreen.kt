package com.example.classroominformer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = true,
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = roomId,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("• Capacity: 45 students")
            Text("• Projector: Available")
            Text("• Air conditioning: Yes")
            Text("• Whiteboard: Yes")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* TODO: hook reservation or favourite */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
        ) {
            Text("Reserve", color = Color.White)
        }
    }
}
