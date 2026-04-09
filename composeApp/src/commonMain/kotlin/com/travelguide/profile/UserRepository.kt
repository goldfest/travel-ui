package com.travelguide.profile

import com.travelguide.domain.models.User
import com.travelguide.network.dto.ChangePasswordRequestDto
import com.travelguide.network.dto.UpdateProfileRequestDto
import com.travelguide.network.dto.UserResponseDto
import com.travelguide.network.user.UserApi

class UserRepository(
    private val api: UserApi,
    private val hostUrl: String,
    private val userCache: UserCache? = null
) {
    private fun resolveAvatarUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
        return hostUrl.trimEnd('/') + raw
    }

    private fun mapUser(it: UserResponseDto): User {
        return User(
            id = it.id,
            email = it.email,
            username = it.username,
            phone = it.phone,
            avatarUrl = resolveAvatarUrl(it.avatarUrl),
            role = it.role,
            status = it.status,
            isBlocked = it.isBlocked,
            homeCityId = it.homeCityId
        )
    }

    suspend fun getMe(): User {
        return mapUser(api.getMe()).also { user -> userCache?.save(user) }
    }

    fun getCachedMe(): User? = userCache?.get()

    suspend fun updateMe(
        username: String? = null,
        phone: String? = null,
        avatarUrl: String? = null,
        homeCityId: Long? = null,
        preferencesJson: String? = null
    ): User {
        return mapUser(
            api.updateMe(
                UpdateProfileRequestDto(
                    username = username,
                    phone = phone,
                    avatarUrl = avatarUrl,
                    homeCityId = homeCityId,
                    preferencesJson = preferencesJson
                )
            )
        ).also { user -> userCache?.save(user) }
    }

    fun updateCachedProfile(
        username: String,
        phone: String?,
        avatarUrl: String?,
        homeCityId: Long?
    ): User? = userCache?.updateLocalProfile(username, phone, avatarUrl, homeCityId)

    suspend fun changePassword(current: String, new: String) {
        api.changePassword(ChangePasswordRequestDto(currentPassword = current, newPassword = new))
    }

    suspend fun deleteMe() {
        api.deleteMe()
        userCache?.clear()
    }

    suspend fun uploadAvatar(bytes: ByteArray, mimeType: String): User {
        return mapUser(api.uploadAvatar(bytes, mimeType)).also { user -> userCache?.save(user) }
    }
}
