package com.travelguide.profile

import com.travelguide.domain.models.User
import com.travelguide.network.dto.ChangePasswordRequestDto
import com.travelguide.network.dto.UpdateProfileRequestDto
import com.travelguide.network.dto.UserResponseDto
import com.travelguide.network.user.UserApi

class UserRepository(
    private val api: UserApi,
    private val hostUrl: String // например: http://10.0.2.2:8084
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
        val it = api.getMe()
        return mapUser(it)
    }

    suspend fun updateMe(
        username: String? = null,
        phone: String? = null,
        avatarUrl: String? = null,
        homeCityId: Long? = null,
        preferencesJson: String? = null
    ): User {
        val it = api.updateMe(
            UpdateProfileRequestDto(
                username = username,
                phone = phone,
                avatarUrl = avatarUrl,
                homeCityId = homeCityId,
                preferencesJson = preferencesJson
            )
        )
        return mapUser(it)
    }

    suspend fun changePassword(current: String, new: String) {
        api.changePassword(ChangePasswordRequestDto(currentPassword = current, newPassword = new))
    }

    suspend fun deleteMe() {
        api.deleteMe()
    }


    suspend fun uploadAvatar(bytes: ByteArray, mimeType: String): User {
        val it = api.uploadAvatar(bytes, mimeType)
        return mapUser(it)
    }
}