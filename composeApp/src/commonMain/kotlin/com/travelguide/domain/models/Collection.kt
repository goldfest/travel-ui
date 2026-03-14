package com.travelguide.domain.models

data class Collection(
    val id: Int,
    val name: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val userId: Int,
    val poiCount: Int = 0,
    val pois: List<POI> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Favorite(
    val id: Int,
    val poiId: Int,
    val userId: Int,
    val createdAt: String = "",
    val poi: POI? = null
)

data class SearchHistoryItem(
    val id: Int,
    val queryText: String? = null,
    val filtersJson: String? = null,
    val userId: Int,
    val cityId: Int? = null,
    val presetFilterId: Int? = null,
    val searchedAt: String? = null
)

data class PresetFilter(
    val id: Int,
    val name: String,
    val filtersJson: String,
    val userId: Int,
    val cityId: Int,
    val poiTypeId: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null
)