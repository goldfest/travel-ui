package com.travelguide.domain.models

data class City(
    val id: Int,
    val name: String,
    val country: String? = null,
    val description: String? = null,
    val centerLat: Double? = null,
    val centerLng: Double? = null,
    val isPopular: Boolean = false,
    val slug: String? = null,
    val countryCode: String? = null,
    val imageUrl: String? = null,
    val imageUrls: List<String> = emptyList()
)