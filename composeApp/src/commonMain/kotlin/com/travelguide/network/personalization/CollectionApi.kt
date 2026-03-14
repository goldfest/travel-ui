package com.travelguide.network.personalization

import com.travelguide.network.dto.common.PageResponseDto
import com.travelguide.network.dto.personalization.CollectionPoiRequestDto
import com.travelguide.network.dto.personalization.CollectionRequestDto
import com.travelguide.network.dto.personalization.CollectionResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class CollectionApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getCollections(
        page: Int = 0,
        size: Int = 20
    ): PageResponseDto<CollectionResponseDto> {
        return client.get("$baseUrl/v1/collections") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getCollection(collectionId: Long): CollectionResponseDto {
        return client.get("$baseUrl/v1/collections/$collectionId").body()
    }

    suspend fun createCollection(
        request: CollectionRequestDto
    ): CollectionResponseDto {
        return client.post("$baseUrl/v1/collections") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateCollection(
        collectionId: Long,
        request: CollectionRequestDto
    ): CollectionResponseDto {
        return client.put("$baseUrl/v1/collections/$collectionId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteCollection(collectionId: Long) {
        client.delete("$baseUrl/v1/collections/$collectionId")
    }

    suspend fun searchCollections(query: String): List<CollectionResponseDto> {
        return client.get("$baseUrl/v1/collections/search") {
            parameter("query", query)
        }.body()
    }

    suspend fun addPoiToCollection(
        collectionId: Long,
        poiId: Long,
        orderIndex: Int? = null
    ) {
        client.post("$baseUrl/v1/collections/$collectionId/pois") {
            contentType(ContentType.Application.Json)
            setBody(CollectionPoiRequestDto(poiId = poiId, orderIndex = orderIndex))
        }
    }

    suspend fun removePoiFromCollection(
        collectionId: Long,
        poiId: Long
    ) {
        client.delete("$baseUrl/v1/collections/$collectionId/pois/$poiId")
    }

    suspend fun getCollectionPois(
        collectionId: Long,
        page: Int = 0,
        size: Int = 50
    ): PageResponseDto<Long> {
        return client.get("$baseUrl/v1/collections/$collectionId/pois") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getCollectionPoiCount(collectionId: Long): Long {
        return client.get("$baseUrl/v1/collections/$collectionId/pois/count").body()
    }
}