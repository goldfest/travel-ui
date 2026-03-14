package com.travelguide.network.personalization

import com.travelguide.network.dto.common.PageResponseDto
import com.travelguide.network.dto.personalization.FavoriteRequestDto
import com.travelguide.network.dto.personalization.FavoriteResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class FavoriteApi(
    private val client: HttpClient,
    private val baseUrl: String // http://10.0.2.2:8085/api/personalization
) {
    suspend fun getFavorites(
        page: Int = 0,
        size: Int = 20
    ): PageResponseDto<FavoriteResponseDto> {
        return client.get("$baseUrl/v1/favorites") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun addToFavorites(poiId: Long): FavoriteResponseDto {
        return client.post("$baseUrl/v1/favorites") {
            contentType(ContentType.Application.Json)
            setBody(FavoriteRequestDto(poiId))
        }.body()
    }

    suspend fun removeFromFavorites(poiId: Long) {
        client.delete("$baseUrl/v1/favorites/$poiId")
    }

    suspend fun isFavorite(poiId: Long): Boolean {
        return client.get("$baseUrl/v1/favorites/check/$poiId").body()
    }

    suspend fun getFavoriteCount(): Long {
        return client.get("$baseUrl/v1/favorites/count").body()
    }

    suspend fun deleteAllFavorites() {
        client.delete("$baseUrl/v1/favorites")
    }
}