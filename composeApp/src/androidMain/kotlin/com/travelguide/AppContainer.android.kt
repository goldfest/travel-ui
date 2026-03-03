package com.travelguide

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.travelguide.auth.AuthRepository
import com.travelguide.auth.TokenStorage
import com.travelguide.network.HttpClientFactory
import com.travelguide.network.auth.AuthApi
import com.travelguide.network.user.UserApi
import com.travelguide.profile.UserRepository
import com.travelguide.session.SessionManager

class AppContainer(context: Context) {

    val sessionManager = SessionManager()
    private val settings: Settings = SharedPreferencesSettings(
        context.getSharedPreferences("travelguide_settings", Context.MODE_PRIVATE)
    )

    // Backend на твоем ПК, Android Emulator:
    // AppContainer.kt
    private val hostUrl = "http://10.0.2.2:8084"
    private val apiBaseUrl = "$hostUrl/api"

    val tokenStorage = TokenStorage(settings)

    private val httpClient = HttpClientFactory().create(apiBaseUrl, tokenStorage)

    private val authApi = AuthApi(httpClient, apiBaseUrl)
    val authRepository = AuthRepository(authApi, tokenStorage)

    private val userApi = UserApi(httpClient, apiBaseUrl)
    val userRepository = UserRepository(userApi, hostUrl)
}