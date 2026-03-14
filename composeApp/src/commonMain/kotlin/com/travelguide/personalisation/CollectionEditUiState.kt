package com.travelguide.personalisation

import com.travelguide.domain.models.Collection
import com.travelguide.domain.models.POI

data class CollectionEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val collection: Collection? = null,
    val pois: List<POI> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)