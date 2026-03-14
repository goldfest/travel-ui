package com.travelguide.search

import com.travelguide.domain.models.SearchHistoryItem
import com.travelguide.network.dto.personalization.SearchHistoryRequestDto
import com.travelguide.network.dto.personalization.SearchHistoryResponseDto
import com.travelguide.network.personalization.SearchHistoryApi

class SearchHistoryRepository(
    private val api: SearchHistoryApi
) {
    suspend fun recordSearch(
        queryText: String?,
        cityId: Int? = null,
        filtersJson: String? = null,
        presetFilterId: Int? = null
    ) {
        api.recordSearch(
            SearchHistoryRequestDto(
                queryText = queryText,
                cityId = cityId?.toLong(),
                filtersJson = filtersJson,
                presetFilterId = presetFilterId?.toLong()
            )
        )
    }

    suspend fun getSearchHistory(
        page: Int = 0,
        size: Int = 20
    ): List<SearchHistoryItem> {
        return api.getSearchHistory(page = page, size = size)
            .content
            .map { it.toDomain() }
    }

    suspend fun getCitySearchHistory(
        cityId: Int,
        page: Int = 0,
        size: Int = 20
    ): List<SearchHistoryItem> {
        return api.getCitySearchHistory(
            cityId = cityId.toLong(),
            page = page,
            size = size
        ).content.map { it.toDomain() }
    }

    suspend fun getRecentQueries(limit: Int = 10): List<String> {
        return api.getRecentQueries(limit)
    }

    suspend fun clearHistory() {
        api.clearHistory()
    }
}

private fun SearchHistoryResponseDto.toDomain(): SearchHistoryItem {
    return SearchHistoryItem(
        id = id.toInt(),
        queryText = queryText,
        filtersJson = filtersJson,
        userId = userId.toInt(),
        cityId = cityId?.toInt(),
        presetFilterId = presetFilterId?.toInt(),
        searchedAt = searchedAt
    )
}