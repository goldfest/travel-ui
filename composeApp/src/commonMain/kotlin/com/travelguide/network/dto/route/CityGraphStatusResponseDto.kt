package com.travelguide.network.dto.route

import kotlinx.serialization.Serializable

@Serializable
data class CityGraphStatusResponseDto(
    val cityId: Long,
    val ready: Boolean,
    val downloadRequested: Boolean = false,
    val message: String? = null
)
