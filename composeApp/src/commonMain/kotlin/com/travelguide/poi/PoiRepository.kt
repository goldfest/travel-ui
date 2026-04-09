package com.travelguide.poi

import com.travelguide.domain.models.Feature
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.POIType
import com.travelguide.domain.models.PoiWorkingHours
import com.travelguide.network.dto.poi.PoiResponseDto
import com.travelguide.network.dto.poi.PoiSearchRequestDto
import com.travelguide.network.dto.poi.PoiTypeResponseDto
import com.travelguide.network.poi.PoiApi
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class PoiRepository(
    private val api: PoiApi
) {

    suspend fun getPoisByCity(
        cityId: Int,
        page: Int = 0,
        size: Int = 50,
        sortBy: String = "name",
        sortDirection: String = "ASC"
    ): List<POI> {
        return api.getPoisByCity(
            cityId = cityId.toLong(),
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        ).content.map { it.toDomain() }
    }

    suspend fun getPoiById(id: Int): POI {
        return api.getPoiById(id.toLong()).toDomain()
    }

    suspend fun searchPois(
        cityId: Int,
        query: String? = null,
        poiTypeIds: List<Int> = emptyList(),
        minRating: Double? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        verifiedOnly: Boolean = true,
        excludeClosed: Boolean = true,
        page: Int = 1,
        size: Int = 50,
        sortBy: String = "name",
        sortDirection: String = "ASC"
    ): List<POI> {
        val request = PoiSearchRequestDto(
            cityId = cityId.toLong(),
            searchQuery = query?.takeIf { it.isNotBlank() },
            poiTypeIds = poiTypeIds.takeIf { it.isNotEmpty() }?.map { it.toLong() },
            minRating = minRating,
            minPrice = minPrice,
            maxPrice = maxPrice,
            verifiedOnly = verifiedOnly,
            excludeClosed = excludeClosed,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        return api.searchPois(request).content.map { it.toDomain() }
    }

    suspend fun getPoiTypes(): List<POIType> {
        return api.getAllPoiTypes().map { it.toDomain() }
    }
}

private fun PoiResponseDto.toDomain(): POI {
    val mappedType = poiType?.let {
        POIType(
            id = it.id.toInt(),
            code = it.code,
            name = it.name,
            icon = it.icon ?: "📍"
        )
    }

    return POI(
        id = id.toInt(),
        name = name,
        description = description,
        address = address,
        latitude = latitude,
        longitude = longitude,
        priceLevel = priceLevel,
        cityId = cityId.toInt(),
        poiTypeId = poiType?.id?.toInt() ?: 0,
        poiType = mappedType,
        tags = tags.toTagList(),
        images = media?.mapNotNull { it.url.takeIf(String::isNotBlank) } ?: emptyList(),
        features = features
            ?.map { Feature(key = it.key, value = it.value) }
            ?: emptyList(),
        hours = hours
            ?.map {
                PoiWorkingHours(
                    dayOfWeek = it.dayOfWeek,
                    openTime = it.openTime,
                    closeTime = it.closeTime,
                    aroundTheClock = it.aroundTheClock == true,
                    isToday = it.isToday == true
                )
            }
            ?: emptyList()
    )
}

private fun PoiTypeResponseDto.toDomain(): POIType {
    return POIType(
        id = id.toInt(),
        code = code,
        name = name,
        icon = icon ?: "📍"
    )
}

private fun JsonElement?.toTagList(): List<String> {
    val array = this as? JsonArray ?: return emptyList()

    return array.mapNotNull { element ->
        val primitive = element as? JsonPrimitive
        primitive?.contentOrNull?.takeIf { it.isNotBlank() }
    }
}
