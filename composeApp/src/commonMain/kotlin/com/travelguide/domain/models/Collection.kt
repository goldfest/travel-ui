package com.travelguide.domain.models

data class Collection(
    val id: Int,
    val name: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val userId: Int,
    val pois: List<POI> = emptyList()
)

data class Favorite(
    val id: Int,
    val poiId: Int,
    val userId: Int,
    val createdAt: String = "",
    val poi: POI? = null
)