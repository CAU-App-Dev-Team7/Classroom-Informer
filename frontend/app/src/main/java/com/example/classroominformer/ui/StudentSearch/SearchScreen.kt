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
import com.example.classroominformer.ui.components.TopBlueHeader

@Composable
fun SearchScreen(
    onBack: () -> Unit,                         // ✅ BACK HANDLER ADDED
    onSearchComplete: (List<String>) -> Unit
) {

    val timeSlots = listOf(
        "Period 0 (08:00 - 08:30)",
        "Period 1 (09:00 - 09:30)",
        "Period A (09:30 - 10:00)",
        "Period 2 (10:00 - 10:30)",
        "Period B (10:30 - 11:00)",
        "Period 3 (11:00 - 11:30)",
        "Period 4 (12:00 - 12:30)",
        "Period C (12:30 - 13:00)",
        "Period 5 (13:00 - 13:30)",
        "Period 6 (14:00 - 14:30)",
        "Period D (14:30 - 15:00)",
        "Period 7 (15:00 - 15:30)",
        "Period E (15:30 - 16:00)",
        "Period 8 (16:00 - 16:30)",
        "Period 9 (17:00 - 17:30)",
        "Period F (17:30 - 18:00)",
        "Period 10 (18:00 - 18:30)",
        "Period G (18:30 - 19:00)",
        "Period 11 (19:00 - 19:30)"
    )

    val checkedState = remember { mutableStateListOf<Boolean>() }
    var dropdownOpen by remember { mutableStateOf(false) }

    if (checkedState.isEmpty()) {
        checkedState.addAll(List(timeSlots.size) { false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // ✅ BACK BUTTON ENABLED
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = true,
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        // MAIN CONTENT
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔍 SEARCH BAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .background(Color(0xFFEFEFEF), shape = MaterialTheme.shapes.medium)
                    .clickable { dropdownOpen = true }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val selectedText = checkedState
                        .mapIndexedNotNull { i, checked ->
                            if (checked) timeSlots[i] else null
                        }
                        .joinToString(", ")

                    Text(
                        text = if (selectedText.isEmpty()) "Select time slots…" else selectedText,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.clickable {
                            val selected = timeSlots.filterIndexed { i, _ ->
                                checkedState[i]
                            }
                            onSearchComplete(selected)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 📌 DROPDOWN LIST
            if (dropdownOpen) {
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
                                    checked = checkedState[index],
                                    onCheckedChange = { checked ->
                                        checkedState[index] = checked
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



