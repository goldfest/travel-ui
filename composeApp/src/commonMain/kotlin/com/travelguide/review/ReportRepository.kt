package com.travelguide.review

import com.travelguide.domain.models.Report
import com.travelguide.network.dto.review.CreateReportRequestDto
import com.travelguide.network.review.ReportApi

class ReportRepository(
    private val api: ReportApi
) {
    suspend fun createPoiReport(
        poiId: Int,
        reportType: String,
        comment: String,
        photoUrl: String? = null
    ): Report {
        return api.createReport(
            CreateReportRequestDto(
                reportType = reportType,
                comment = comment,
                photoUrl = photoUrl,
                poiId = poiId.toLong()
            )
        ).toDomain()
    }

    suspend fun createReviewReport(
        reviewId: Int,
        reportType: String,
        comment: String,
        photoUrl: String? = null
    ): Report {
        return api.createReport(
            CreateReportRequestDto(
                reportType = reportType,
                comment = comment,
                photoUrl = photoUrl,
                reviewId = reviewId.toLong()
            )
        ).toDomain()
    }
}