package com.travelguide.network

import com.travelguide.auth.TokenStorage
import com.travelguide.network.auth.AuthApi
import io.ktor.client.HttpClient

expect class HttpClientFactory() {
    fun create(baseUrl: String, storage: TokenStorage): HttpClient
}