package com.example.classroominformer.data

// One free slot (e.g. 09:00 ~ 10:00)
data class FreeSlot(
    val start: String,   // "HH:MM"
    val end: String      // "HH:MM"
)

// Whole response for one room
data class RoomFreeSlotsResponse(
    val building_code: String,
    val room_number: String,
    val free_slots_by_day: Map<String, List<FreeSlot>>
    // ex: { "월": [ { "start":"09:00","end":"10:00" }, ... ], "화": [] }
)
