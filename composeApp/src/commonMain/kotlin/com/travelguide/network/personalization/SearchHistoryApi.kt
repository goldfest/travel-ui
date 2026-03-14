package com.travelguide.network.personalization

import com.travelguide.network.dto.common.PageResponseDto
import com.travelguide.network.dto.personalization.SearchHistoryRequestDto
import com.travelguide.network.dto.personalization.SearchHistoryResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SearchHistoryApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun recordSearch(request: SearchHistoryRequestDto) {
        client.post("$baseUrl/v1/search-history") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getSearchHistory(
        page: Int = 0,
        size: Int = 20
    ): PageResponseDto<SearchHistoryResponseDto> {
        return client.get("$baseUrl/v1/search-history") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getCitySearchHistory(
        cityId: Long,
        page: Int = 0,
        size: Int = 20
    ): PageResponseDto<SearchHistoryResponseDto> {
        return client.get("$baseUrl/v1/search-history/city/$cityId") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getRecentQueries(limit: Int = 10): List<String> {
        return client.get("$baseUrl/v1/search-history/recent-queries") {
            parameter("limit", limit)
        }.body()
    }

    suspend fun clearHistory() {
        client.delete("$baseUrl/v1/search-history")
    }
}