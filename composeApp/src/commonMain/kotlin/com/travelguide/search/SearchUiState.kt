package com.travelguide.search

import com.travelguide.domain.models.City
import com.travelguide.domain.models.POI

data class SearchUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val cities: List<City> = emptyList(),
    val pois: List<POI> = emptyList(),
    val selectedCity: City? = null,
    val recentQueries: List<String> = emptyList(),
    val favoritePoiIds: Set<Int> = emptySet(),
    val errorMessage: String? = null
)