package com.travelguide

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.travelguide.auth.AuthRepository
import com.travelguide.auth.TokenStorage
import com.travelguide.network.HttpClientFactory
import com.travelguide.network.auth.AuthApi

class AppContainer(context: Context) {

    private val settings: Settings = SharedPreferencesSettings(
        context.getSharedPreferences("travelguide_settings", Context.MODE_PRIVATE)
    )

    private val httpClient = HttpClientFactory().create()

    // Backend на твоем ПК, Android Emulator:
    private val baseUrl = "http://10.0.2.2:8084/api"

    val tokenStorage = TokenStorage(settings)
    private val authApi = AuthApi(httpClient, baseUrl)

    val authRepository = AuthRepository(authApi, tokenStorage)
}