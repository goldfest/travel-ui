package com.travelguide.network.poi

import com.travelguide.network.dto.common.PageResponseDto
import com.travelguide.network.dto.poi.PoiMediaDto
import com.travelguide.network.dto.poi.PoiResponseDto
import com.travelguide.network.dto.poi.PoiSearchRequestDto
import com.travelguide.network.dto.poi.PoiTypeResponseDto
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
import kotlinx.serialization.Serializable

class PoiApi(
    private val client: HttpClient,
    private val baseUrl: String // http://10.0.2.2:8081/api/poi
) {

    suspend fun getPoiById(id: Long): PoiResponseDto {
        return client.get("$baseUrl/pois/$id").body()
    }

    suspend fun getPoisByCity(
        cityId: Long,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "name",
        sortDirection: String = "ASC"
    ): PageResponseDto<PoiResponseDto> {
        return client.get("$baseUrl/pois/city/$cityId") {
            parameter("page", page)
            parameter("size", size)
            parameter("sortBy", sortBy)
            parameter("sortDirection", sortDirection)
        }.body()
    }

    suspend fun searchPois(
        request: PoiSearchRequestDto
    ): PageResponseDto<PoiResponseDto> {
        return client.post("$baseUrl/pois/search") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getAllPoiTypes(): List<PoiTypeResponseDto> {
        return client.get("$baseUrl/poi-types/all").body()
    }

    suspend fun uploadUserPhotos(
        poiId: Long,
        files: List<UploadFile>
    ): List<PoiMediaDto> {
        return client.post("$baseUrl/pois/$poiId/media") {
            setBody(
                MultiPartFormDataContent(
                    formData {
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

    suspend fun getPendingMedia(page: Int = 0, size: Int = 50): PageResponseDto<PoiMediaDto> {
        return client.get("$baseUrl/pois/media/pending") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun approveMedia(poiId: Long, mediaId: Long): PoiMediaDto {
        return client.post("$baseUrl/pois/$poiId/media/$mediaId/approve").body()
    }

    suspend fun rejectMedia(poiId: Long, mediaId: Long, reason: String? = null): PoiMediaDto {
        return client.post("$baseUrl/pois/$poiId/media/$mediaId/reject") {
            contentType(ContentType.Application.Json)
            setBody(PoiMediaRejectRequestDto(reason = reason))
        }.body()
    }
}

@Serializable
data class PoiMediaRejectRequestDto(
    val reason: String? = null
)
