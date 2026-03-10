package com.travelguide.network.dto.poi

import kotlinx.serialization.Serializable

@Serializable
data class PoiSearchRequestDto(
    val cityId: Long,
    val searchQuery: String? = null,
    val poiTypeIds: List<Long>? = null,
    val minRating: Double? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val verifiedOnly: Boolean = true,
    val excludeClosed: Boolean = true,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val radiusKm: Int? = null,
    val features: List<String>? = null,
    val page: Int = 1,
    val size: Int = 20,
    val sortBy: String = "name",
    val sortDirection: String = "ASC"
)