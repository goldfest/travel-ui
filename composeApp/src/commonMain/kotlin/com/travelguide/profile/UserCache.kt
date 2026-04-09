package com.travelguide.profile

import com.russhwolf.settings.Settings
import com.travelguide.domain.models.User

class UserCache(
    private val settings: Settings
) {
    private val prefix = "user_cache."

    fun get(): User? {
        val id = settings.getLongOrNull("${prefix}id") ?: return null
        val email = settings.getStringOrNull("${prefix}email") ?: return null
        val username = settings.getStringOrNull("${prefix}username") ?: return null

        return User(
            id = id,
            email = email,
            username = username,
            phone = settings.getStringOrNull("${prefix}phone"),
            avatarUrl = settings.getStringOrNull("${prefix}avatarUrl"),
            role = settings.getStringOrNull("${prefix}role") ?: "USER",
            status = settings.getStringOrNull("${prefix}status") ?: "ACTIVE",
            isBlocked = settings.getBooleanOrNull("${prefix}isBlocked") ?: false,
            homeCityId = settings.getLongOrNull("${prefix}homeCityId")
        )
    }

    fun save(user: User) {
        settings.putLong("${prefix}id", user.id)
        settings.putString("${prefix}email", user.email)
        settings.putString("${prefix}username", user.username)
        putNullableString("${prefix}phone", user.phone)
        putNullableString("${prefix}avatarUrl", user.avatarUrl)
        settings.putString("${prefix}role", user.role)
        settings.putString("${prefix}status", user.status)
        settings.putBoolean("${prefix}isBlocked", user.isBlocked)
        putNullableLong("${prefix}homeCityId", user.homeCityId)
    }

    fun clear() {
        settings.remove("${prefix}id")
        settings.remove("${prefix}email")
        settings.remove("${prefix}username")
        settings.remove("${prefix}phone")
        settings.remove("${prefix}avatarUrl")
        settings.remove("${prefix}role")
        settings.remove("${prefix}status")
        settings.remove("${prefix}isBlocked")
        settings.remove("${prefix}homeCityId")
    }

    fun updateLocalProfile(
        username: String,
        phone: String?,
        avatarUrl: String?,
        homeCityId: Long?
    ): User? {
        val current = get() ?: return null
        val updated = current.copy(
            username = username,
            phone = phone,
            avatarUrl = avatarUrl ?: current.avatarUrl,
            homeCityId = homeCityId
        )
        save(updated)
        return updated
    }

    private fun putNullableString(key: String, value: String?) {
        if (value == null) settings.remove(key) else settings.putString(key, value)
    }

    private fun putNullableLong(key: String, value: Long?) {
        if (value == null) settings.remove(key) else settings.putLong(key, value)
    }
}
