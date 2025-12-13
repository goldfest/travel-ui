package com.travelguide.domain.models

data class Report(
    val id: Int,
    val reportType: String, // incorrect_info, offensive_content
    val comment: String,
    val status: String = "pending", // pending, approved, rejected
    val photoUrl: String? = null,
    val createdAt: String = "",
    val userId: Int,
    val reviewId: Int? = null,
    val poiId: Int? = null
) {
    fun statusText(): String = when (status) {
        "pending" -> "Ожидает рассмотрения"
        "approved" -> "Одобрена"
        "rejected" -> "Отклонена"
        else -> "Неизвестно"
    }

    fun reportTypeText(): String = when (reportType) {
        "incorrect_info" -> "Неверная информация"
        "offensive_content" -> "Оскорбительный контент"
        "spam" -> "Спам"
        "other" -> "Другое"
        else -> "Неизвестно"
    }
}