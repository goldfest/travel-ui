package com.travelguide.personalisation

import com.travelguide.domain.models.Collection

data class CollectionsUiState(
    val isLoading: Boolean = false,
    val collections: List<Collection> = emptyList(),
    val errorMessage: String? = null
)