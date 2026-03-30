package com.humanperformcenter.shared.data.network

import com.humanperformcenter.shared.data.local.impl.AuthLocalDataSourceImpl
import com.humanperformcenter.shared.data.model.auth.RefreshResponse
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
    private val storage = AuthLocalDataSourceImpl

    val logoutEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val authClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    val apiClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val access = storage.getAccessToken()
                    val refresh = storage.getRefreshToken()
                    if (access == null || refresh == null) null else BearerTokens(access, refresh)
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

    private suspend fun handleLogout() {
        storage.clearSession()
        logoutEvents.emit(Unit)
    }
}
