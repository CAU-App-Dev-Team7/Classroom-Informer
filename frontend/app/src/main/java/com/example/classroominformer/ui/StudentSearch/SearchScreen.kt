package com.example.classroominformer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.classroominformer.data.RetrofitClient
import com.example.classroominformer.data.RoomFreeSlotsResponse
import com.example.classroominformer.ui.components.TopBlueHeader
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    isReservationMode: Boolean,                    // 👈 added
    onSearchComplete: (List<String>) -> Unit
) {
    // --- user inputs ---
    var buildingCode by remember { mutableStateOf("") }   // e.g. "310"
    var roomNumber by remember { mutableStateOf("") }     // e.g. "201"

    // --- slots shown in UI ---
    var timeSlots by remember { mutableStateOf<List<String>>(emptyList()) }
    val checkedState = remember { mutableStateListOf<Boolean>() }

    var dropdownOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Optional: change subtitle depending on mode
    val modeHint = if (isReservationMode) {
        "Select time slots to reserve this room."
    } else {
        "Select free time slots to check empty rooms."
    }

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

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = modeHint,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // --- building / room input ---
            OutlinedTextField(
                value = buildingCode,
                onValueChange = { buildingCode = it },
                label = { Text("Building code (e.g. 310)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = roomNumber,
                onValueChange = { roomNumber = it },
                label = { Text("Room number (e.g. 201)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // loading / error message
            if (isLoading) {
                Text(
                    text = "Loading free time slots...",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // --- clickable "search bar" area ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .background(Color(0xFFEFEFEF), shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedText = timeSlots
                        .mapIndexedNotNull { i, slot ->
                            if (checkedState.getOrNull(i) == true) slot else null
                        }
                        .joinToString(", ")

                    Text(
                        text = if (selectedText.isEmpty())
                            "Select free time slots…" else selectedText,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.clickable {
                            if (buildingCode.isBlank() || roomNumber.isBlank()) {
                                errorMessage = "Enter building code and room number first."
                                return@clickable
                            }

                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val api = RetrofitClient.infoApi
                                    val result: List<RoomFreeSlotsResponse> =
                                        api.getRoomFreeSlots(
                                            buildingCode = buildingCode.trim(),
                                            roomNumber = roomNumber.trim()
                                        )

                                    val first = result.firstOrNull()
                                    if (first == null) {
                                        timeSlots = emptyList()
                                        checkedState.clear()
                                        errorMessage = "No free slots found."
                                    } else {
                                        // flatten { day -> slots } into ["월 09:00 - 10:00", ...]
                                        val flat = mutableListOf<String>()
                                        first.free_slots_by_day.forEach { (day, slots) ->
                                            slots.forEach { slot ->
                                                flat.add("$day ${slot.start} - ${slot.end}")
                                            }
                                        }
                                        timeSlots = flat
                                        checkedState.clear()
                                        checkedState.addAll(List(flat.size) { false })
                                        dropdownOpen = true
                                    }
                                } catch (e: Exception) {
                                    errorMessage =
                                        "Failed to load slots: ${e.localizedMessage ?: "Unknown error"}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- dropdown list with checkboxes ---
            if (dropdownOpen && timeSlots.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        itemsIndexed(timeSlots) { index, slot ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(slot)

                                Checkbox(
                                    checked = checkedState.getOrNull(index) ?: false,
                                    onCheckedChange = { checked ->
                                        if (index < checkedState.size) {
                                            checkedState[index] = checked
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ✅ FINALIZE SELECTION BUTTON
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val selected = timeSlots.filterIndexed { i, _ ->
                        checkedState.getOrNull(i) == true
                    }
                    onSearchComplete(selected)
                },
                enabled = timeSlots.isNotEmpty()
            ) {
                Text(
                    if (isReservationMode) "Reserve selected slots"
                    else "Use selected slots"
                )
            }
        }
    }
}
