package com.travelguide.personalisation

import com.travelguide.domain.models.Collection
import com.travelguide.domain.models.POI
import com.travelguide.network.dto.personalization.CollectionRequestDto
import com.travelguide.network.dto.personalization.CollectionResponseDto
import com.travelguide.network.personalization.CollectionApi
import com.travelguide.poi.PoiRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CollectionRepository(
    private val api: CollectionApi,
    private val poiRepository: PoiRepository
) {
    suspend fun getCollections(
        page: Int = 0,
        size: Int = 20
    ): List<Collection> {
        return api.getCollections(page = page, size = size)
            .content
            .map { it.toDomain() }
    }

    suspend fun getCollection(collectionId: Int): Collection {
        return api.getCollection(collectionId.toLong()).toDomain()
    }

    suspend fun createCollection(
        name: String,
        description: String?,
        coverUrl: String? = null
    ): Collection {
        return api.createCollection(
            CollectionRequestDto(
                name = name,
                description = description,
                coverUrl = coverUrl
            )
        ).toDomain()
    }

    suspend fun updateCollection(
        collectionId: Int,
        name: String,
        description: String?,
        coverUrl: String? = null
    ): Collection {
        return api.updateCollection(
            collectionId = collectionId.toLong(),
            request = CollectionRequestDto(
                name = name,
                description = description,
                coverUrl = coverUrl
            )
        ).toDomain()
    }

    suspend fun deleteCollection(collectionId: Int) {
        api.deleteCollection(collectionId.toLong())
    }

    suspend fun searchCollections(query: String): List<Collection> {
        return api.searchCollections(query).map { it.toDomain() }
    }

    suspend fun addPoiToCollection(
        collectionId: Int,
        poiId: Int,
        orderIndex: Int? = null
    ) {
        api.addPoiToCollection(
            collectionId = collectionId.toLong(),
            poiId = poiId.toLong(),
            orderIndex = orderIndex
        )
    }

    suspend fun removePoiFromCollection(
        collectionId: Int,
        poiId: Int
    ) {
        api.removePoiFromCollection(
            collectionId = collectionId.toLong(),
            poiId = poiId.toLong()
        )
    }

    suspend fun getCollectionPois(
        collectionId: Int,
        page: Int = 0,
        size: Int = 50
    ): List<POI> = coroutineScope {
        val poiIds = api.getCollectionPois(
            collectionId = collectionId.toLong(),
            page = page,
            size = size
        ).content

        poiIds.map { poiId ->
            async {
                runCatching { poiRepository.getPoiById(poiId.toInt()) }.getOrNull()
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun getCollectionPoiCount(collectionId: Int): Long {
        return api.getCollectionPoiCount(collectionId.toLong())
    }
}

private fun CollectionResponseDto.toDomain(): Collection {
    return Collection(
        id = id.toInt(),
        name = name,
        description = description,
        coverUrl = coverUrl,
        userId = userId.toInt(),
        poiCount = poiCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}