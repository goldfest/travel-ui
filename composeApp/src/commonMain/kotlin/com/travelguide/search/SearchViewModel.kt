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

private const val SEARCH_POI_PAGE_SIZE = 10

class SearchViewModel(
    private val cityRepository: CityRepository,
    private val poiRepository: PoiRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState(poiPageSize = SEARCH_POI_PAGE_SIZE))
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
            poiPage = 0,
            poiTotalPages = 0,
            poiTotalElements = 0,
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
        _state.value = _state.value.copy(
            selectedCity = city,
            items = emptyList(),
            poiPage = 0,
            poiTotalPages = 0,
            poiTotalElements = 0
        )
        val currentQuery = _state.value.query
        if (currentQuery.isNotBlank()) {
            searchPoisInSelectedCity(currentQuery, city, page = 0)
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
                poiPage = 0,
                poiTotalPages = 0,
                poiTotalElements = 0,
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
                val selectedCity = _state.value.selectedCity

                _state.value = _state.value.copy(
                    isLoading = false,
                    cities = cities,
                    items = if (selectedCity == null) emptyList() else _state.value.items,
                    poiPage = if (selectedCity == null) 0 else _state.value.poiPage,
                    poiTotalPages = if (selectedCity == null) 0 else _state.value.poiTotalPages,
                    poiTotalElements = if (selectedCity == null) 0 else _state.value.poiTotalElements,
                    errorMessage = null
                )

                recordSearch(query, selectedCity?.id)

                if (selectedCity != null) {
                    searchPoisInSelectedCity(query, selectedCity, page = 0)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Ошибка поиска")
                )
            }
        }
    }

    fun searchPoisInSelectedCity(
        query: String,
        city: City? = _state.value.selectedCity,
        page: Int = 0
    ) {
        if (city == null || query.isBlank()) {
            _state.value = _state.value.copy(
                items = emptyList(),
                poiPage = 0,
                poiTotalPages = 0,
                poiTotalElements = 0
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                val poisPage = poiRepository.searchPoisPage(
                    cityId = city.id,
                    query = query,
                    page = page,
                    size = SEARCH_POI_PAGE_SIZE
                )

                val items = buildPoiCardItems(poisPage.content)
                poisPage to items
            }.onSuccess { (poisPage, items) ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    items = items,
                    poiPage = poisPage.page,
                    poiPageSize = poisPage.size.takeIf { it > 0 } ?: SEARCH_POI_PAGE_SIZE,
                    poiTotalPages = poisPage.totalPages,
                    poiTotalElements = poisPage.totalElements,
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

    fun loadPoiSearchPage(page: Int) {
        searchPoisInSelectedCity(
            query = _state.value.query,
            city = _state.value.selectedCity,
            page = page
        )
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
