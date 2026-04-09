package com.travelguide.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.city.CityRepository
import com.travelguide.domain.models.City
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.PoiCardUiModel
import com.travelguide.favorite.FavoriteRepository
import com.travelguide.poi.PoiRepository
import com.travelguide.review.ReviewRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.travelguide.core.toUserMessage
class SearchViewModel(
    private val cityRepository: CityRepository,
    private val poiRepository: PoiRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val reviewRepository: ReviewRepository
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
            items = emptyList(),
            selectedCity = null,
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
                items = emptyList(),
                selectedCity = null,
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
                    items = if (_state.value.selectedCity == null) emptyList() else _state.value.items,
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
                    errorMessage = e.toUserMessage("Ошибка поиска")
                )
            }
        }
    }

    fun searchPoisInSelectedCity(query: String, city: City? = _state.value.selectedCity) {
        if (city == null || query.isBlank()) {
            _state.value = _state.value.copy(
                items = emptyList()
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

                buildPoiCardItems(pois)
            }.onSuccess { items ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    items = items,
                    errorMessage = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Ошибка поиска объектов")
                )
            }
        }
    }

    fun toggleFavorite(poiId: Int) {
        viewModelScope.launch {
            val currentItems = _state.value.items
            val target = currentItems.firstOrNull { it.poi.id == poiId } ?: return@launch

            runCatching {
                if (target.isFavorite) {
                    favoriteRepository.removeFromFavorites(poiId)
                } else {
                    favoriteRepository.addToFavorites(poiId)
                }
            }.onSuccess {
                _state.value = _state.value.copy(
                    items = currentItems.map { item ->
                        if (item.poi.id == poiId) {
                            item.copy(isFavorite = !item.isFavorite)
                        } else {
                            item
                        }
                    }
                )
            }
        }
    }

    private suspend fun buildPoiCardItems(pois: List<POI>): List<PoiCardUiModel> =
        coroutineScope {
            pois.map { poi ->
                async {
                    val isFavorite = runCatching {
                        favoriteRepository.isFavorite(poi.id)
                    }.getOrDefault(false)

                    val stats = runCatching {
                        reviewRepository.getPoiStats(poi.id)
                    }.getOrNull()

                    PoiCardUiModel(
                        poi = poi,
                        isFavorite = isFavorite,
                        averageRating = stats?.averageRating,
                        reviewCount = stats?.totalReviews?: 0
                    )
                }
            }.awaitAll()
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