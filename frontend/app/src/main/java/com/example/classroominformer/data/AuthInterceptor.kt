package com.example.classroominformer.data

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // If no token, continue the request normally
        val token = AuthManager.accessToken
        if (token.isNullOrBlank()) {
            return chain.proceed(request)
        }

        // Attach Authorization header
        val newRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}
