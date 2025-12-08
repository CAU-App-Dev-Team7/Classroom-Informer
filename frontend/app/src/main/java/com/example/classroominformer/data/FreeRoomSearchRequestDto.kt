package com.example.classroominformer.data

data class FreeRoomSearchRequestDto(
    val day: String,          // ex: "MON"
    val periods: List<String> // ex: ["Period 1 (09:00-09:30)", "Period 2 (10:00-10:30)"]
)
