package com.example.classroominformer.data

object AuthManager {
    var accessToken: String? = null
    var refreshToken: String? = null
    var userId: String? = null

    fun saveAuth(access: String?, refresh: String?, userIdValue: String?) {
        accessToken = access
        refreshToken = refresh
        userId = userIdValue
    }

    fun clear() {
        accessToken = null
        refreshToken = null
        userId = null
    }
}
