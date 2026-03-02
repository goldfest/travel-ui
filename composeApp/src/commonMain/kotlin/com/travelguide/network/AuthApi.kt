@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.travelguide.network.auth

import com.travelguide.network.dto.AuthResponseDto
import com.travelguide.network.dto.LoginRequestDto
import com.travelguide.network.dto.RefreshTokenRequestDto
import com.travelguide.network.dto.RegisterRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String // например: http://10.0.2.2:8084/api
) {
    suspend fun login(email: String, password: String): AuthResponseDto {
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email, password))
        }.body()
    }

    suspend fun register(email: String, username: String, password: String, phone: String?): AuthResponseDto {
        return client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(email, username, password, phone))
        }.body()
    }

    suspend fun refresh(refreshToken: String): AuthResponseDto {
        return client.post("$baseUrl/auth/refresh-token") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken))
        }.body()
    }
}