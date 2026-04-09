package com.travelguide.profile

import com.russhwolf.settings.Settings
import com.travelguide.domain.models.User

class UserPreferencesCache(
    settings: Settings
) {
    private val delegate = UserCache(settings)

    fun get(): User? = delegate.get()

    fun save(user: User) {
        delegate.save(user)
    }

    fun clear() {
        delegate.clear()
    }
}
