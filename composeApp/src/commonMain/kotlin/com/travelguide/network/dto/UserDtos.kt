package com.travelguide.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    val id: Long,
    val email: String,
    val username: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val role: String = "USER",
    val status: String = "ACTIVE",
    @SerialName("isBlocked") val isBlocked: Boolean = false,
    val homeCityId: Long? = null,
    val preferencesJson: String? = null
)

@Serializable
data class UpdateProfileRequestDto(
    val username: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val homeCityId: Long? = null,
    val preferencesJson: String? = null
)

@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)