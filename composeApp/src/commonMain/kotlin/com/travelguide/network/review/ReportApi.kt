package com.travelguide.network.review

import com.travelguide.network.dto.review.CreateReportRequestDto
import com.travelguide.network.dto.review.ReportResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ReportApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun createReport(request: CreateReportRequestDto): ReportResponseDto {
        return client.post("$baseUrl/v1/reports") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}