package com.travelguide.network.review

import com.travelguide.network.dto.review.CreateReviewRequestDto
import com.travelguide.network.dto.review.PoiReviewStatsResponseDto
import com.travelguide.network.dto.review.ReviewPageResponseDto
import com.travelguide.network.dto.review.ReviewResponseDto
import com.travelguide.network.dto.review.UpdateReviewRequestDto
import com.travelguide.network.upload.UploadFile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class ReviewApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getReviewsByPoi(
        poiId: Long,
        page: Int = 0,
        size: Int = 20
    ): ReviewPageResponseDto {
        return client.get("$baseUrl/v1/reviews/poi/$poiId") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getReviewById(reviewId: Long): ReviewResponseDto {
        return client.get("$baseUrl/v1/reviews/$reviewId").body()
    }

    suspend fun getReviewsByUser(
        userId: Long,
        page: Int = 0,
        size: Int = 20
    ): ReviewPageResponseDto {
        return client.get("$baseUrl/v1/reviews/user/$userId") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun createReview(request: CreateReviewRequestDto): ReviewResponseDto {
        return client.post("$baseUrl/v1/reviews") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun createReviewWithMedia(
        poiId: Long,
        rating: Int,
        comment: String?,
        files: List<UploadFile>
    ): ReviewResponseDto {
        return client.post("$baseUrl/v1/reviews/with-media") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("poiId", poiId.toString())
                        append("rating", rating.toString())
                        comment?.takeIf { it.isNotBlank() }?.let { append("comment", it) }
                        files.forEach { file ->
                            append(
                                key = "files",
                                value = file.bytes,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentType, file.contentType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"${file.fileName}\"")
                                }
                            )
                        }
                    }
                )
            )
        }.body()
    }

    suspend fun updateReview(
        reviewId: Long,
        request: UpdateReviewRequestDto
    ): ReviewResponseDto {
        return client.put("$baseUrl/v1/reviews/$reviewId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteReview(reviewId: Long) {
        client.delete("$baseUrl/v1/reviews/$reviewId").body<Unit>()
    }

    suspend fun toggleLike(reviewId: Long): ReviewResponseDto {
        return client.post("$baseUrl/v1/reviews/$reviewId/like").body()
    }

    suspend fun getPoiStats(poiId: Long): PoiReviewStatsResponseDto {
        return client.get("$baseUrl/v1/reviews/poi/$poiId/stats").body()
    }

    suspend fun getPendingReviews(page: Int = 0, size: Int = 50): ReviewPageResponseDto {
        return client.get("$baseUrl/v1/reviews/moderation/pending") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun approveReview(reviewId: Long, moderationComment: String? = null): ReviewResponseDto {
        return client.post("$baseUrl/v1/reviews/$reviewId/approve") {
            moderationComment?.takeIf { it.isNotBlank() }?.let { parameter("moderationComment", it) }
        }.body()
    }

    suspend fun rejectReview(reviewId: Long, moderationComment: String? = null): ReviewResponseDto {
        return client.post("$baseUrl/v1/reviews/$reviewId/reject") {
            moderationComment?.takeIf { it.isNotBlank() }?.let { parameter("moderationComment", it) }
        }.body()
    }
}
