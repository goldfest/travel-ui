package com.travelguide.domain.models

data class User(
    val id: Int,
    val email: String,
    val phone: String? = null,
    val username: String,
    val avatarUrl: String? = null,
    val isBlocked: Boolean = false,
    val role: String = "USER", // USER, ADMIN, MODERATOR
    val status: String = "ACTIVE",
    val homeCityId: Int? = null
) {
    fun isAdmin(): Boolean = role == "ADMIN"
    fun isModerator(): Boolean = role == "MODERATOR"
}