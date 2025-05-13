package com.humaneperformcenter.shared.data.repository

import com.humaneperformcenter.shared.data.model.ErrorResponse
import com.humaneperformcenter.shared.data.model.LoginRequest
import com.humaneperformcenter.shared.data.model.RegisterRequest
import com.humaneperformcenter.shared.data.model.UserResponse
import com.humaneperformcenter.shared.data.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

object AuthRepository {
    suspend fun login(email: String, password: String): Result<UserResponse> {
        return try {
            val respuesta: UserResponse = ApiClient.httpClient.post("${ApiClient.baseUrl}/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body()  // deserializa directamente a UserResponse
            Result.success(respuesta)
        } catch (e: ClientRequestException) {
            // Error 4XX del servidor (ej: credenciales incorrectas, datos inválidos)
            // Podemos obtener el cuerpo de error si lo proporciona:
            val errorMessage = try { e.response.body<ErrorResponse>().message } catch (_: Exception) { e.message }
            Result.failure(Exception("Error de cliente: $errorMessage"))
        } catch (e: Exception) {
            // Otras excepciones (no hay conexión, servidor caído, etc.)
            Result.failure(e)
        }
    }

    suspend fun registrar(datos: RegisterRequest): Result<UserResponse> {
        return try {
            val nuevoUsuario: UserResponse = ApiClient.httpClient.post("${ApiClient.baseUrl}/register") {
                contentType(ContentType.Application.Json)
                setBody(datos)
            }.body()
            Result.success(nuevoUsuario)
        } catch (e: ClientRequestException) {
            val errorMessage = try { e.response.body<ErrorResponse>().message } catch (_: Exception) { e.message }
            Result.failure(Exception("Registro fallido: $errorMessage"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
