package com.humanperformcenter.shared.data.network

import com.humanperformcenter.shared.data.model.RefreshResponse
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

object ApiClient {
    private const val BASE = "https://apihuman.fransdata.com/api"
    private val storage = SecureStorage

    // flujo que emitirá cuando toque cerrar sesión
    val logoutEvents = MutableSharedFlow<Unit>(replay = 0)

    // 1) Cliente puro para refresh
    val authClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // 2) Cliente principal sin lógica de refresh en Auth
    val apiClient = HttpClient(CIO) {
        install(LogoutPlugin) {
            sink = logoutEvents
        }

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val access = storage.getAccessToken().orEmpty()
                    val refresh = storage.getRefreshToken().orEmpty()
                    BearerTokens(access, refresh)
                }
                refreshTokens {
                    val oldRefresh = storage.getRefreshToken()
                        ?: throw IllegalStateException("No hay refresh token")
                    val resp = authClient.post("$BASE/mobile/refresh") {
                        markAsRefreshTokenRequest()
                        bearerAuth(oldRefresh)
                    }
                    if (resp.status == HttpStatusCode.Unauthorized) {
                        runBlocking { storage.clear() }
                        return@refreshTokens null
                    } else {
                        val newTokens = resp.body<RefreshResponse>()
                        storage.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                        BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                    }
                }
            }
        }
    }

    val baseUrl get() = BASE
}
