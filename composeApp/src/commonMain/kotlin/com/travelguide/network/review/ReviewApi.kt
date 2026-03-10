package com.travelguide.network.review

import com.travelguide.network.dto.review.CreateReviewRequestDto
import com.travelguide.network.dto.review.PoiReviewStatsResponseDto
import com.travelguide.network.dto.review.ReviewPageResponseDto
import com.travelguide.network.dto.review.ReviewResponseDto
import com.travelguide.network.dto.review.UpdateReviewRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
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
        return client.get("$baseUrl/v1/reviews/poi/$poiId?page=$page&size=$size").body()
    }

    suspend fun getReviewById(reviewId: Long): ReviewResponseDto {
        return client.get("$baseUrl/v1/reviews/$reviewId").body()
    }

    suspend fun getReviewsByUser(
        userId: Long,
        page: Int = 0,
        size: Int = 20
    ): ReviewPageResponseDto {
        return client.get("$baseUrl/v1/reviews/user/$userId?page=$page&size=$size").body()
    }

    suspend fun createReview(request: CreateReviewRequestDto): ReviewResponseDto {
        return client.post("$baseUrl/v1/reviews") {
            contentType(ContentType.Application.Json)
            setBody(request)
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
}