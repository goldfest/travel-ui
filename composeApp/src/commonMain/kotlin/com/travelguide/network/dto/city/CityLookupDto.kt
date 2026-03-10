package com.travelguide.network.dto.city

import kotlinx.serialization.Serializable

@Serializable
data class CityLookupDto(
    val id: Long,
    val name: String,
    val country: String? = null,
    val slug: String? = null,
    val isPopular: Boolean = false,
    val countryCode: String? = null
)