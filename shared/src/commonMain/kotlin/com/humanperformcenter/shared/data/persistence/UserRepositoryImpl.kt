package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.EstadisticasUsuario
import com.humanperformcenter.shared.data.model.Professional
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.model.UserBooking
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.contentType
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.Json

object UserRepositoryImpl: UserRepository {

    /**
     * Envía un PUT a "/user" con el objeto LoginResponse completo (excepto token, que no cambia).
     * Si el servidor responde 200 OK, devuelve Result.success(updatedUser).
     * Si responde otro código o lanza excepción, devuelve Result.failure con el error.
     */
    override suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> {
        return try {
            // 1) Serializamos el User a JSON
            val userJson = Json.encodeToString(User.serializer(), user)

            // 2) Construimos los distintos parts del formulario
            val formData = formData {
                // Campo "user" con JSON
                append(
                    key = "user",
                    value = userJson,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                )

                // Si hay bytes de imagen, lo añadimos como archivo
                profilePicBytes?.let { bytes ->
                    append("profile_pic", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition,
                            "filename=\"${user.profilePictureName}\"")
                    })
                }
            }

            // 3) Hacemos la petición PUT con multipart/form-data
            val resp: HttpResponse = ApiClient.apiClient.put("${ApiClient.baseUrl}/mobile/user") {
                setBody(MultiPartFormDataContent(formData))
            }

            // 4) Procesamos la respuesta
            if (resp.status == HttpStatusCode.OK) {
                val updatedUser: User = resp.body()
                SecureStorage.saveUser(updatedUser)
                Result.success(updatedUser)
            } else {
                Result.failure(
                    Exception("Error al actualizar usuario: código HTTP ${resp.status.value}")
                )
            }
        } catch (e: Exception) {
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

    override suspend fun getCoaches(): Result<List<Professional>> {
        return try {
            val resp: HttpResponse = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/mobile/list_coaches"
            ) {
                contentType(ContentType.Application.Json)
            }

            if (resp.status == HttpStatusCode.OK) {
                val coaches: List<Professional> = resp.body()
                Result.success(coaches)
            } else {
                Result.failure(Exception("Error al leer entrenadores: código HTTP " +
                        "${resp.status.value}"))
            }
        } catch (e: Exception) {
            // Si hay timeout, red de falla, JSON malformado, etc.
            Result.failure(e)
        }
    }

    override suspend fun getUserAllowedServices(customerId: Int): List<ServiceAvailable> {
        val response = ApiClient.apiClient.get(
            "${ApiClient.baseUrl}/mobile/user-services"
        ) {
            url {
                parameters.append("user_id", customerId.toString())
            }
        }
        return response.body()
    }

    override suspend fun getUserBookings(customerId: Int): List<UserBooking> {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-bookings") {
            url {
                parameters.append("user_id", customerId.toString())
            }
        }
        return response.body()
    }

    override suspend fun cancelUserBooking(bookingId: Int): Result<Unit> {
        return try {
            val response: HttpResponse = ApiClient.apiClient.delete("${ApiClient.baseUrl}/mobile/booking/$bookingId")

            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al cancelar la reserva: código HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserStats(customerId: Int): EstadisticasUsuario {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-stats") {
            url {
                parameters.append("user_id", customerId.toString())
            }
        }
        return response.body()
    }

}