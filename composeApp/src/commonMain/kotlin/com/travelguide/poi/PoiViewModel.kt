package com.travelguide.poi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PoiViewModel(
    private val repository: PoiRepository
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
                val poi = repository.getPoiById(id)
                _detailsState.value = PoiDetailsUiState(
                    isLoading = false,
                    poi = poi,
                    errorMessage = null
                )
            }.onFailure { e ->
                _detailsState.value = PoiDetailsUiState(
                    isLoading = false,
                    poi = null,
                    errorMessage = e.message ?: "Не удалось загрузить объект"
                )
            }
        }
    }
}