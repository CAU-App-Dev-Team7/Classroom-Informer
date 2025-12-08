// FavoriteDtos.kt
package com.example.classroominformer.data

data class RoomDetailDto(
    val id: Long,
    val room_number: String,
    val building_code: String
)

data class FavoriteResponseDto(
    val user_id: String,
    val room_id: Long,
    val created_at: String,
    val room: RoomDetailDto? = null
)
 