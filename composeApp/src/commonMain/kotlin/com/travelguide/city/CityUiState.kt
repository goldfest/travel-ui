package com.travelguide.city

import com.travelguide.domain.models.City

data class CityListUiState(
    val isLoading: Boolean = false,
    val cities: List<City> = emptyList(),
    val popularCities: List<City> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

data class CityDetailsUiState(
    val isLoading: Boolean = false,
    val city: City? = null,
    val errorMessage: String? = null
)