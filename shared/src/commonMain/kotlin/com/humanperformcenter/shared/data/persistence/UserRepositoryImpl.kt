package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

object UserRepositoryImpl: UserRepository {

    /**
     * Envía un PUT a "/user" con el objeto LoginResponse completo (excepto token, que no cambia).
     * Si el servidor responde 200 OK, devuelve Result.success(updatedUser).
     * Si responde otro código o lanza excepción, devuelve Result.failure con el error.
     */
    override suspend fun updateUser(user: User): Result<User> {
        return try {
            // Realizamos la petición PUT al endpoint /user
            val resp: HttpResponse = ApiClient.apiClient.put("${ApiClient.baseUrl}/mobile/user") {
                contentType(ContentType.Application.Json)
                // El body se serializa automáticamente usando kotlinx-serialization
                setBody(user)
            }

            // Comprobamos el código de estado HTTP
            return if (resp.status == HttpStatusCode.OK) {
                val updatedUser: User = resp.body()
                SecureStorage.saveUser(updatedUser)
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Error al actualizar usuario: código HTTP ${resp.status.value}"))
            }
        } catch (e: Exception) {
            // Si hay timeout, red de falla, JSON malformado, etc.
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(email: String): Result<Unit> {
        return try {
            val resp: HttpResponse = ApiClient.apiClient.delete("${ApiClient.baseUrl}/mobile/user") {
                parameter("email", email)
            }
            return when (resp.status) {
                HttpStatusCode.OK       -> Result.success(Unit)
                HttpStatusCode.NotFound -> Result.failure(Exception("Usuario no encontrado"))
                else                    -> Result.failure(
                    Exception("Error al eliminar usuario: código HTTP ${resp.status.value}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserAllowedServices(customerId: Int): List<Int> {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-services") {
            url {
                parameters.append("user_id", customerId.toString())
            }
        }
        return response.body<Map<String, List<Int>>>()["service_ids"] ?: emptyList()
    }

}