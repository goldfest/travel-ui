package com.travelguide.domain.models

data class Review(
    val id: Int,
    val rating: Int,
    val comment: String? = null,
    val isHidden: Boolean = false,
    val createdAt: String = "",
    val likesCount: Int = 0,
    val poiId: Int,
    val userId: Int,
    val user: User? = null,
    val images: List<String> = emptyList()
) {
    fun isPositive(): Boolean = rating >= 4
    fun isNegative(): Boolean = rating <= 2
}