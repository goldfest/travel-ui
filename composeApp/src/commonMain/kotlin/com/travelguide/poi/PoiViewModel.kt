package com.travelguide.poi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.favorite.FavoriteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PoiViewModel(
    private val repository: PoiRepository,
    private val favoriteRepository: FavoriteRepository
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

                _listState.value = PoiListUiState(
                    isLoading = false,
                    pois = pois,
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

                _listState.value = _listState.value.copy(
                    isLoading = false,
                    pois = pois,
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

                val poi = poiDeferred.await()
                val isFavorite = favoriteDeferred.await()

                _detailsState.value = PoiDetailsUiState(
                    isLoading = false,
                    poi = poi,
                    isFavorite = isFavorite,
                    errorMessage = null
                )
            }.onFailure { e ->
                _detailsState.value = PoiDetailsUiState(
                    isLoading = false,
                    poi = null,
                    isFavorite = false,
                    errorMessage = e.message ?: "Не удалось загрузить объект"
                )
            }
        }
    }

    fun toggleFavorite() {
        val poi = _detailsState.value.poi ?: return

        viewModelScope.launch {
            val current = _detailsState.value.isFavorite

            runCatching {
                if (current) {
                    favoriteRepository.removeFromFavorites(poi.id)
                } else {
                    favoriteRepository.addToFavorites(poi.id)
                }
            }.onSuccess {
                _detailsState.value = _detailsState.value.copy(
                    isFavorite = !current
                )
            }.onFailure { e ->
                _detailsState.value = _detailsState.value.copy(
                    errorMessage = e.message ?: "Не удалось обновить избранное"
                )
            }
        }
    }
}