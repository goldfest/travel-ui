package com.travelguide.network.route

import com.travelguide.network.dto.common.PageResponseDto
import com.travelguide.network.dto.route.CityGraphStatusResponseDto
import com.travelguide.network.dto.route.CreateRouteRequestDto
import com.travelguide.network.dto.route.GenerateRouteRequestDto
import com.travelguide.network.dto.route.ReorderRouteDayPointsRequestDto
import com.travelguide.network.dto.route.RouteOptimizationRequestDto
import com.travelguide.network.dto.route.RouteMapResponseDto
import com.travelguide.network.dto.route.RouteResponseDto
import com.travelguide.network.dto.route.UpdateRouteRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class AddPointToRouteRequestDto(
    val poiId: Long,
    val dayNumber: Int? = null,
    val orderIndex: Int? = null
)

class RouteApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    private val routesUrl = "$baseUrl/v1/routes"

    suspend fun getRoutes(
        page: Int = 0,
        size: Int = 20
    ): PageResponseDto<RouteResponseDto> {
        return client.get(routesUrl) {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getArchivedRoutes(
        page: Int = 0,
        size: Int = 20
    ): PageResponseDto<RouteResponseDto> {
        return client.get("$routesUrl/archived") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getRoutesByCity(cityId: Long): List<RouteResponseDto> {
        return client.get("$routesUrl/city/$cityId").body()
    }

    suspend fun getRouteById(routeId: Long): RouteResponseDto {
        return client.get("$routesUrl/$routeId").body()
    }

    suspend fun deleteRoute(routeId: Long) {
        client.delete("$routesUrl/$routeId")
    }

    suspend fun createRoute(request: CreateRouteRequestDto): RouteResponseDto {
        return client.post(routesUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateRoute(routeId: Long, request: UpdateRouteRequestDto): RouteResponseDto {
        return client.put("$routesUrl/$routeId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun addPointToRoute(
        routeId: Long,
        poiId: Long,
        dayNumber: Int? = null,
        orderIndex: Int? = null
    ): RouteResponseDto {
        return client.post("$routesUrl/$routeId/points") {
            contentType(ContentType.Application.Json)
            setBody(
                AddPointToRouteRequestDto(
                    poiId = poiId,
                    dayNumber = dayNumber,
                    orderIndex = orderIndex
                )
            )
        }.body()
    }

    suspend fun removePointFromRoute(
        routeId: Long,
        routePointId: Long
    ): RouteResponseDto {
        return client.delete("$routesUrl/$routeId/points/$routePointId").body()
    }

    suspend fun reorderDayPoints(
        routeId: Long,
        dayId: Long,
        routePointIdsInOrder: List<Long>
    ): RouteResponseDto {
        return client.post("$routesUrl/$routeId/days/$dayId/reorder") {
            contentType(ContentType.Application.Json)
            setBody(ReorderRouteDayPointsRequestDto(routePointIdsInOrder))
        }.body()
    }

    suspend fun optimizeRoute(
        routeId: Long,
        request: RouteOptimizationRequestDto
    ): RouteResponseDto {
        return client.post("$routesUrl/$routeId/optimize") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun generateRoute(request: GenerateRouteRequestDto): RouteResponseDto {
        return client.post("$routesUrl/generate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getRouteMap(routeId: Long): RouteMapResponseDto {
        return client.get("$routesUrl/$routeId/map").body()
    }


    suspend fun getCityGraphStatus(cityId: Long): CityGraphStatusResponseDto {
        return client.get("$baseUrl/v1/graphs/cities/$cityId/status").body()
    }

    suspend fun requestCityGraphDownload(cityId: Long): CityGraphStatusResponseDto {
        return client.post("$baseUrl/v1/graphs/cities/$cityId/download").body()
    }

    suspend fun downloadOfflineRoute(routeId: Long): ByteArray {
        return client.post("$baseUrl/v1/offline/routes/$routeId/download").body()
    }

    suspend fun exportRoutePdf(routeId: Long): ByteArray {
        return client.get("$baseUrl/v1/export/routes/$routeId/pdf").body()
    }

    suspend fun exportRouteGpx(routeId: Long): ByteArray {
        return client.get("$baseUrl/v1/export/routes/$routeId/gpx").body()
    }

    suspend fun exportRouteJson(routeId: Long): ByteArray {
        return client.get("$baseUrl/v1/export/routes/$routeId/json").body()
    }
}
