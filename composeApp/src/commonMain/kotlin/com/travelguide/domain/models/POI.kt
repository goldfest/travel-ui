package com.travelguide.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class POI(
    val id: Int,
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val priceLevel: Int? = null,
    val cityId: Int,
    val poiTypeId: Int,
    val poiType: POIType? = null,
    val tags: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val features: List<Feature> = emptyList()
) {
    fun priceLevelText(): String = when (priceLevel) {
        0 -> "Бесплатно"
        1 -> "Низкие цены"
        2 -> "Средние цены"
        3 -> "Выше среднего"
        4 -> "Высокие цены"
        else -> "Цена неизвестна"
    }
}

@Serializable
data class Feature(
    val key: String,
    val value: String
)

@Serializable
data class POIType(
    val id: Int,
    val code: String,
    val name: String,
    val icon: String
)
