package com.travelguide.network.dto.route

import kotlinx.serialization.Serializable

@Serializable
data class UpdateRouteRequestDto(
    val name: String? = null,
    val description: String? = null,
    val coverPhotoUrl: String? = null,
    val transportMode: String? = null,
    val distanceKm: Double? = null,
    val durationMin: Int? = null,
    val startPoint: String? = null,
    val endPoint: String? = null,
    val isOptimized: Boolean? = null,
    val optimizationMode: String? = null,
    val status: String? = null
)