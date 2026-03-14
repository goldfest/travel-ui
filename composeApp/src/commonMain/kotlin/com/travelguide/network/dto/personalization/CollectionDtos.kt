package com.travelguide.network.dto.personalization

import kotlinx.serialization.Serializable

@Serializable
data class CollectionRequestDto(
    val name: String,
    val description: String? = null,
    val coverUrl: String? = null
)

@Serializable
data class CollectionPoiRequestDto(
    val poiId: Long,
    val orderIndex: Int? = null
)

@Serializable
data class CollectionResponseDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val userId: Long,
    val poiCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)