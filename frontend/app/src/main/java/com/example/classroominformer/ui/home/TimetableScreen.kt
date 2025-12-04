package com.example.classroominformer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classroominformer.ui.components.TopBlueHeader

data class TimeSlot(
    val time: String,
    val subject: String
)

@Composable
fun TimetableScreen(
    dayLabel: String = "Monday (Nov 14)",
    onBackClick: () -> Unit = {}
) {
    val initialSlots = listOf(
        TimeSlot("8:00", "Linux"),
        TimeSlot("9:00", "Linux"),
        TimeSlot("12:00", "NO Class"),
        TimeSlot("14:00", "NO Class"),
        TimeSlot("15:00", "Linux"),
        TimeSlot("16:30", "Linux"),
        TimeSlot("", "") // empty last row like your mockup
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top blue header
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = false      // we put back arrow in the white bar below
        )

        // White bar: back arrow + "Time-Table" + Day label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                fontSize = 24.sp,
                modifier = Modifier
                    .clickable { onBackClick() }
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = "Time-Table",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Time slots list
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(initialSlots) { slot ->
                TimeSlotRow(
                    time = slot.time,
                    initialSubject = slot.subject
                )
            }
        }
    }
}
