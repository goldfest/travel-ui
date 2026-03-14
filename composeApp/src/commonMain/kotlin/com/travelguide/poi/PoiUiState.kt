package com.travelguide.poi

import com.travelguide.domain.models.POI
import com.travelguide.domain.models.POIType

data class PoiListUiState(
    val isLoading: Boolean = false,
    val pois: List<POI> = emptyList(),
    val poiTypes: List<POIType> = emptyList(),
    val errorMessage: String? = null
)

data class PoiDetailsUiState(
    val isLoading: Boolean = false,
    val poi: POI? = null,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null
)