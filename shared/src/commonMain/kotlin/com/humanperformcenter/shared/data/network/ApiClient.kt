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
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {
    private const val BASE = "http://163.172.71.195:8085/api"
    private val storage = SecureStorage

    // 1) Cliente puro para refresh
    val authClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // 2) Cliente principal sin lógica de refresh en Auth
    val apiClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL   // mira cabeceras y cuerpos
        }

        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(Auth) {
            bearer {
                // 1) Cada vez que Ktor necesite el token, lo carga desde tu storage
                loadTokens {
                    val access = storage.getAccessToken().orEmpty()
                    val refresh = storage.getRefreshToken().orEmpty()
                    println("→ loadTokens() devuelve access=$access")
                    BearerTokens(access, refresh)
                }

                // 2) Si llega un 401 + WWW-Authenticate: Bearer, entra aquí
                refreshTokens {
                    val oldRefresh = storage.getRefreshToken()
                        ?: throw IllegalStateException("No hay refresh token")

                    // Llamada de refresco *marcada* para que Ktor no la intercepte de nuevo
                    val resp = authClient.post("$BASE/mobile/refresh") {
                        markAsRefreshTokenRequest()
                        bearerAuth(oldRefresh)
                    }

                    return@refreshTokens if (resp.status == HttpStatusCode.Unauthorized) {
                        println("   ⚠️ refresh token expirado también; fuerza logout")
                        null
                    } else {
                        // parseo y guardado
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
