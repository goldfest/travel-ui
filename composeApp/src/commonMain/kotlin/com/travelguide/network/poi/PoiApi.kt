package com.travelguide.network.poi

import com.travelguide.network.dto.common.PageResponseDto
import com.travelguide.network.dto.poi.PoiResponseDto
import com.travelguide.network.dto.poi.PoiSearchRequestDto
import com.travelguide.network.dto.poi.PoiTypeResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PoiApi(
    private val client: HttpClient,
    private val baseUrl: String // http://10.0.2.2:8081/api/poi
) {

    suspend fun getPoiById(id: Long): PoiResponseDto {
        return client.get("$baseUrl/pois/$id").body()
    }

    suspend fun getPoisByCity(
        cityId: Long,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "name",
        sortDirection: String = "ASC"
    ): PageResponseDto<PoiResponseDto> {
        return client.get("$baseUrl/pois/city/$cityId") {
            parameter("page", page)
            parameter("size", size)
            parameter("sortBy", sortBy)
            parameter("sortDirection", sortDirection)
        }.body()
    }

    suspend fun searchPois(
        request: PoiSearchRequestDto
    ): PageResponseDto<PoiResponseDto> {
        return client.post("$baseUrl/pois/search") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getAllPoiTypes(): List<PoiTypeResponseDto> {
        return client.get("$baseUrl/poi-types/all").body()
    }
}