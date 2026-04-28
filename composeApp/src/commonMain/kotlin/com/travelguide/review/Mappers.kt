package com.travelguide.review

import com.travelguide.core.MediaUrlResolver
import com.travelguide.domain.models.Report
import com.travelguide.domain.models.Review
import com.travelguide.domain.models.User
import com.travelguide.network.dto.review.ReportResponseDto
import com.travelguide.network.dto.review.ReviewResponseDto

fun ReviewResponseDto.toDomain(): Review {
    return Review(
        id = id.toInt(),
        rating = rating,
        comment = comment,
        isHidden = isHidden,
        createdAt = createdAt ?: "",
        likesCount = likesCount,
        poiId = poiId.toInt(),
        userId = userId.toInt(),
        user = User(
            id = userId,
            email = "",
            username = userName ?: "Пользователь",
            avatarUrl = MediaUrlResolver.resolve(userAvatar) ?: userAvatar
        ),
        images = media.mapNotNull { mediaItem ->
            MediaUrlResolver.resolve(mediaItem.imageUrl ?: mediaItem.thumbnailUrl)
        }
    )
}

fun ReportResponseDto.toDomain(): Report {
    return Report(
        id = id.toInt(),
        reportType = reportType,
        comment = comment ?: "",
        status = status,
        photoUrl = MediaUrlResolver.resolve(photoUrl) ?: photoUrl,
        createdAt = createdAt ?: "",
        userId = userId.toInt(),
        reviewId = reviewId?.toInt(),
        poiId = poiId?.toInt()
    )
}