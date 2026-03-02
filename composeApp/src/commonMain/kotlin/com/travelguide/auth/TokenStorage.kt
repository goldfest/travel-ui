package com.travelguide.auth

import com.russhwolf.settings.Settings

class TokenStorage(private val settings: Settings) {
    var accessToken: String?
        get() = settings.getStringOrNull(KEY_ACCESS)
        set(value) {
            if (value == null) settings.remove(KEY_ACCESS) else settings.putString(KEY_ACCESS, value)
        }

    var refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH)
        set(value) {
            if (value == null) settings.remove(KEY_REFRESH) else settings.putString(KEY_REFRESH, value)
        }

    fun clear() {
        settings.remove(KEY_ACCESS)
        settings.remove(KEY_REFRESH)
    }

    companion object {
        private const val KEY_ACCESS = "auth.accessToken"
        private const val KEY_REFRESH = "auth.refreshToken"
    }
}