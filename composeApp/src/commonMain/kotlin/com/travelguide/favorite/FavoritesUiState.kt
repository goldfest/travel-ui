package com.travelguide.favorite

import com.travelguide.domain.models.Favorite

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val favorites: List<Favorite> = emptyList(),
    val errorMessage: String? = null
)