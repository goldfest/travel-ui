package com.travelguide.network.dto.review

import kotlinx.serialization.Serializable

@Serializable
data class ReportMediaResponseDto(
    val id: Long,
    val imageUrl: String? = null,
    val thumbnailUrl: String? = null,
    val createdAt: String? = null
)

@Serializable
data class ReportResponseDto(
    val id: Long,
    val reportType: String,
    val comment: String? = null,
    val moderatorComment: String? = null,
    val status: String,
    val photoUrl: String? = null,
    val media: List<ReportMediaResponseDto> = emptyList(),
    val totalMediaCount: Int? = null,
    val createdAt: String? = null,
    val handledAt: String? = null,
    val userId: Long,
    val handledByUserId: Long? = null,
    val reviewId: Long? = null,
    val poiId: Long? = null,
    val userName: String? = null,
    val userAvatarUrl: String? = null,
    val handledByUserName: String? = null,
    val handledByUserAvatarUrl: String? = null
)

@Serializable
data class CreateReportRequestDto(
    val reportType: String,
    val comment: String? = null,
    val photoUrl: String? = null,
    val reviewId: Long? = null,
    val poiId: Long? = null
)

@Serializable
data class ProcessReportRequestDto(
    val status: String,
    val moderatorComment: String? = null
)
