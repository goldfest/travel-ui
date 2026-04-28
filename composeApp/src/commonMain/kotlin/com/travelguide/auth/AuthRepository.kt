package com.travelguide.auth

import com.travelguide.domain.models.User
import com.travelguide.network.auth.AuthApi
import com.travelguide.profile.UserCache

class AuthRepository(
    private val api: AuthApi,
    private val storage: TokenStorage,
    private val userCache: UserCache? = null
) {
    suspend fun login(email: String, password: String): User? {
        storage.clear()
        userCache?.clear()

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
            ).also { user -> userCache?.save(user) }
        }
    }

    suspend fun register(email: String, username: String, password: String, phone: String?): User? {
        storage.clear()
        userCache?.clear()

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
            ).also { user -> userCache?.save(user) }
        }
    }

    fun isLoggedIn(): Boolean = !storage.accessToken.isNullOrBlank()

    suspend fun logout() {
        try {
            val refresh = storage.refreshToken
            if (!refresh.isNullOrBlank()) {
                api.logout(refresh)
            }
        } catch (_: Exception) {
        } finally {
            storage.clear()
            userCache?.clear()
        }
    }
}
