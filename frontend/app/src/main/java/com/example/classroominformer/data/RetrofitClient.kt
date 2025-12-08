package com.example.classroominformer.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://13.209.181.240:8001/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // AUTH API (LOGIN / SIGNUP)
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    // INFO API (rooms, timetable)
    val infoApi: InfoApi by lazy {
        retrofit.create(InfoApi::class.java)
    }

    // FAVORITES API
    val favoritesApi: FavoritesApi by lazy {
        retrofit.create(FavoritesApi::class.java)
    }
}
