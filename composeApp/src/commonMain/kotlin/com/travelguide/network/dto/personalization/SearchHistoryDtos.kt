package com.travelguide.network.dto.personalization

import kotlinx.serialization.Serializable

@Serializable
data class SearchHistoryRequestDto(
    val queryText: String? = null,
    val filtersJson: String? = null,
    val cityId: Long? = null,
    val presetFilterId: Long? = null
)

@Serializable
data class SearchHistoryResponseDto(
    val id: Long,
    val queryText: String? = null,
    val filtersJson: String? = null,
    val userId: Long,
    val cityId: Long? = null,
    val presetFilterId: Long? = null,
    val searchedAt: String? = null
)