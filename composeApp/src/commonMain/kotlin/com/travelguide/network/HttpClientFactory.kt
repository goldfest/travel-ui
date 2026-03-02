package com.travelguide.network

import io.ktor.client.HttpClient

expect class HttpClientFactory {
    fun create(): HttpClient
}