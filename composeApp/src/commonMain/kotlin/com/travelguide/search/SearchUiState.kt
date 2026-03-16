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
    val errorMessage: String? = null
)