package com.travelguide.personalisation

import com.travelguide.domain.models.Collection

data class CollectionPickerUiState(
    val isLoading: Boolean = false,
    val collections: List<Collection> = emptyList(),
    val isAdding: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)