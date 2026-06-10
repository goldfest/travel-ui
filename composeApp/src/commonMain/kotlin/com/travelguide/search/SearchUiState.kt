package com.travelguide.search

import com.travelguide.domain.models.City
import com.travelguide.domain.models.PoiCardUiModel

data class SearchUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val cities: List<City> = emptyList(),
    val items: List<PoiCardUiModel> = emptyList(),
    val selectedCity: City? = null,
    val recentQueries: List<String> = emptyList(),
    val poiPage: Int = 0,
    val poiPageSize: Int = 10,
    val poiTotalPages: Int = 0,
    val poiTotalElements: Long = 0,
    val errorMessage: String? = null
)
