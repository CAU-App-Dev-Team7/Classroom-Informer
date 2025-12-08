package com.example.classroominformer.ui.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classroominformer.ui.components.TopBlueHeader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ──────────────────────────
// Data models
// ──────────────────────────

data class TimetableItem(
    val time: String,
    val label: String?,   // null = empty slot
)

data class WeekInfo(
    val dates: List<Calendar>, // Mon..Sun
    val todayIndex: Int        // 0..6
)

// ──────────────────────────
// Repository abstraction
// ──────────────────────────

interface TimetableRepository {
    fun loadWeek(userName: String): Map<Int, Map<String, String>>
    fun saveWeek(userName: String, data: Map<Int, Map<String, String>>)
}

/**
 * Local SharedPreferences implementation.
 * Later you can create RemoteTimetableRepository that calls your backend.
 */
class SharedPrefsTimetableRepository(
    private val context: Context
) : TimetableRepository {

    companion object {
        private const val PREF_NAME = "timetable_prefs"
    }

    override fun loadWeek(userName: String): Map<Int, Map<String, String>> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("timetable_$userName", null)

        val result = (0..6).associateWith { mutableMapOf<String, String>() }.toMutableMap()

        if (raw.isNullOrBlank()) return result

        // format: day|time=subject;day|time=subject;...
        raw.split(";").forEach { entry ->
            if (entry.isBlank()) return@forEach
            val parts = entry.split("=")
            if (parts.size != 2) return@forEach

            val key = parts[0]           // "0|09:00"
            val subject = parts[1]

            val keyParts = key.split("|")
            if (keyParts.size != 2) return@forEach

            val dayIndex = keyParts[0].toIntOrNull() ?: return@forEach
            val time = keyParts[1]

            result[dayIndex]?.put(time, subject)
        }

        return result.mapValues { it.value.toMap() }
    }

    override fun saveWeek(userName: String, data: Map<Int, Map<String, String>>) {
        val builder = StringBuilder()

        data.forEach { (dayIndex, dayMap) ->
            dayMap.forEach { (time, subject) ->
                if (builder.isNotEmpty()) builder.append(";")
                builder.append(dayIndex)
                    .append("|")
                    .append(time)
                    .append("=")
                    .append(subject)
            }
        }

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("timetable_$userName", builder.toString())
            .apply()
    }
}

// ──────────────────────────
// Main screen
// ──────────────────────────

@Composable
fun TimetableScreen(
    userName: String,
    onBackClick: () -> Unit = {},
    onEmptySlotClick: (String) -> Unit = {},   // called when label == "NO CLASS"
    repository: TimetableRepository? = null
) {
    val context = LocalContext.current

    val timetableRepo = remember(context, repository) {
        repository ?: SharedPrefsTimetableRepository(context)
    }

    // time slots: 09:00..20:00, 1h
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

    // 1) load plain map from repo
    val loadedData = remember(userName) { timetableRepo.loadWeek(userName) }

    // 2) convert to state maps for Compose
    val subjectsByDay: Map<Int, SnapshotStateMap<String, String>> =
        remember(userName) {
            (0..6).associateWith { dayIndex ->
                val stateMap = mutableStateMapOf<String, String>()
                loadedData[dayIndex]?.forEach { (time, subject) ->
                    stateMap[time] = subject
                }
                stateMap
            }
        }

    val formatter = remember { SimpleDateFormat("EEEE (MMM d)", Locale.ENGLISH) }

    val selectedCalendar = weekDates[selectedDayIndex]
    val daySubjects = subjectsByDay[selectedDayIndex]!!

    val slotsForUi = timeSlots.map { time ->
        TimetableItem(time = time, label = daySubjects[time])
    }

    // dialog state
    var editingTime by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBlueHeader(
            title = "Classroom Informer",
            showBackButton = true,
            onBackClick = onBackClick
        )

        // header row: ◀ Time-Table Monday (Nov 14) ▶
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val canGoPrev = selectedDayIndex > 0
            Text(
                text = "◀",
                fontSize = 20.sp,
                modifier = Modifier
                    .width(32.dp)
                    .clickable(enabled = canGoPrev) {
                        if (canGoPrev) selectedDayIndex--
                    },
                textAlign = TextAlign.Start,
                color = if (canGoPrev) Color.Black else Color.LightGray
            )

            Text(
                text = "Time-Table",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Text(
                text = formatter.format(selectedCalendar.time),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )

            val canGoNext = selectedDayIndex < 6
            Text(
                text = "▶",
                fontSize = 20.sp,
                modifier = Modifier
                    .width(32.dp)
                    .clickable(enabled = canGoNext) {
                        if (canGoNext) selectedDayIndex++
                    },
                textAlign = TextAlign.End,
                color = if (canGoNext) Color.Black else Color.LightGray
            )
        }

        // list
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                items(slotsForUi) { item ->

                    // check if this slot is "NO CLASS"
                    val isNoClass = item.label
                        ?.trim()
                        ?.uppercase(Locale.ENGLISH) == "NO CLASS"

                    TimetableRow(
                        item = item,
                        onClick = {
                            if (isNoClass) {
                                // go to professor/student search page
                                onEmptySlotClick(item.time)
                            } else {
                                // normal edit flow
                                editingTime = item.time
                                editingText = daySubjects[item.time] ?: ""
                            }
                        }
                    )
                }
            }
        }
    }

    // edit dialog
    if (editingTime != null) {
        AlertDialog(
            onDismissRequest = { editingTime = null },
            title = { Text(text = "Edit $editingTime") },
            text = {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = { editingText = it },
                    singleLine = true,
                    label = { Text("Subject") },
                    placeholder = { Text("e.g. Linux, HCI, NO CLASS") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val time = editingTime!!
                        if (editingText.isBlank()) {
                            daySubjects.remove(time)
                        } else {
                            daySubjects[time] = editingText
                        }

                        timetableRepo.saveWeek(
                            userName,
                            subjectsByDay.mapValues { it.value.toMap() }
                        )
                        editingTime = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            val time = editingTime!!
                            daySubjects.remove(time)
                            timetableRepo.saveWeek(
                                userName,
                                subjectsByDay.mapValues { it.value.toMap() }
                            )
                            editingTime = null
                        }
                    ) { Text("Clear") }

                    TextButton(onClick = { editingTime = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

// ──────────────────────────
// Row + helpers
// ──────────────────────────

@Composable
private fun TimetableRow(
    item: TimetableItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.time,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.width(70.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        val label = item.label
        val isEmpty = label.isNullOrBlank()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    color = if (isEmpty) Color(0xFFE6E8EB) else Color(0xFF4A8CFF),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isEmpty) "Add subject" else label!!,
                color = if (isEmpty) Color.DarkGray else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun createWeekInfo(): WeekInfo {
    val now = Calendar.getInstance()
    val dayOfWeek = now.get(Calendar.DAY_OF_WEEK) // 1=Sun..7=Sat

    val todayIndex = when (dayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    val mondayCal = (now.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, -todayIndex)
    }

    val dates = (0..6).map { i ->
        (mondayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i) }
    }

    return WeekInfo(dates = dates, todayIndex = todayIndex)
}

private fun generateTimeSlots(
    startHour: Int,
    endHour: Int,
    intervalMinutes: Int
): List<String> {
    val list = mutableListOf<String>()

    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, startHour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, endHour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val formatter = SimpleDateFormat("H:mm", Locale.ENGLISH)

    while (cal.timeInMillis <= endCal.timeInMillis) {
        list.add(formatter.format(cal.time))
        cal.add(Calendar.MINUTE, intervalMinutes)
    }

    return list
}
