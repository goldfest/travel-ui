package com.travelguide.network.user

import com.travelguide.network.dto.ChangePasswordRequestDto
import com.travelguide.network.dto.UpdateProfileRequestDto
import com.travelguide.network.dto.UserResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class UserApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getMe(): UserResponseDto {
        return client.get("$baseUrl/users/me").body()
    }

    suspend fun updateMe(req: UpdateProfileRequestDto): UserResponseDto {
        return client.put("$baseUrl/users/me") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()
    }

    suspend fun changePassword(req: ChangePasswordRequestDto) {
        client.put("$baseUrl/users/me/password") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body<Unit>()
    }

    suspend fun deleteMe() {
        client.delete("$baseUrl/users/me").body<Unit>()
    }

    suspend fun uploadAvatar(bytes: ByteArray, mimeType: String): UserResponseDto {
        val safeMime = if (mimeType.startsWith("image/")) mimeType else "image/jpeg"
        val safeFileName = when (safeMime.lowercase()) {
            "image/png" -> "avatar.png"
            "image/webp" -> "avatar.webp"
            else -> "avatar.jpg"
        }

        return client.submitFormWithBinaryData(
            url = "$baseUrl/users/me/avatar",
            formData = formData {
                append(
                    key = "file",
                    value = bytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, safeMime)
                        append(HttpHeaders.ContentDisposition, "filename=\"$safeFileName\"")
                    }
                )
            }
        ).body()
    }
}