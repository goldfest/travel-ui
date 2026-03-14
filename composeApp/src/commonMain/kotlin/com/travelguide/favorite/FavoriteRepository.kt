package com.travelguide.favorite

import com.travelguide.domain.models.Favorite
import com.travelguide.domain.models.POI
import com.travelguide.network.dto.personalization.FavoriteResponseDto
import com.travelguide.network.personalization.FavoriteApi
import com.travelguide.poi.PoiRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class FavoriteRepository(
    private val api: FavoriteApi,
    private val poiRepository: PoiRepository
) {
    suspend fun getFavorites(
        page: Int = 0,
        size: Int = 20
    ): List<Favorite> = coroutineScope {
        val favorites = api.getFavorites(page = page, size = size).content

        favorites.map { dto ->
            async {
                val poi = runCatching {
                    poiRepository.getPoiById(dto.poiId.toInt())
                }.getOrNull()

                dto.toDomain(poi)
            }
        }.awaitAll()
    }

    suspend fun addToFavorites(poiId: Int): Favorite {
        return api.addToFavorites(poiId.toLong()).toDomain(
            poi = runCatching { poiRepository.getPoiById(poiId) }.getOrNull()
        )
    }

    suspend fun removeFromFavorites(poiId: Int) {
        api.removeFromFavorites(poiId.toLong())
    }

    suspend fun isFavorite(poiId: Int): Boolean {
        return api.isFavorite(poiId.toLong())
    }

    suspend fun getFavoriteCount(): Long {
        return api.getFavoriteCount()
    }

    suspend fun deleteAllFavorites() {
        api.deleteAllFavorites()
    }
}

private fun FavoriteResponseDto.toDomain(poi: POI?): Favorite {
    return Favorite(
        id = id.toInt(),
        poiId = poiId.toInt(),
        userId = userId.toInt(),
        createdAt = createdAt.orEmpty(),
        poi = poi
    )
}