package com.humanperformcenter.shared.data.network

import com.humanperformcenter.shared.data.model.auth.RefreshResponse
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json

object ApiClient {
    private const val BASE = "https://human-app.duckdns.org/api"
    private val storage = SecureStorage

    // Cambiamos a SharedFlow con buffer para que el evento no se pierda
    val logoutEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // 1) Cliente puro para refresh
    val authClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // 2) Cliente principal sin lógica de refresh en Auth
    val apiClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val access = storage.getAccessToken()
                    val refresh = storage.getRefreshToken()
                    if (access == null || refresh == null) null
                    else BearerTokens(access, refresh)
                }
                refreshTokens {
                    val oldRefresh = storage.getRefreshToken()
                    if (oldRefresh.isNullOrBlank()) {
                        handleLogout()
                        return@refreshTokens null
                    }

                    try {
                        val resp = authClient.post("$BASE/mobile/tokens/refresh") {
                            markAsRefreshTokenRequest()
                            bearerAuth(oldRefresh)
                        }

                        if (resp.status == HttpStatusCode.Unauthorized) {
                            handleLogout()
                            null
                        } else {
                            val newTokens = resp.body<RefreshResponse>()
                            storage.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                            BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                        }
                    } catch (_: Exception) {
                        handleLogout()
                        null
                    }
                }
            }
        }
    }

    val baseUrl get() = BASE

    // Centralizamos la lógica de salida
    private suspend fun handleLogout() {
        storage.clear()
        logoutEvents.emit(Unit) // .emit() espera si es necesario, asegurando que llegue
    }
}