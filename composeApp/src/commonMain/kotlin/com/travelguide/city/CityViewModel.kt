package com.travelguide.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.travelguide.core.toUserMessage

private const val CITY_PAGE_SIZE = 10

class CityViewModel(
    private val repository: CityRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(CityListUiState(cityPageSize = CITY_PAGE_SIZE))
    val listState: StateFlow<CityListUiState> = _listState.asStateFlow()

    private val _detailsState = MutableStateFlow(CityDetailsUiState())
    val detailsState: StateFlow<CityDetailsUiState> = _detailsState.asStateFlow()

    fun loadCities(page: Int = 0) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(
                isLoading = true,
                searchQuery = "",
                errorMessage = null
            )

            runCatching {
                val citiesPage = repository.getCitiesPage(page = page, size = CITY_PAGE_SIZE)
                val popular = if (_listState.value.popularCities.isEmpty()) {
                    repository.getPopularCities()
                } else {
                    _listState.value.popularCities
                }

                _listState.value = _listState.value.copy(
                    isLoading = false,
                    cities = citiesPage.content,
                    popularCities = popular,
                    cityPage = citiesPage.page,
                    cityPageSize = citiesPage.size.takeIf { it > 0 } ?: CITY_PAGE_SIZE,
                    cityTotalPages = citiesPage.totalPages,
                    cityTotalElements = citiesPage.totalElements,
                    errorMessage = null
                )
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить города")
                )
            }
        }
    }

    fun searchCities(query: String, page: Int = 0) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(
                isLoading = true,
                searchQuery = query,
                errorMessage = null
            )

            runCatching {
                val result = if (query.isBlank()) {
                    repository.getCitiesPage(page = page, size = CITY_PAGE_SIZE)
                } else {
                    repository.searchCitiesPage(query = query, page = page, size = CITY_PAGE_SIZE)
                }

                _listState.value = _listState.value.copy(
                    isLoading = false,
                    cities = result.content,
                    cityPage = result.page,
                    cityPageSize = result.size.takeIf { it > 0 } ?: CITY_PAGE_SIZE,
                    cityTotalPages = result.totalPages,
                    cityTotalElements = result.totalElements,
                    errorMessage = null
                )
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Ошибка поиска городов")
                )
            }
        }
    }

    fun loadCityPage(page: Int) {
        val query = _listState.value.searchQuery
        if (query.isBlank()) {
            loadCities(page = page)
        } else {
            searchCities(query = query, page = page)
        }
    }

    fun loadCity(cityId: Int) {
        viewModelScope.launch {
            _detailsState.value = CityDetailsUiState(isLoading = true)

            runCatching {
                val city = repository.getCityById(cityId)
                _detailsState.value = CityDetailsUiState(
                    isLoading = false,
                    city = city,
                    errorMessage = null
                )
            }.onFailure { e ->
                _detailsState.value = CityDetailsUiState(
                    isLoading = false,
                    city = null,
                    errorMessage = e.toUserMessage("Не удалось загрузить город")
                )
            }
        }
    }
}
