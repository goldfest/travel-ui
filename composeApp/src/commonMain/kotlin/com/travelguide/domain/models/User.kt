package com.travelguide.domain.models

data class User(
    val id: Long,
    val email: String,
    val username: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val role: String = "USER",
    val status: String = "ACTIVE",
    val isBlocked: Boolean = false,
    val homeCityId: Long? = null
) {
    fun isAdmin() = role == "ADMIN"
    fun isModerator() = role == "MODERATOR"
}