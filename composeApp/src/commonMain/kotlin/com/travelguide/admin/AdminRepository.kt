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
        val reviews = reviewApi.getPendingReviews(size = 50)
        val reports = reportApi.getReportsByStatus(status = "pending", size = 50)
        val photos = poiApi.getPendingMedia(size = 50)

        return AdminUiState(
            dashboard = AdminDashboard(
                pendingReviews = reviews.totalElements.toInt(),
                pendingReports = reports.totalElements.toInt(),
                pendingPoiPhotos = photos.totalElements.toInt()
            ),
            pendingReviews = reviews.content,
            pendingReports = reports.content,
            pendingPoiPhotos = photos.content
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
