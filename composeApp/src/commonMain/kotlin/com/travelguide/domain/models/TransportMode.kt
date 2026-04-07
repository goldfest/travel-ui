package com.travelguide.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class TransportMode {
    WALK,
    PUBLIC_TRANSPORT,
    CAR,
    MIXED;

    fun label(): String = when (this) {
        WALK -> "Пешком"
        PUBLIC_TRANSPORT -> "Общественный транспорт"
        CAR -> "На машине"
        MIXED -> "Смешанный"
    }
}
