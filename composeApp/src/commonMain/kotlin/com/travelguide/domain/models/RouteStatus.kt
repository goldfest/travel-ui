package com.travelguide.domain.models

enum class RouteStatus {
    DRAFT,
    READY,
    ARCHIVED;

    fun label(): String = when (this) {
        DRAFT -> "Черновик"
        READY -> "Готов"
        ARCHIVED -> "Архив"
    }
}