package com.travelguide.network

import com.travelguide.auth.TokenStorage
import com.travelguide.network.dto.AuthResponseDto
import com.travelguide.network.dto.RefreshTokenRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

actual class HttpClientFactory {
    actual fun create(baseUrl: String, storage: TokenStorage): HttpClient {
        val refreshMutex = Mutex()

        val refreshClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        android.util.Log.d("KTOR_HTTP", message)
                    }
                }
                level = LogLevel.ALL
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 120_000
            }
        }

        return HttpClient(OkHttp) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        android.util.Log.d("KTOR_HTTP", message)
                    }
                }
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 120_000
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val access = storage.accessToken
                        val refresh = storage.refreshToken
                        if (access.isNullOrBlank() || refresh.isNullOrBlank()) null
                        else BearerTokens(access, refresh)
                    }

                    refreshTokens {
                        refreshMutex.withLock {
                            val refresh = storage.refreshToken ?: return@withLock null

                            try {
                                val resp: AuthResponseDto = refreshClient.post("$baseUrl/auth/refresh-token") {
                                    contentType(ContentType.Application.Json)
                                    setBody(RefreshTokenRequestDto(refresh))
                                }.body()

                                storage.accessToken = resp.accessToken
                                storage.refreshToken = resp.refreshToken
                                BearerTokens(resp.accessToken, resp.refreshToken)
                            } catch (_: Exception) {
                                storage.clear()
                                throw UnauthorizedException()
                            }
                        }
                    }
                }
            }

            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, _ ->
                    if (cause is io.ktor.client.plugins.ClientRequestException) {
                        val code = cause.response.status.value
                        if (code == 401) throw UnauthorizedException()

                        val bodyText = runCatching { cause.response.bodyAsText() }.getOrDefault("")
                        throw RuntimeException("HTTP $code ${cause.response.status.description}. $bodyText")
                    }
                }
            }
        }
    }
}