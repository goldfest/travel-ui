package com.travelguide.admin

import com.travelguide.network.poi.PoiApi
import com.travelguide.network.review.ReportApi
import com.travelguide.network.review.ReviewApi

class AdminRepository(
    private val reviewApi: ReviewApi,
    private val reportApi: ReportApi,
    private val poiApi: PoiApi
) {
    suspend fun loadModerationQueue(): AdminUiState {
        val reviewsResult = runCatching {
            reviewApi.getPendingReviews(size = 50)
        }

        val reportsResult = runCatching {
            reportApi.getReportsByStatus(status = "pending", size = 50)
        }

        val photosResult = runCatching {
            poiApi.getPendingMedia(size = 50)
        }

        val reviews = reviewsResult.getOrNull()
        val reports = reportsResult.getOrNull()
        val photos = photosResult.getOrNull()

        val errors = buildList {
            reviewsResult.exceptionOrNull()?.let { add("Отзывы: ${it.message}") }
            reportsResult.exceptionOrNull()?.let { add("Жалобы: ${it.message}") }
            photosResult.exceptionOrNull()?.let { add("Фото: ${it.message}") }
        }

        return AdminUiState(
            dashboard = AdminDashboard(
                pendingReviews = reviews?.totalElements?.toInt() ?: 0,
                pendingReports = reports?.totalElements?.toInt() ?: 0,
                pendingPoiPhotos = photos?.totalElements?.toInt() ?: 0
            ),
            pendingReviews = reviews?.content ?: emptyList(),
            pendingReports = reports?.content ?: emptyList(),
            pendingPoiPhotos = photos?.content ?: emptyList(),
            error = errors.takeIf { it.isNotEmpty() }?.joinToString("\n")
        )
    }

    suspend fun approveReview(id: Long) = reviewApi.approveReview(id)
    suspend fun rejectReview(id: Long, reason: String? = null) = reviewApi.rejectReview(id, reason)

    suspend fun resolveReport(id: Long, comment: String? = null) =
        reportApi.processReport(id, status = "approved", moderatorComment = comment)

    suspend fun rejectReport(id: Long, comment: String? = null) =
        reportApi.processReport(id, status = "rejected", moderatorComment = comment)

    suspend fun approvePoiPhoto(poiId: Long, mediaId: Long) = poiApi.approveMedia(poiId, mediaId)
    suspend fun rejectPoiPhoto(poiId: Long, mediaId: Long, reason: String? = null) = poiApi.rejectMedia(poiId, mediaId, reason)
}
