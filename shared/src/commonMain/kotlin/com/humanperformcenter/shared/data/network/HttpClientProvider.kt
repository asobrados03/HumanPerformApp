package com.humanperformcenter.shared.data.network

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.auth.RefreshResponse
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
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
    authClientEngine: HttpClientEngine? = null,
    apiClientEngine: HttpClientEngine? = null,
    private val enableNetworkLogging: Boolean = false,
) : HttpClientProvider {
    private val logoutEventsMutable = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override val logoutEvents: SharedFlow<Unit> = logoutEventsMutable

    override val authClient: HttpClient = createHttpClient(authClientEngine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override val apiClient: HttpClient = createHttpClient(apiClientEngine) {
        expectSuccess = true
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

    private fun createHttpClient(
        engine: HttpClientEngine?,
        block: HttpClientConfig<*>.() -> Unit,
    ): HttpClient = if (engine == null) {
        HttpClient {
            installNetworkLogging()
            block()
        }
    } else {
        HttpClient(engine) {
            installNetworkLogging()
            block()
        }
    }

    private fun HttpClientConfig<*>.installNetworkLogging() {
        if (!enableNetworkLogging) return

        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.SIMPLE.withSensitiveBodyRedaction()
            sanitizeHeader { header -> header.equals("Authorization", ignoreCase = true) }
        }
    }

    private fun Logger.withSensitiveBodyRedaction(): Logger = object : Logger {
        override fun log(message: String) {
            this@withSensitiveBodyRedaction.log(message.redactSensitiveBodyFields())
        }
    }

    private fun String.redactSensitiveBodyFields(): String {
        return SENSITIVE_BODY_PATTERNS.fold(this) { acc, pattern ->
            acc.replace(pattern, "$1***REDACTED***$2")
        }
    }

    private companion object {
        val SENSITIVE_BODY_PATTERNS = listOf(
            Regex("(\"password\"\\s*:\\s*\")[^\"]*(\")", RegexOption.IGNORE_CASE),
            Regex("(\"newPassword\"\\s*:\\s*\")[^\"]*(\")", RegexOption.IGNORE_CASE),
            Regex("(\"confirmPassword\"\\s*:\\s*\")[^\"]*(\")", RegexOption.IGNORE_CASE),
            Regex("(password=)[^&\\s]*(?=(&|\\s|$))", RegexOption.IGNORE_CASE),
        )
    }

    private suspend fun handleLogout() {
        authLocalDataSource.clear()
        logoutEventsMutable.emit(Unit)
    }
}
