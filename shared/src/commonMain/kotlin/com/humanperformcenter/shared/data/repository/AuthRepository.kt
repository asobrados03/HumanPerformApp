package com.humanperformcenter.shared.data.repository

import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.data.model.UserResponse
import com.humanperformcenter.shared.data.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

object AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response: LoginResponse = ApiClient.httpClient.post("${ApiClient.baseUrl}/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to email, "pass" to password))
            }.body()
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
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
