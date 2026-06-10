package com.travelguide.city

import com.travelguide.domain.models.City

data class CityListUiState(
    val isLoading: Boolean = false,
    val cities: List<City> = emptyList(),
    val popularCities: List<City> = emptyList(),
    val searchQuery: String = "",
    val cityPage: Int = 0,
    val cityPageSize: Int = 10,
    val cityTotalPages: Int = 0,
    val cityTotalElements: Long = 0,
    val errorMessage: String? = null
)

data class CityDetailsUiState(
    val isLoading: Boolean = false,
    val city: City? = null,
    val errorMessage: String? = null
)
