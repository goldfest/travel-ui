package com.travelguide.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class RouteStatus {
    DRAFT,
    GRAPH_PREPARING,
    READY,
    ARCHIVED;

    fun label(): String = when (this) {
        DRAFT -> "Черновик"
        GRAPH_PREPARING -> "Маршрут строится"
        READY -> "Готов"
        ARCHIVED -> "Архив"
    }
}
