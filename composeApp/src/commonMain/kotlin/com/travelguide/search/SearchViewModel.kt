package com.travelguide.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.city.CityRepository
import com.travelguide.domain.models.City
import com.travelguide.favorite.FavoriteRepository
import com.travelguide.poi.PoiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val cityRepository: CityRepository,
    private val poiRepository: PoiRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadRecentQueries()
    }

    fun loadRecentQueries() {
        viewModelScope.launch {
            runCatching {
                searchHistoryRepository.getRecentQueries(limit = 10)
            }.onSuccess { recent ->
                _state.value = _state.value.copy(recentQueries = recent)
            }
        }
    }

    fun updateQuery(query: String) {
        _state.value = _state.value.copy(query = query)
    }

    fun clearSearch() {
        _state.value = _state.value.copy(
            query = "",
            cities = emptyList(),
            pois = emptyList(),
            selectedCity = null,
            favoritePoiIds = emptySet(),
            isLoading = false,
            errorMessage = null
        )
        loadRecentQueries()
    }

    fun clearHistory() {
        viewModelScope.launch {
            runCatching {
                searchHistoryRepository.clearHistory()
            }.onSuccess {
                _state.value = _state.value.copy(recentQueries = emptyList())
            }
        }
    }

    fun selectCity(city: City) {
        _state.value = _state.value.copy(selectedCity = city)
        val currentQuery = _state.value.query
        if (currentQuery.isNotBlank()) {
            searchPoisInSelectedCity(currentQuery, city)
            recordSearch(currentQuery, city.id)
        }
    }

    fun useRecentQuery(query: String) {
        updateQuery(query)
        search(query)
    }

    fun search(query: String) {
        updateQuery(query)

        if (query.isBlank()) {
            _state.value = _state.value.copy(
                isLoading = false,
                cities = emptyList(),
                pois = emptyList(),
                selectedCity = null,
                favoritePoiIds = emptySet(),
                errorMessage = null
            )
            loadRecentQueries()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)

            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                cityRepository.searchCities(query = query)
            }.onSuccess { cities ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    cities = cities,
                    pois = if (_state.value.selectedCity == null) emptyList() else _state.value.pois,
                    errorMessage = null
                )

                recordSearch(query, _state.value.selectedCity?.id)

                val selectedCity = _state.value.selectedCity
                if (selectedCity != null) {
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
            _state.value = _state.value.copy(
                pois = emptyList(),
                favoritePoiIds = emptySet()
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                val pois = poiRepository.searchPois(
                    cityId = city.id,
                    query = query
                )

                val favoriteIds = loadFavoriteIds(pois.map { it.id })

                pois to favoriteIds
            }.onSuccess { (pois, favoriteIds) ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    pois = pois,
                    favoritePoiIds = favoriteIds,
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

    fun toggleFavorite(poiId: Int) {
        viewModelScope.launch {
            val current = poiId in _state.value.favoritePoiIds

            runCatching {
                if (current) {
                    favoriteRepository.removeFromFavorites(poiId)
                } else {
                    favoriteRepository.addToFavorites(poiId)
                }
            }.onSuccess {
                _state.value = _state.value.copy(
                    favoritePoiIds = if (current) {
                        _state.value.favoritePoiIds - poiId
                    } else {
                        _state.value.favoritePoiIds + poiId
                    }
                )
            }
        }
    }

    private suspend fun loadFavoriteIds(poiIds: List<Int>): Set<Int> = coroutineScope {
        poiIds.map { poiId ->
            async {
                poiId to runCatching { favoriteRepository.isFavorite(poiId) }.getOrDefault(false)
            }
        }.awaitAll()
            .filter { it.second }
            .map { it.first }
            .toSet()
    }

    private fun recordSearch(query: String, cityId: Int?) {
        viewModelScope.launch {
            runCatching {
                searchHistoryRepository.recordSearch(
                    queryText = query,
                    cityId = cityId
                )
            }.onSuccess {
                loadRecentQueries()
            }
        }
    }
}