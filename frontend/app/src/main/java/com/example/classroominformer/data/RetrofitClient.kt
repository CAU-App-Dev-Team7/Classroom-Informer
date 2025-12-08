package com.example.classroominformer.data

import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        AuthManager.accessToken?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}

object RetrofitClient {

    private const val BASE_URL = "http://YOUR_BACKEND_URL"

    private val okHttp = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // --- login/signup ---
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    // --- timetable / free-slots ---
    val infoApi: InfoApi by lazy {
        retrofit.create(InfoApi::class.java)
    }

    // --- favorites ---
    val favoritesApi: FavoritesApi by lazy {
        retrofit.create(FavoritesApi::class.java)
    }
}
