package com.humanperformcenter.shared.data.network

import com.humanperformcenter.shared.data.model.RefreshResponse
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.call.body
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.request.post
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.bearerAuth
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodedPath
import kotlinx.serialization.json.Json

object ApiClient {
    private const val BASE = "http://163.172.71.195:8085/api"
    private val storage = SecureStorage

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    // se invoca en cada petición automatizada
                    storage.getAccessToken()?.let { access ->
                        BearerTokens(access, storage.getRefreshToken().orEmpty())
                    }
                }
                refreshTokens {
                    // 1) obtiene el refresh token
                    val refresh = storage.getRefreshToken()
                        ?: throw IllegalStateException("No hay refresh token")

                    // 2) lanza la llamada para refrescar
                    val httpResponse = client.post("$BASE/refresh") {
                        markAsRefreshTokenRequest()
                        bearerAuth(refresh)
                    }
                    // 3) parsea la respuesta
                    val resp: RefreshResponse = httpResponse.body()

                    // 4) guarda los nuevos tokens
                    storage.saveTokens(resp.accessToken, resp.refreshToken)

                    // 5) retorna al plugin
                    BearerTokens(resp.accessToken, resp.refreshToken)
                }
                sendWithoutRequest { request ->
                    // no pongas header en login o refresh
                    request.url.encodedPath.endsWith("/login") ||
                            request.url.encodedPath.endsWith("/refresh")
                }
            }
        }
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status == HttpStatusCode.Unauthorized) {
                    throw ResponseException(response, "Token expirado o inválido")
                }
            }
        }
    }

    val baseUrl get() = BASE
}
