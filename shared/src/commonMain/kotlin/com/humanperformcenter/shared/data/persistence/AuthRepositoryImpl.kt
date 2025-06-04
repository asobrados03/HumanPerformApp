package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.data.model.RegisterResponse
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.AuthRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

object AuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            // 1) Ejecutamos la petición y obtenemos el HttpResponse
            val httpResponse: HttpResponse = ApiClient.httpClient.post("${ApiClient.baseUrl}/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to email, "password" to password))
            }

            // 2) Si es 200 OK, deserializamos el body a LoginResponse
            return if (httpResponse.status == HttpStatusCode.OK) {
                // body<LoginResponse>() lee el JSON y crea el objeto
                val loginResponse: LoginResponse = httpResponse.body()
                Result.success(loginResponse)
            } else {
                // Cualquier otro status (400, 401, 500, etc.) lo consideramos fallo
                Result.failure(Exception("Error al autenticar: código HTTP ${httpResponse.status.value}"))
            }

        } catch (e: Exception) {
            // Si la llamada lanza excepción (timeout, JSON malformado, 401 sin body válido, etc.)
            Result.failure(e)
        }
    }

    override suspend fun register(datos: RegisterRequest): Result<RegisterResponse> {
        return try {
            val successMessage: RegisterResponse = ApiClient.httpClient.post("${ApiClient.baseUrl}/register") {
                contentType(ContentType.Application.Json)
                setBody(datos)
            }.body()
            Result.success(successMessage)
        } catch (e: ClientRequestException) {
            val errorMessage = try { e.response.body<ErrorResponse>().message } catch (_: Exception) { e.message }
            Result.failure(Exception("Registro fallido: $errorMessage"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
