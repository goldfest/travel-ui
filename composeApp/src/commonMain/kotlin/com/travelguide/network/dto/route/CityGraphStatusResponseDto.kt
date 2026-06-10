package com.travelguide.network.dto.route

import kotlinx.serialization.Serializable

@Serializable
data class CityGraphStatusResponseDto(
    val cityId: Long,
    val downloaded: Boolean = false,
    val downloading: Boolean = false,
    val ready: Boolean = false,
    val status: String? = null,
    val progressPercent: Int = 0,
    val message: String? = null,
    val downloadRequested: Boolean = false
)
