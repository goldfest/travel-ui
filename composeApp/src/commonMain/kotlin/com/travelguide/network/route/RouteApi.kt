package com.travelguide.network.route

import com.travelguide.network.dto.common.PageResponseDto
import com.travelguide.network.dto.route.CreateRouteRequestDto
import com.travelguide.network.dto.route.GenerateRouteRequestDto
import com.travelguide.network.dto.route.ReorderRouteDayPointsRequestDto
import com.travelguide.network.dto.route.RouteResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RouteApi(
    private val client: HttpClient,
    private val baseUrl: String // http://10.0.2.2:8087/api/routes
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

    suspend fun getRouteById(routeId: Long): RouteResponseDto {
        return client.get("$routesUrl/$routeId").body()
    }

    suspend fun createRoute(request: CreateRouteRequestDto): RouteResponseDto {
        return client.post(routesUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
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
        mode: String = "distance"
    ): RouteResponseDto {
        return client.post("$routesUrl/$routeId/optimize") {
            parameter("optimizationMode", mode)
        }.body()
    }

    suspend fun generateRoute(request: GenerateRouteRequestDto): RouteResponseDto {
        return client.post("$routesUrl/generate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}