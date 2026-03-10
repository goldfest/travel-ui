package com.travelguide.network.dto.city

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityResponseDto(
    val id: Long,
    val name: String,
    val country: String? = null,
    val description: String? = null,
    val centerLat: Double? = null,
    val centerLng: Double? = null,
    val isPopular: Boolean = false,
    val slug: String? = null,
    val countryCode: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)