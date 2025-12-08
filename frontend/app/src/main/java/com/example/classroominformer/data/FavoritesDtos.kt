// app/src/main/java/com/example/classroominformer/data/FavoritesDtos.kt
package com.example.classroominformer.data

// Detail info for joined room
data class RoomDetailDto(
    val id: Long,
    val room_number: String,
    val building_code: String
)

// Response for GET /favorites
data class FavoriteResponseDto(
    val user_id: String,
    val room_id: Long,
    val created_at: String,
    val room: RoomDetailDto?   // may be null
)

// Request for POST /favorites/toggle
data class FavoriteToggleRequest(
    val room_id: Long
)
