package com.travelguide.admin

import com.travelguide.network.dto.poi.PoiMediaDto
import com.travelguide.network.dto.review.ReportResponseDto
import com.travelguide.network.dto.review.ReviewResponseDto

data class AdminDashboard(
    val pendingReviews: Int = 0,
    val pendingReports: Int = 0,
    val pendingPoiPhotos: Int = 0
)

data class AdminUiState(
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val dashboard: AdminDashboard = AdminDashboard(),
    val pendingReviews: List<ReviewResponseDto> = emptyList(),
    val pendingReports: List<ReportResponseDto> = emptyList(),
    val pendingPoiPhotos: List<PoiMediaDto> = emptyList(),
    val message: String? = null,
    val error: String? = null
)
