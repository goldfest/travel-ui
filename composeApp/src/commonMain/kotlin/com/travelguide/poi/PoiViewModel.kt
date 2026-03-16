package com.travelguide.poi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.domain.models.PoiCardUiModel
import com.travelguide.favorite.FavoriteRepository
import com.travelguide.review.ReviewRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PoiViewModel(
    private val repository: PoiRepository,
    private val favoriteRepository: FavoriteRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(PoiListUiState())
    val listState: StateFlow<PoiListUiState> = _listState.asStateFlow()

    private val _detailsState = MutableStateFlow(PoiDetailsUiState())
    val detailsState: StateFlow<PoiDetailsUiState> = _detailsState.asStateFlow()

    fun loadPoisByCity(cityId: Int) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                val poisDeferred = async { repository.getPoisByCity(cityId = cityId) }
                val typesDeferred = async { repository.getPoiTypes() }

                val pois = poisDeferred.await()
                val types = typesDeferred.await()
                val items = buildPoiCardItems(pois)

                _listState.value = PoiListUiState(
                    isLoading = false,
                    items = items,
                    poiTypes = types,
                    errorMessage = null
                )
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Не удалось загрузить достопримечательности"
                )
            }
        }
    }

    fun searchPoisInCity(
        cityId: Int,
        query: String,
        poiTypeIds: List<Int> = emptyList()
    ) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                val pois = if (query.isBlank() && poiTypeIds.isEmpty()) {
                    repository.getPoisByCity(cityId = cityId)
                } else {
                    repository.searchPois(
                        cityId = cityId,
                        query = query,
                        poiTypeIds = poiTypeIds
                    )
                }

                val items = buildPoiCardItems(pois)

                _listState.value = _listState.value.copy(
                    isLoading = false,
                    items = items,
                    errorMessage = null
                )
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Ошибка поиска достопримечательностей"
                )
            }
        }
    }

    fun loadPoi(id: Int) {
        viewModelScope.launch {
            _detailsState.value = PoiDetailsUiState(isLoading = true)

            runCatching {
                val poiDeferred = async { repository.getPoiById(id) }
                val favoriteDeferred = async { favoriteRepository.isFavorite(id) }
                val statsDeferred = async { reviewRepository.getPoiStats(id) }

                val poi = poiDeferred.await()
                val isFavorite = favoriteDeferred.await()
                val stats = statsDeferred.await()

                _detailsState.value = PoiDetailsUiState(
                    isLoading = false,
                    poi = poi,
                    isFavorite = isFavorite,
                    averageRating = stats.averageRating,
                    reviewCount = stats.totalReviews.toInt(),
                    errorMessage = null
                )
            }.onFailure { e ->
                _detailsState.value = PoiDetailsUiState(
                    isLoading = false,
                    poi = null,
                    isFavorite = false,
                    averageRating = null,
                    reviewCount = 0,
                    errorMessage = e.message ?: "Не удалось загрузить объект"
                )
            }
        }
    }

    fun toggleFavorite() {
        val poi = _detailsState.value.poi ?: return

        viewModelScope.launch {
            val current = _detailsState.value.isFavorite
            val newValue = !current

            runCatching {
                if (current) {
                    favoriteRepository.removeFromFavorites(poi.id)
                } else {
                    favoriteRepository.addToFavorites(poi.id)
                }
            }.onSuccess {
                updateFavoriteInDetails(poi.id, newValue)
                updateFavoriteInList(poi.id, newValue)
            }.onFailure { e ->
                _detailsState.value = _detailsState.value.copy(
                    errorMessage = e.message ?: "Не удалось обновить избранное"
                )
            }
        }
    }

    fun toggleFavoriteForCard(poiId: Int) {
        viewModelScope.launch {
            val currentItems = _listState.value.items
            val target = currentItems.firstOrNull { it.poi.id == poiId } ?: return@launch
            val newValue = !target.isFavorite

            runCatching {
                if (target.isFavorite) {
                    favoriteRepository.removeFromFavorites(poiId)
                } else {
                    favoriteRepository.addToFavorites(poiId)
                }
            }.onSuccess {
                updateFavoriteInList(poiId, newValue)
                updateFavoriteInDetails(poiId, newValue)
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    errorMessage = e.message ?: "Не удалось обновить избранное"
                )
            }
        }
    }

    private suspend fun buildPoiCardItems(pois: List<com.travelguide.domain.models.POI>): List<PoiCardUiModel> =
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
                        reviewCount = stats?.totalReviews ?: 0
                    )
                }
            }.awaitAll()
        }
    private fun updateFavoriteInList(poiId: Int, isFavorite: Boolean) {
        _listState.value = _listState.value.copy(
            items = _listState.value.items.map { item ->
                if (item.poi.id == poiId) item.copy(isFavorite = isFavorite) else item
            }
        )
    }

    private fun updateFavoriteInDetails(poiId: Int, isFavorite: Boolean) {
        val currentPoi = _detailsState.value.poi
        if (currentPoi?.id == poiId) {
            _detailsState.value = _detailsState.value.copy(isFavorite = isFavorite)
        }
    }
}