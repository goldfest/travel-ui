package com.travelguide.poi

import com.travelguide.domain.models.POI
import com.travelguide.domain.models.POIType
import com.travelguide.domain.models.PoiCardUiModel

data class PoiListUiState(
    val isLoading: Boolean = false,
    val items: List<PoiCardUiModel> = emptyList(),
    val poiTypes: List<POIType> = emptyList(),
    val currentPage: Int = 0,
    val pageSize: Int = 10,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val errorMessage: String? = null
)

data class PoiDetailsUiState(
    val isLoading: Boolean = false,
    val poi: POI? = null,
    val isFavorite: Boolean = false,
    val averageRating: Double? = null,
    val reviewCount: Int = 0,
    val errorMessage: String? = null,
    val isPhotoUploading: Boolean = false,
    val photoUploadMessage: String? = null
)
