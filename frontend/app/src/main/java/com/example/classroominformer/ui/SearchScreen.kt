package com.example.classroominformer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.classroominformer.data.AvailableRoomDto
import com.example.classroominformer.data.FreeSlotDto
import com.example.classroominformer.data.RetrofitClient
import com.example.classroominformer.ui.components.TopBlueHeader
import kotlinx.coroutines.launch

// ===================================================================
// SearchScreen
// ===================================================================

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    isReservationMode: Boolean = false,
    onRoomSelected: (Int) -> Unit        // passes REAL room_id (Int)
) {
    // ----------------------------------------------------
    // User Inputs
    // ----------------------------------------------------
    var buildingCode by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }

    var timeSlots by remember { mutableStateOf<List<String>>(emptyList()) }
    val checkedState = remember { mutableStateListOf<Boolean>() }

    var results by remember { mutableStateOf<List<AvailableRoomDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {

        TopBlueHeader(
            title = "Search Rooms",
            showBackButton = true,
            onBackClick = onBack
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // ----------------------------------------------------
            // Building input
            // ----------------------------------------------------
            OutlinedTextField(
                value = buildingCode,
                onValueChange = { buildingCode = it },
                label = { Text("Building code (e.g. 310)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // ----------------------------------------------------
            // Room number input (optional)
            // ----------------------------------------------------
            OutlinedTextField(
                value = roomNumber,
                onValueChange = { roomNumber = it },
                label = { Text("Room number (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ----------------------------------------------------
            // Slot selector → Loads free slots per room
            // ----------------------------------------------------
            SlotSelector(
                timeSlots = timeSlots,
                checkedState = checkedState,
                onGenerateSlots = { bc, rn ->
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val res = RetrofitClient.infoApi.getFreeSlotsByRoom(bc, rn)
                            val first = res.firstOrNull()

                            if (first != null) {
                                timeSlots = flattenFreeSlots(first.free_slots_by_day)
                                checkedState.clear()
                                checkedState.addAll(List(timeSlots.size) { false })
                            } else {
                                errorMessage = "No free slots found."
                            }

                        } catch (e: Exception) {
                            errorMessage = "Failed to load time slots: ${e.localizedMessage}"
                        }
                        isLoading = false
                    }
                },
                buildingCode = buildingCode,
                roomNumber = roomNumber
            )

            Spacer(Modifier.height(16.dp))

            // ----------------------------------------------------
            // SEARCH BUTTON
            // ----------------------------------------------------
            Button(
                onClick = {
                    val selectedSlots = timeSlots.filterIndexed { i, _ -> checkedState[i] }

                    if (selectedSlots.isEmpty()) {
                        errorMessage = "Select at least one time slot."
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        try {
                            results = RetrofitClient.infoApi.getAvailableRooms(
                                buildingCode = buildingCode,
                                slots = selectedSlots
                            )
                        } catch (e: Exception) {
                            errorMessage = "Failed: ${e.localizedMessage}"
                        }

                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search")
            }

            // ----------------------------------------------------
            // Loading indicator
            // ----------------------------------------------------
            if (isLoading) {
                Spacer(Modifier.height(20.dp))
                CircularProgressIndicator()
            }

            // ----------------------------------------------------
            // Error message
            // ----------------------------------------------------
            errorMessage?.let {
                Spacer(Modifier.height(20.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))

            // ----------------------------------------------------
            // SEARCH RESULTS
            // ----------------------------------------------------
            if (results.isNotEmpty()) {
                Text(
                    text = "Available Rooms:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(results) { room ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRoomSelected(room.room_id.toInt()) },
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Column {
                                    Text("${room.building_code}-${room.room_number}")
                                    room.type?.let {
                                        Text(
                                            it,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===================================================================
// SlotSelector (FULL WORKING VERSION)
// ===================================================================

@Composable
fun SlotSelector(
    timeSlots: List<String>,
    checkedState: MutableList<Boolean>,
    onGenerateSlots: (String, String) -> Unit,
    buildingCode: String,
    roomNumber: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column {

        OutlinedTextField(
            value = if (checkedState.any { it }) "Slots selected" else "Select time slots",
            onValueChange = {},
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            // If no data loaded → show "Load slots"
            if (timeSlots.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Load available time slots") },
                    onClick = {
                        if (buildingCode.isNotBlank() && roomNumber.isNotBlank()) {
                            onGenerateSlots(buildingCode, roomNumber)
                        }
                        expanded = false
                    }
                )
                return@DropdownMenu
            }

            // Otherwise show selectable items
            timeSlots.forEachIndexed { index, slot ->
                DropdownMenuItem(
                    text = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(slot)
                            Checkbox(
                                checked = checkedState[index],
                                onCheckedChange = { checkedState[index] = it }
                            )
                        }
                    },
                    onClick = { }
                )
            }
        }
    }
}

// ===================================================================
// FLATTEN FREE SLOTS
// ===================================================================

fun flattenFreeSlots(map: Map<String, List<FreeSlotDto>>): List<String> {
    val out = mutableListOf<String>()
    map.forEach { (day, slots) ->
        slots.forEach { slot ->
            out.add("$day ${slot.start} - ${slot.end}")
        }
    }
    return out
}
