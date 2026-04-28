package com.travelguide.review

import com.travelguide.domain.models.Review
import com.travelguide.network.dto.review.CreateReviewRequestDto
import com.travelguide.network.dto.review.UpdateReviewRequestDto
import com.travelguide.network.review.ReviewApi
import com.travelguide.network.upload.UploadFile

class ReviewRepository(
    private val api: ReviewApi
) {
    suspend fun getReviewsByPoi(poiId: Int): List<Review> {
        return api.getReviewsByPoi(poiId.toLong()).content.map { it.toDomain() }
    }

    suspend fun getReviewById(reviewId: Int): Review {
        return api.getReviewById(reviewId.toLong()).toDomain()
    }

    suspend fun getReviewsByUser(userId: Long): List<Review> {
        return api.getReviewsByUser(userId).content.map { it.toDomain() }
    }

    suspend fun createReview(
        poiId: Int,
        rating: Int,
        comment: String,
        imageUrls: List<String> = emptyList(),
        files: List<UploadFile> = emptyList()
    ): Review {
        return if (files.isNotEmpty()) {
            api.createReviewWithMedia(
                poiId = poiId.toLong(),
                rating = rating,
                comment = comment,
                files = files
            ).toDomain()
        } else {
            api.createReview(
                CreateReviewRequestDto(
                    poiId = poiId.toLong(),
                    rating = rating,
                    comment = comment,
                    imageUrls = imageUrls
                )
            ).toDomain()
        }
    }

    suspend fun updateReview(
        reviewId: Int,
        rating: Int?,
        comment: String?
    ): Review {
        return api.updateReview(
            reviewId = reviewId.toLong(),
            request = UpdateReviewRequestDto(
                rating = rating,
                comment = comment
            )
        ).toDomain()
    }

    suspend fun deleteReview(reviewId: Int) {
        api.deleteReview(reviewId.toLong())
    }

    suspend fun toggleLike(reviewId: Int): Review {
        return api.toggleLike(reviewId.toLong()).toDomain()
    }

    suspend fun getPoiStats(poiId: Int) = api.getPoiStats(poiId.toLong())
}
