package com.travelguide

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.travelguide.auth.AuthRepository
import com.travelguide.auth.TokenStorage
import com.travelguide.city.CityRepository
import com.travelguide.network.HttpClientFactory
import com.travelguide.network.auth.AuthApi
import com.travelguide.network.city.CityApi
import com.travelguide.network.user.UserApi
import com.travelguide.profile.UserRepository
import com.travelguide.session.SessionManager

class AppContainer(context: Context) {

    val sessionManager = SessionManager()
    private val settings: Settings = SharedPreferencesSettings(
        context.getSharedPreferences("travelguide_settings", Context.MODE_PRIVATE)
    )

    private val authHostUrl = "http://10.0.2.2:8084"
    private val authApiBaseUrl = "$authHostUrl/api"

    private val cityBaseUrl = "http://10.0.2.2:8082/api/cities"

    val tokenStorage = TokenStorage(settings)

    private val httpClient = HttpClientFactory().create(authApiBaseUrl, tokenStorage)

    private val authApi = AuthApi(httpClient, authApiBaseUrl)
    val authRepository = AuthRepository(authApi, tokenStorage)

    private val userApi = UserApi(httpClient, authApiBaseUrl)
    val userRepository = UserRepository(userApi, authHostUrl)

    private val cityApi = CityApi(httpClient, cityBaseUrl)
    val cityRepository = CityRepository(cityApi)
}