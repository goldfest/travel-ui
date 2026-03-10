package com.travelguide.network.city

import com.travelguide.network.dto.city.CityResponseDto
import com.travelguide.network.dto.common.PageResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class CityApi(
    private val client: HttpClient,
    private val baseUrl: String // http://10.0.2.2:8082/api/cities
) {
    suspend fun getCities(
        page: Int = 0,
        size: Int = 20,
        sort: String = "name",
        direction: String = "ASC",
        isPopular: Boolean? = null
    ): PageResponseDto<CityResponseDto> {
        return client.get("$baseUrl/v1") {
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
            parameter("direction", direction)
            isPopular?.let { parameter("isPopular", it) }
        }.body()
    }

    suspend fun getPopularCities(): List<CityResponseDto> {
        return client.get("$baseUrl/v1/popular").body()
    }

    suspend fun getCityById(id: Long): CityResponseDto {
        return client.get("$baseUrl/v1/$id").body()
    }

    suspend fun searchCities(
        query: String,
        page: Int = 0,
        size: Int = 20,
        sort: String = "name",
        direction: String = "ASC"
    ): PageResponseDto<CityResponseDto> {
        return client.get("$baseUrl/v1/search") {
            parameter("query", query)
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
            parameter("direction", direction)
        }.body()
    }
}