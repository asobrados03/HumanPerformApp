package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserRepository
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

object UserRepositoryImpl: UserRepository {

    /**
     * Envía un PUT a "/user" con el objeto LoginResponse completo (excepto token, que no cambia).
     * Si el servidor responde 200 OK, devuelve Result.success(updatedUser).
     * Si responde otro código o lanza excepción, devuelve Result.failure con el error.
     */
    override suspend fun updateUser(user: LoginResponse): Result<LoginResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Realizamos la petición PUT al endpoint /user
            val httpResponse: HttpResponse = ApiClient.httpClient.put("${ApiClient.baseUrl}/user") {
                contentType(ContentType.Application.Json)
                // El body se serializa automáticamente usando kotlinx-serialization
                setBody(user)
            }

            // Comprobamos el código de estado HTTP
            return@withContext if (httpResponse.status == HttpStatusCode.OK) {
                // Deserializamos el JSON de respuesta a LoginResponse
                val updatedUser: LoginResponse = httpResponse.body()
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Error al actualizar usuario: código HTTP ${httpResponse.status.value}"))
            }
        } catch (e: Exception) {
            // Si hay timeout, red de falla, JSON malformado, etc.
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = ApiClient.httpClient.delete("${ApiClient.baseUrl}/user") {
                parameter("email", email)
            }
            return@withContext when (response.status) {
                HttpStatusCode.OK       -> Result.success(Unit)
                HttpStatusCode.NotFound -> Result.failure(Exception("Usuario no encontrado"))
                else                    -> Result.failure(
                    Exception("Error al eliminar usuario: código HTTP ${response.status.value}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}