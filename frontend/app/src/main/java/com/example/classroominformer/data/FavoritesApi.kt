package com.example.classroominformer.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FavoritesApi {

    // GET /favorites  ->  List[FavoriteResponse]
    @GET("favorites")
    suspend fun getMyFavorites(): List<FavoriteResponseDto>

    // POST /favorites/toggle  ->  {"status": "...", "message": "..."}
    @POST("favorites/toggle")
    suspend fun toggleFavorite(
        @Body body: FavoriteToggleRequest
    )
}
