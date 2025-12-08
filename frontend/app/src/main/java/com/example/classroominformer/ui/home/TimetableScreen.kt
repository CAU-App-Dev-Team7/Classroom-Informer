package com.example.classroominformer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classroominformer.data.FreeSlotDto
import com.example.classroominformer.data.FreeSlotsResponseDto
import com.example.classroominformer.data.RetrofitClient
import com.example.classroominformer.ui.components.TopBlueHeader
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ─────────────────────────────────
// UI State
// ─────────────────────────────────
private sealed class TimetableUiState {
    object Loading : TimetableUiState()
    data class Success(val freeSlotsByDay: Map<String, List<FreeSlotDto>>) : TimetableUiState()
    data class Error(val message: String) : TimetableUiState()
}

// ─────────────────────────────────
// Week Info
// ─────────────────────────────────
private data class WeekInfo(
    val dates: List<Calendar>,
    val todayIndex: Int
)

// ─────────────────────────────────
// Main Screen
// ─────────────────────────────────
@Composable
fun TimetableScreen(
    buildingCode: String,
    roomNumber: String,
    onBackClick: () -> Unit = {},
    onFreeSlotClick: (String) -> Unit = {}
) {
    val timeSlots = remember {
        generateTimeSlots(
            startHour = 9,
            endHour = 20,
            intervalMinutes = 60
        )
    }

    val weekInfo = remember { createWeekInfo() }
    val weekDates = weekInfo.dates
    var selectedDayIndex by remember { mutableIntStateOf(weekInfo.todayIndex) }

    val formatter = remember { SimpleDateFormat("EEEE (MMM d)", Locale.ENGLISH) }
    val selectedCalendar = weekDates[selectedDayIndex]
    val dayKey = indexToKoreanDay(selectedDayIndex)

    var uiState by remember { mutableStateOf<TimetableUiState>(TimetableUiState.Loading) }
    val scope = rememberCoroutineScope()

    // Fetch from backend
    LaunchedEffect(buildingCode, roomNumber) {
        scope.launch {
            try {
                uiState = TimetableUiState.Loading

                val res: List<FreeSlotsResponseDto> =
                    RetrofitClient.infoApi.getFreeSlotsByRoom(
                        buildingCode = buildingCode,
                        roomNumber = roomNumber
                    )

                val first = res.firstOrNull()
                if (first == null) {
                    uiState = TimetableUiState.Error("No timetable found.")
                } else {
                    uiState = TimetableUiState.Success(first.free_slots_by_day)
                }
            } catch (e: Exception) {
                uiState = TimetableUiState.Error(e.localizedMessage ?: "HTTP Error")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        TopBlueHeader(
            title = "$buildingCode - $roomNumber",
            showBackButton = true,
            onBackClick = onBackClick
        )

        // Navigation header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val canGoPrev = selectedDayIndex > 0
            Text(
                text = "◀",
                modifier = Modifier
                    .width(32.dp)
                    .clickable(enabled = canGoPrev) { if (canGoPrev) selectedDayIndex-- },
                fontSize = 20.sp,
                color = if (canGoPrev) Color.Black else Color.LightGray
            )

            Text(
                text = "Time-Table",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = formatter.format(selectedCalendar.time),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                fontSize = 14.sp
            )

            val canGoNext = selectedDayIndex < 6
            Text(
                text = "▶",
                modifier = Modifier
                    .width(32.dp)
                    .clickable(enabled = canGoNext) { if (canGoNext) selectedDayIndex++ },
                fontSize = 20.sp,
                color = if (canGoNext) Color.Black else Color.LightGray
            )
        }

        // Content card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            when (val state = uiState) {
                TimetableUiState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                is TimetableUiState.Error ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }

                is TimetableUiState.Success -> {
                    val freeSlotsForDay = state.freeSlotsByDay[dayKey] ?: emptyList()

                    LazyColumn(Modifier.fillMaxSize()) {
                        items(timeSlots) { time ->
                            val isFree = isTimeInFreeSlots(time, freeSlotsForDay)
                            TimetableRowReadonly(
                                time = time,
                                isFree = isFree,
                                onClick = { if (isFree) onFreeSlotClick(time) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────
// Time Row UI
// ─────────────────────────────────
@Composable
private fun TimetableRowReadonly(
    time: String,
    isFree: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 12.dp)
            .clickable(enabled = isFree) { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            modifier = Modifier.width(70.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    color = if (isFree) Color(0xFF4CAF50) else Color(0xFFE6E8EB),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFree) "Free" else "Busy",
                color = if (isFree) Color.White else Color.DarkGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────
// Helpers
// ─────────────────────────────────
private fun indexToKoreanDay(index: Int): String =
    listOf("월", "화", "수", "목", "금", "토", "일")[index]

private fun isTimeInFreeSlots(time: String, freeSlots: List<FreeSlotDto>): Boolean {
    fun toMin(t: String): Int {
        val parts = t.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    val tMin = toMin(time)

    return freeSlots.any { slot ->
        val s = toMin(slot.start)
        val e = toMin(slot.end)
        tMin in s until e
    }
}

private fun createWeekInfo(): WeekInfo {
    val now = Calendar.getInstance()
    val dayIndex = when (now.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    val monday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -dayIndex) }

    val dates = (0..6).map { i ->
        (monday.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i) }
    }

    return WeekInfo(dates, dayIndex)
}

private fun generateTimeSlots(
    startHour: Int,
    endHour: Int,
    intervalMinutes: Int
): List<String> {
    val times = mutableListOf<String>()
    val cal = Calendar.getInstance()

    cal.set(Calendar.HOUR_OF_DAY, startHour)
    cal.set(Calendar.MINUTE, 0)

    while (cal.get(Calendar.HOUR_OF_DAY) <= endHour) {
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        times.add(String.format("%02d:00", hour))
        cal.add(Calendar.MINUTE, intervalMinutes)
    }
    return times
}
