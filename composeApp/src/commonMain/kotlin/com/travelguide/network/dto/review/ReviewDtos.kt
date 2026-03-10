package com.travelguide.network.dto.review

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewMediaResponseDto(
    val id: Long,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("createdAt") val createdAt: String? = null
)

@Serializable
data class ReviewResponseDto(
    val id: Long,
    val rating: Int,
    val comment: String? = null,
    @SerialName("isHidden") val isHidden: Boolean = false,
    val likesCount: Int = 0,
    val likedByCurrentUser: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val poiId: Long,
    val userId: Long,
    val userName: String? = null,
    val userAvatar: String? = null,
    val media: List<ReviewMediaResponseDto> = emptyList(),
    val totalMediaCount: Int? = null
)

@Serializable
data class ReviewPageResponseDto(
    val content: List<ReviewResponseDto> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 20
)

@Serializable
data class PoiReviewStatsResponseDto(
    val poiId: Long,
    val totalReviews: Long = 0,
    val averageRating: Double = 0.0,
    val visibleReviews: Long = 0,
    val oneStarCount: Long? = null,
    val twoStarsCount: Long? = null,
    val threeStarsCount: Long? = null,
    val fourStarsCount: Long? = null,
    val fiveStarsCount: Long? = null
)

@Serializable
data class CreateReviewRequestDto(
    val poiId: Long,
    val rating: Int,
    val comment: String? = null,
    val imageUrls: List<String> = emptyList()
)

@Serializable
data class UpdateReviewRequestDto(
    val rating: Int? = null,
    val comment: String? = null,
    val isHidden: Boolean? = null,
    val imageUrlsToAdd: List<String> = emptyList(),
    val imageIdsToRemove: List<Long> = emptyList()
)