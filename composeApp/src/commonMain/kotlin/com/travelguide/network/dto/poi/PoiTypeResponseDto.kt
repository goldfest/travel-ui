package com.travelguide.network.dto.poi

import kotlinx.serialization.Serializable

@Serializable
data class PoiTypeResponseDto(
    val id: Long,
    val code: String,
    val name: String,
    val icon: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)