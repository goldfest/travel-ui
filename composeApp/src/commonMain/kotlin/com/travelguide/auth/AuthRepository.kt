package com.travelguide.auth

import com.travelguide.domain.models.User
import com.travelguide.network.auth.AuthApi

class AuthRepository(
    private val api: AuthApi,
    private val storage: TokenStorage
) {
    suspend fun login(email: String, password: String): User? {
        val resp = api.login(email, password)
        storage.accessToken = resp.accessToken
        storage.refreshToken = resp.refreshToken

        return resp.user?.let {
            User(
                id = it.id.toInt(),
                email = it.email,
                username = it.username,
                phone = it.phone,
                avatarUrl = it.avatarUrl,
                isBlocked = it.isBlocked,
                role = it.role,
                status = it.status,
                homeCityId = it.homeCityId?.toInt()
            )
        }
    }

    suspend fun register(email: String, username: String, password: String, phone: String?): User? {
        val resp = api.register(email, username, password, phone)
        storage.accessToken = resp.accessToken
        storage.refreshToken = resp.refreshToken

        return resp.user?.let {
            User(
                id = it.id.toInt(),
                email = it.email,
                username = it.username,
                phone = it.phone,
                avatarUrl = it.avatarUrl,
                isBlocked = it.isBlocked,
                role = it.role,
                status = it.status,
                homeCityId = it.homeCityId?.toInt()
            )
        }
    }

    fun isLoggedIn(): Boolean = !storage.accessToken.isNullOrBlank()

    fun logout() {
        storage.clear()
    }
}