package com.travelguide.network.dto.personalization

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteRequestDto(
    val poiId: Long
)

@Serializable
data class FavoriteResponseDto(
    val id: Long,
    val userId: Long,
    val poiId: Long,
    val createdAt: String? = null
)