package com.example.classroominformer.data

import retrofit2.http.Body
import retrofit2.http.POST

// ---------- DTO들 ----------

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String? = null
)

data class SignupResponse(
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    val access_token: String,
    val refresh_token: String,
    val user_id: String
)

// ---------- Retrofit API ----------

interface AuthApi {

    @POST("auth/signup")
    suspend fun signup(
        @Body body: SignupRequest
    ): SignupResponse

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): TokenResponse
}
