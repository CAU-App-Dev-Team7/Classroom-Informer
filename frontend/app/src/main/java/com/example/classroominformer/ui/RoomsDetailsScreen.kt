package com.example.classroominformer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classroominformer.data.RetrofitClient
import com.example.classroominformer.data.RoomDetailDto
import com.example.classroominformer.ui.components.TopBlueHeader
import kotlinx.coroutines.launch

@Composable
fun RoomDetailScreen(
    roomId: Long,
    onBack: () -> Unit,
    onOpenTimetable: (String, String) -> Unit
) {
    val infoApi = RetrofitClient.infoApi
    val scope = rememberCoroutineScope()

    var detail by remember { mutableStateOf<RoomDetailDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // -------------------------------
    // Load room detail from backend
    // -------------------------------
    LaunchedEffect(roomId) {
        scope.launch {
            try {
                loading = true
                error = null

                detail = infoApi.getRoomDetail(roomId)   // <-- backend call

            } catch (e: Exception) {
                error = e.message ?: "Failed to load room detail"
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        TopBlueHeader(
            title = "Room Detail",
            showBackButton = true,
            onBackClick = onBack
        )

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            error != null -> {
                Text(
                    text = error!!,
                    color = Color.Red,
                    modifier = Modifier.padding(24.dp)
                )
            }

            detail != null -> {
                val d = detail!!

                // Title
                Text(
                    text = "${d.building_code}-${d.room_number}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(24.dp)
                )

                // Basic backend info
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Building: ${d.building_code}", fontSize = 16.sp)
                    Text("Room number: ${d.room_number}", fontSize = 16.sp)
                    Text("Room ID: ${d.id}", fontSize = 14.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            onOpenTimetable(d.building_code, d.room_number)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Timetable")
                    }

                    Button(
                        onClick = { /* optional features later */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        )
                    ) {
                        Text("Reserve", color = Color.White)
                    }
                }
            }
        }
    }
}
