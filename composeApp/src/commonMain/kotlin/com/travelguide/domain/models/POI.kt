package com.travelguide.domain.models

data class POI(
    val id: Int,
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val priceLevel: Int? = null,
    val averageRating: Float? = null,
    val ratingCount: Int = 0,
    val cityId: Int,
    val poiTypeId: Int,
    val poiType: POIType? = null,
    // Добавь недостающие поля:
    val tags: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val features: List<Feature> = emptyList()
) {
    fun hasRating(): Boolean = averageRating != null && averageRating > 0
    fun ratingFormatted(): String = "${averageRating ?: 0f}"
    fun priceLevelText(): String = when (priceLevel) {
        0 -> "Бесплатно"
        1 -> "Низкие цены"
        2 -> "Средние цены"
        3 -> "Выше среднего"
        4 -> "Высокие цены"
        else -> "Цена неизвестна"
    }
}

data class Feature(
    val key: String,
    val value: String
)

// Создай объект POITypes:
object POITypes {
    val ATTRACTION = POIType(1, "attraction", "Достопримечательность", "🏛️")
    val RESTAURANT = POIType(2, "restaurant", "Ресторан", "🍽️")
    val HOTEL = POIType(3, "hotel", "Отель", "🏨")
    val TOILET = POIType(4, "toilet", "Туалет", "🚻")
}

data class POIType(
    val id: Int,
    val code: String,
    val name: String,
    val icon: String
)