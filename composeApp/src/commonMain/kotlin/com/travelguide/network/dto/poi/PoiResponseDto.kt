package com.travelguide.network.dto.poi

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PoiResponseDto(
    val id: Long,
    val name: String,
    val slug: String? = null,
    val tags: JsonElement? = null,
    val description: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val siteUrl: String? = null,
    val priceLevel: Int? = null,
    val averageRating: Double? = null,
    val ratingCount: Int? = null,
    val isVerified: Boolean? = null,
    val isClosed: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val poiType: PoiTypeShortDto? = null,
    val cityId: Long,
    val createdBy: Long? = null,
    val features: Map<String, String>? = null,
    val hours: List<PoiHoursDto>? = null,
    val media: List<PoiMediaDto>? = null,
    val sources: List<PoiSourceDto>? = null,
    val distanceKm: Double? = null,
    val isOpenNow: Boolean? = null,
    val currentStatus: String? = null
)

@Serializable
data class PoiTypeShortDto(
    val id: Long,
    val code: String,
    val name: String,
    val icon: String? = null
)

@Serializable
data class PoiHoursDto(
    val dayOfWeek: Int? = null,
    val openTime: String? = null,
    val closeTime: String? = null,
    val aroundTheClock: Boolean? = null,
    val isToday: Boolean? = null
)

@Serializable
data class PoiMediaDto(
    val id: Long? = null,
    val url: String,
    val mediaType: String? = null,
    val moderationStatus: String? = null,
    val createdAt: String? = null,
    val userId: Long? = null
)

@Serializable
data class PoiSourceDto(
    val id: Long? = null,
    val sourceCode: String? = null,
    val sourceUrl: String? = null,
    val confidenceScore: Double? = null,
    val createdAt: String? = null
)