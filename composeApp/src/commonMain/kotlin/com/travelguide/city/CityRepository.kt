package com.travelguide.city

import com.travelguide.domain.models.City
import com.travelguide.network.city.CityApi
import com.travelguide.network.dto.city.CityResponseDto

class CityRepository(
    private val api: CityApi
) {
    suspend fun getCities(
        page: Int = 0,
        size: Int = 20,
        sort: String = "name",
        direction: String = "ASC",
        isPopular: Boolean? = null
    ): List<City> {
        return api.getCities(
            page = page,
            size = size,
            sort = sort,
            direction = direction,
            isPopular = isPopular
        ).content.map { it.toDomain() }
    }

    suspend fun getPopularCities(): List<City> {
        return api.getPopularCities().map { it.toDomain() }
    }

    suspend fun getCityById(id: Int): City {
        return api.getCityById(id.toLong()).toDomain()
    }

    suspend fun searchCities(
        query: String,
        page: Int = 0,
        size: Int = 20,
        sort: String = "name",
        direction: String = "ASC"
    ): List<City> {
        return api.searchCities(
            query = query,
            page = page,
            size = size,
            sort = sort,
            direction = direction
        ).content.map { it.toDomain() }
    }
}

private fun CityResponseDto.toDomain(): City {
    return City(
        id = id.toInt(),
        name = name,
        country = country,
        description = description,
        centerLat = centerLat,
        centerLng = centerLng,
        isPopular = isPopular,
        slug = slug,
        countryCode = countryCode,
        imageUrl = imageUrl,
        imageUrls = imageUrls
    )
}