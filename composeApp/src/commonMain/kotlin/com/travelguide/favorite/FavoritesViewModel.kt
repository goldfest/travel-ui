package com.travelguide.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoriteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                repository.getFavorites(page = 0, size = 100)
            }.onSuccess { favorites ->
                _state.value = FavoritesUiState(
                    isLoading = false,
                    favorites = favorites,
                    errorMessage = null
                )
            }.onFailure { e ->
                _state.value = FavoritesUiState(
                    isLoading = false,
                    favorites = emptyList(),
                    errorMessage = e.message ?: "Не удалось загрузить избранное"
                )
            }
        }
    }

    fun removeFromFavorites(poiId: Int) {
        viewModelScope.launch {
            runCatching {
                repository.removeFromFavorites(poiId)
            }.onSuccess {
                _state.value = _state.value.copy(
                    favorites = _state.value.favorites.filterNot { it.poiId == poiId }
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    errorMessage = e.message ?: "Не удалось удалить из избранного"
                )
            }
        }
    }
}