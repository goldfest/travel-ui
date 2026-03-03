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
                id = it.id,
                email = it.email,
                username = it.username,
                phone = it.phone,
                avatarUrl = it.avatarUrl,
                isBlocked = it.isBlocked,
                role = it.role,
                status = it.status,
                homeCityId = it.homeCityId
            )
        }
    }

    suspend fun register(email: String, username: String, password: String, phone: String?): User? {
        val resp = api.register(email, username, password, phone)
        storage.accessToken = resp.accessToken
        storage.refreshToken = resp.refreshToken

        return resp.user?.let {
            User(
                id = it.id,
                email = it.email,
                username = it.username,
                phone = it.phone,
                avatarUrl = it.avatarUrl,
                isBlocked = it.isBlocked,
                role = it.role,
                status = it.status,
                homeCityId = it.homeCityId
            )
        }
    }

    fun isLoggedIn(): Boolean = !storage.accessToken.isNullOrBlank()

    suspend fun logout() {
        try {
            val refresh = storage.refreshToken
            if (!refresh.isNullOrBlank()) {
                api.logout(refresh)   // вызов backend
            }
        } catch (e: Exception) {
            // backend может быть недоступен — это не критично
        } finally {
            storage.clear()
        }
    }
}