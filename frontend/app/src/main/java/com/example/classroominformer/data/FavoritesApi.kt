// app/src/main/java/com/example/classroominformer/data/FavoritesApi.kt
package com.example.classroominformer.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FavoritesApi {

    @GET("favorites")
    suspend fun getMyFavorites(): List<FavoriteResponseDto>

    @POST("favorites/toggle")
    suspend fun toggleFavorite(
        @Body req: FavoriteToggleRequest
    ): Map<String, String>  // { "status": "...", "message": "..." }
}
