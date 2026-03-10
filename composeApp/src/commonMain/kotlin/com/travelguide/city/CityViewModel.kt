package com.travelguide.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CityViewModel(
    private val repository: CityRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(CityListUiState())
    val listState: StateFlow<CityListUiState> = _listState.asStateFlow()

    private val _detailsState = MutableStateFlow(CityDetailsUiState())
    val detailsState: StateFlow<CityDetailsUiState> = _detailsState.asStateFlow()

    fun loadCities() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, errorMessage = null)

            runCatching {
                val allCities = repository.getCities()
                val popular = repository.getPopularCities()

                _listState.value = _listState.value.copy(
                    isLoading = false,
                    cities = allCities,
                    popularCities = popular,
                    errorMessage = null
                )
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Не удалось загрузить города"
                )
            }
        }
    }

    fun searchCities(query: String) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(
                isLoading = true,
                searchQuery = query,
                errorMessage = null
            )

            runCatching {
                val result = if (query.isBlank()) repository.getCities()
                else repository.searchCities(query)

                _listState.value = _listState.value.copy(
                    isLoading = false,
                    cities = result,
                    errorMessage = null
                )
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Ошибка поиска городов"
                )
            }
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
                    errorMessage = e.message ?: "Не удалось загрузить город"
                )
            }
        }
    }
}