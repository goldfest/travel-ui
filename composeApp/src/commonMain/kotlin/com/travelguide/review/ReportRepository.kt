package com.travelguide.review

import com.travelguide.domain.models.Report
import com.travelguide.network.dto.review.CreateReportRequestDto
import com.travelguide.network.review.ReportApi
import com.travelguide.network.upload.UploadFile

class ReportRepository(
    private val api: ReportApi
) {
    suspend fun createPoiReport(
        poiId: Int,
        reportType: String,
        comment: String,
        photoUrl: String? = null,
        files: List<UploadFile> = emptyList()
    ): Report {
        return if (files.isNotEmpty()) {
            api.createReportWithMedia(
                reportType = reportType,
                comment = comment,
                reviewId = null,
                poiId = poiId.toLong(),
                files = files
            ).toDomain()
        } else {
            api.createReport(
                CreateReportRequestDto(
                    reportType = reportType,
                    comment = comment,
                    photoUrl = photoUrl,
                    poiId = poiId.toLong()
                )
            ).toDomain()
        }
    }

    suspend fun createReviewReport(
        reviewId: Int,
        reportType: String,
        comment: String,
        photoUrl: String? = null,
        files: List<UploadFile> = emptyList()
    ): Report {
        return if (files.isNotEmpty()) {
            api.createReportWithMedia(
                reportType = reportType,
                comment = comment,
                reviewId = reviewId.toLong(),
                poiId = null,
                files = files
            ).toDomain()
        } else {
            api.createReport(
                CreateReportRequestDto(
                    reportType = reportType,
                    comment = comment,
                    photoUrl = photoUrl,
                    reviewId = reviewId.toLong()
                )
            ).toDomain()
        }
    }
}
