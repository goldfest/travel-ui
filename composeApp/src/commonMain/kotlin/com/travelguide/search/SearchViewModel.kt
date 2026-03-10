package com.travelguide.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.city.CityRepository
import com.travelguide.domain.models.City
import com.travelguide.poi.PoiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val cityRepository: CityRepository,
    private val poiRepository: PoiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    fun updateQuery(query: String) {
        _state.value = _state.value.copy(query = query)
    }

    fun clearSearch() {
        _state.value = SearchUiState()
    }

    fun selectCity(city: City) {
        _state.value = _state.value.copy(selectedCity = city)
        val currentQuery = _state.value.query
        if (currentQuery.isNotBlank()) {
            searchPoisInSelectedCity(currentQuery, city)
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(
            query = query,
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            runCatching {
                val cities = if (query.isBlank()) {
                    emptyList()
                } else {
                    cityRepository.searchCities(query = query)
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    cities = cities,
                    pois = emptyList(),
                    errorMessage = null
                )

                val selectedCity = _state.value.selectedCity
                if (selectedCity != null && query.isNotBlank()) {
                    searchPoisInSelectedCity(query, selectedCity)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Ошибка поиска"
                )
            }
        }
    }

    fun searchPoisInSelectedCity(query: String, city: City? = _state.value.selectedCity) {
        if (city == null || query.isBlank()) {
            _state.value = _state.value.copy(pois = emptyList())
            return
        }

        _state.value = _state.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            runCatching {
                val pois = poiRepository.searchPois(
                    cityId = city.id,
                    query = query
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    pois = pois,
                    errorMessage = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Ошибка поиска объектов"
                )
            }
        }
    }
}