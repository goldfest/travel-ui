package com.travelguide.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val username: String,
    val password: String,
    val phone: String? = null
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String
)

@Serializable
data class UserDto(
    val id: Long,
    val email: String,
    val username: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val role: String = "USER",
    val status: String = "ACTIVE",
    val isBlocked: Boolean = false,
    val homeCityId: Long? = null
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long? = null,
    val user: UserDto? = null
)