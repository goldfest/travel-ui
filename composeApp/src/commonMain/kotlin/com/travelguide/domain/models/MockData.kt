package com.travelguide.data.mock

import com.travelguide.domain.models.*

object MockData {
    val cities = listOf(
        City(
            id = 1,
            name = "Москва",
            country = "Россия",
            description = "Столица России с богатой историей и архитектурой. Красная площадь, Кремль и множество музеев.",
            isPopular = true,
            countryCode = "RU"
        ),
        City(
            id = 2,
            name = "Санкт-Петербург",
            country = "Россия",
            description = "Культурная столица России с дворцами и каналами. Эрмитаж и разводные мосты.",
            isPopular = true,
            countryCode = "RU"
        ),
        City(
            id = 3,
            name = "Казань",
            country = "Россия",
            description = "Город где встречаются Европа и Азия. Казанский кремль и мечеть Кул-Шариф.",
            isPopular = true,
            countryCode = "RU"
        ),
        City(
            id = 4,
            name = "Сочи",
            country = "Россия",
            description = "Курортный город на Черном море. Горные лыжи зимой и пляжи летом.",
            isPopular = false,
            countryCode = "RU"
        ),
        City(
            id = 5,
            name = "Екатеринбург",
            country = "Россия",
            description = "Столица Урала с промышленной историей. Храм на Крови и небоскреб Высоцкий.",
            isPopular = false,
            countryCode = "RU"
        )
    )

    // Используй исправленный конструктор POI:
    val pois = listOf(
        POI(
            id = 1,
            name = "Кремль",
            description = "Исторический центр",
            cityId = 1,
            poiTypeId = 1,
            averageRating = 4.8f,
            ratingCount = 1245,
            priceLevel = 0,
            tags = listOf("история", "архитектура"),
            images = listOf("https://example.com/kremlin.jpg"),
            features = listOf(
                Feature("wifi", "free"),
                Feature("wheelchair_accessible", "yes")
            )
        )
    )
}