package com.travelguide.core

data class PagedResult<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val page: Int = 0,
    val size: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true
)
