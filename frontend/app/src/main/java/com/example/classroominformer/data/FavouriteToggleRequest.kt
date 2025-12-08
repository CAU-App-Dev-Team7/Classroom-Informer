package com.example.classroominformer.data

/**
 * Request body for:
 *   POST /favorites/toggle
 *
 * Matches FastAPI:
 *   class FavoriteToggleRequest(BaseModel):
 *       room_id: int
 */
data class FavoriteToggleRequest(
    val room_id: Long
)
