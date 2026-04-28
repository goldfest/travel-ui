package com.travelguide.network.review

import com.travelguide.network.dto.review.CreateReportRequestDto
import com.travelguide.network.dto.review.ProcessReportRequestDto
import com.travelguide.network.dto.review.ReportResponseDto
import com.travelguide.network.dto.common.PageResponseDto
import com.travelguide.network.upload.UploadFile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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

    suspend fun createReportWithMedia(
        reportType: String,
        comment: String?,
        reviewId: Long?,
        poiId: Long?,
        files: List<UploadFile>
    ): ReportResponseDto {
        return client.post("$baseUrl/v1/reports/with-media") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("reportType", reportType)
                        comment?.takeIf { it.isNotBlank() }?.let { append("comment", it) }
                        reviewId?.let { append("reviewId", it.toString()) }
                        poiId?.let { append("poiId", it.toString()) }
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

    suspend fun getReportsByStatus(status: String = "PENDING", page: Int = 0, size: Int = 50): PageResponseDto<ReportResponseDto> {
        return client.get("$baseUrl/v1/reports/status/$status") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun processReport(reportId: Long, status: String, moderatorComment: String? = null): ReportResponseDto {
        return client.post("$baseUrl/v1/reports/$reportId/process") {
            contentType(ContentType.Application.Json)
            setBody(ProcessReportRequestDto(status = status, moderatorComment = moderatorComment))
        }.body()
    }
}
