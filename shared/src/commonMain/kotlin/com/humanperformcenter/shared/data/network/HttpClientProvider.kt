package com.humanperformcenter.shared.data.network

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json

const val API_BASE_URL: String = "https://human-app.duckdns.org/api"

interface HttpClientProvider {
    val apiClient: HttpClient
    val authClient: HttpClient
    val baseUrl: String
    val logoutEvents: SharedFlow<Unit>
}

class DefaultHttpClientProvider(
    private val authLocalDataSource: AuthLocalDataSource,
) : HttpClientProvider {
    private val logoutEventsMutable = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override val logoutEvents: SharedFlow<Unit> = logoutEventsMutable

    override val authClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override val apiClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val access = authLocalDataSource.getAccessToken()
                    val refresh = authLocalDataSource.getRefreshToken()
                    if (access == null || refresh == null) null else BearerTokens(access, refresh)
                }
                refreshTokens {
                    val oldRefresh = authLocalDataSource.getRefreshToken()
                    if (oldRefresh.isNullOrBlank()) {
                        handleLogout()
                        return@refreshTokens null
                    }

                    try {
                        val resp = authClient.post("$API_BASE_URL/mobile/tokens/refresh") {
                            markAsRefreshTokenRequest()
                            bearerAuth(oldRefresh)
                        }

                        if (resp.status == HttpStatusCode.Unauthorized) {
                            handleLogout()
                            null
                        } else {
                            val newTokens = resp.body<RefreshResponse>()
                            authLocalDataSource.saveTokens(newTokens.accessToken, newTokens.refreshToken)
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

    override val baseUrl: String = API_BASE_URL

    private suspend fun handleLogout() {
        authLocalDataSource.clearSession()
        logoutEventsMutable.emit(Unit)
    }
}
