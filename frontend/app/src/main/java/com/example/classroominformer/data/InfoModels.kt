package com.example.classroominformer.data

// For GET /info/rooms/available
data class AvailableRoomDto(
    val room_id: Int,
    val building_code: String,
    val room_number: String,
    val type: String? = null      // Lecture / Seminar ...
)

// One free slot interval
data class FreeSlotDto(
    val start: String,            // "09:00"
    val end: String               // "10:00"
)

// For GET /info/room/timetable/free-slots
data class FreeSlotsResponseDto(
    val building_code: String,
    val room_number: String,
    val free_slots_by_day: Map<String, List<FreeSlotDto>>
)
