package com.travelguide.domain.models

data class PoiCardUiModel(
    val poi: POI,
    val isFavorite: Boolean = false,
    val averageRating: Double? = null,
    val reviewCount: Long = 0
)