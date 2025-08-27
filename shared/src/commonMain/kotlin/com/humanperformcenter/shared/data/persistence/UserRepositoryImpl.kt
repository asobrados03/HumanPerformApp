package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.AssignPreferredCoachRequest
import com.humanperformcenter.shared.data.model.AssignPreferredCoachResponse
import com.humanperformcenter.shared.data.model.Coupon
import com.humanperformcenter.shared.data.model.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.EwalletTransaction
import com.humanperformcenter.shared.data.model.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.Professional
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.UploadResponse
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.model.UserBooking
import com.humanperformcenter.shared.data.model.UserStatistics
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object UserRepositoryImpl: UserRepository {

    /**
     * Envía un PUT a "/user" con el objeto LoginResponse completo (excepto token, que no cambia).
     * Si el servidor responde 200 OK, devuelve el `User` actualizado.
     * Si responde otro código o ocurre una excepción, se lanza dicho error.
     */
    override suspend fun updateUser(user: User, profilePicBytes: ByteArray?): User {
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
            return updatedUser
        } else {
            throw Exception("Error al actualizar usuario: código HTTP ${resp.status.value}")
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

    override suspend fun markFavorite(
        coachId: Int,
        serviceName: String?,
        userId: Int?
    ): Result<String> {
        return try {
            val response: HttpResponse = ApiClient.apiClient.post(
                "${ApiClient.baseUrl}/mobile/user/preferred-coach"
            ) {

                val customerId = userId ?: 0

                contentType(ContentType.Application.Json)
                setBody(
                    AssignPreferredCoachRequest(
                        serviceName = serviceName.toString(),
                        customerId = customerId,
                        coachId = coachId
                    )
                )
            }
            val bodyResponse: AssignPreferredCoachResponse = response.body()

            Result.success(bodyResponse.message)
        } catch (e: ClientRequestException) {
            // 4xx
            Result.failure(RuntimeException("Error de cliente: ${e.response.status}"))
        } catch (e: ServerResponseException) {
            // 5xx
            Result.failure(RuntimeException("Error de servidor: ${e.response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> {
        return try {
            // Llamada al endpoint, pasando el customerId como query parameter
            val response: HttpResponse = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/mobile/user/preferred-coach"
            ) {
                contentType(ContentType.Application.Json)
                parameter("customer_id", customerId)
            }

            if (response.status == HttpStatusCode.OK) {
                val body: GetPreferredCoachResponse = response.body()
                return Result.success(body)
            } else {
                // parsea el mensaje de error o lanza uno genérico
                val errorResponse = response.body<ErrorResponse>()
                return Result.failure(RuntimeException(errorResponse.error))
            }
        } catch (e: ClientRequestException) {
            // 4xx
            Result.failure(RuntimeException("Error de cliente: ${e.response.status}"))
        } catch (e: ServerResponseException) {
            // 5xx
            Result.failure(RuntimeException("Error de servidor: ${e.response.status}"))
        } catch (e: Exception) {
            // Otros errores (timeout, parseo, etc.)
            Result.failure(e)
        }
    }

    override suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> {
        return try {
            val resp: HttpResponse = ApiClient.apiClient.delete(
                "${ApiClient.baseUrl}/mobile/user/photo"
            ) {
                url {
                    parameters.append("profilePictureName", req.profilePictureName.toString())
                    parameters.append("email", req.email)
                }
            }
            return when (resp.status) {
                HttpStatusCode.OK       -> Result.success(resp.body())
                HttpStatusCode.NotFound -> Result.failure(Exception(resp.body<ErrorResponse>().error))
                else                    -> Result.failure(
                    Exception("Error al eliminar la foto de perfil: código HTTP ${resp.status.value}")
                )
            }
        } catch (e: Exception) {
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

    override suspend fun getUserStats(customerId: Int): UserStatistics {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-stats") {
            url {
                parameters.append("user_id", customerId.toString())
            }
        }
        return response.body()
    }

    override suspend fun addCouponToUser(
        userId: Int,
        couponCode: String
    ): Result<Unit> {
        return try{
            val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/user/$userId/coupon") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("coupon_code" to couponCode))
            }

            if (response.status == HttpStatusCode.NoContent) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        "Error al añadir el cupon de descuento: ${response.body<ErrorResponse>().error}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /*override suspend fun getUserCoupon(userId: Int): Result<Coupon?> = runCatching {
        // Asumimos que el endpoint devuelve 204 si no hay cupón,
        // o JSON { … } si existe uno.
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user/$userId/coupon")

        return@runCatching when (response.status) {
            HttpStatusCode.NoContent -> null
            HttpStatusCode.OK -> response.body<Coupon>()
            else -> {
                val txt = response.bodyAsText()
                throw RuntimeException("GET  → ${response.status}: $txt")
            }
        }
    }*/
    override suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> {
        return try {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user/$userId/coupon") {
                url {
                    parameters.append("user_id", userId.toString())
                }
            }

            val coupons: List<Coupon> = response.body()
            Result.success(coupons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadDocument(
        name: String,
        data: ByteArray
    ): Result<String> = runCatching {
        // 1) Detectar mime type de forma más robusta
        val contentType = when (name.substringAfterLast('.', "").lowercase()) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }

        // 2) Crear el formData siguiendo el patrón que funciona en register
        val parts = formData {
            append("file", data, Headers.build {
                append(HttpHeaders.ContentType, contentType)
                append(HttpHeaders.ContentDisposition, "filename=\"$name\"")
            })
        }

        try {
            // 3) Realizar la petición POST con el mismo patrón que funciona
            val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/user/document") {
                setBody(MultiPartFormDataContent(parts))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val uploadResponse = response.body<UploadResponse>()
                    uploadResponse.message
                }
                HttpStatusCode.BadRequest -> {
                    val errorBody = try {
                        response.body<ErrorResponse>()
                    } catch (_: Exception) {
                        ErrorResponse("Solicitud inválida: ${response.bodyAsText()}")
                    }
                    throw Exception("Error en la solicitud: ${errorBody.error}")
                }
                HttpStatusCode.Unauthorized -> {
                    throw Exception("No autorizado. Verifica tu token de autenticación")
                }
                HttpStatusCode.Forbidden -> {
                    throw Exception("Acceso denegado al recurso")
                }
                HttpStatusCode.RequestHeaderFieldTooLarge -> {
                    throw Exception("El archivo es demasiado grande")
                }
                HttpStatusCode.UnsupportedMediaType -> {
                    throw Exception("Tipo de archivo no soportado: $contentType")
                }
                HttpStatusCode.InternalServerError -> {
                    throw Exception("Error interno del servidor. Intenta nuevamente")
                }
                else -> {
                    val errorBody = try {
                        response.body<ErrorResponse>()
                    } catch (_: Exception) {
                        ErrorResponse("Error HTTP ${response.status.value}: ${response.bodyAsText()}")
                    }
                    throw Exception("Error al subir archivo: HTTP ${response.status.value} → ${errorBody.error}")
                }
            }
        } catch (e: Exception) {
            when (e) {
                is HttpRequestTimeoutException -> {
                    throw Exception("Timeout al subir el archivo. Verifica tu conexión e intenta con un archivo más pequeño")
                }
                is ConnectTimeoutException -> {
                    throw Exception("No se pudo conectar al servidor. Verifica tu conexión de internet")
                }
                is SocketTimeoutException -> {
                    throw Exception("Timeout de conexión. El archivo puede ser demasiado grande")
                }
                else -> throw e
            }
        }
    }

    override suspend fun getEwalletBalance(userId: Int): Result<Double?> {
        return try {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user/e-wallet-balance") {
                url {
                    parameters.append("user_id", userId.toString())
                }
            }

            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            val balance = json["balance"]?.jsonPrimitive?.doubleOrNull
            Result.success(balance)
        } catch (e: Exception) {
            println("❌ Error al obtener saldo: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getEwalletTransactions(userId: Int): List<EwalletTransaction> {
        return try {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user/transactions") {
                url {
                    parameters.append("user_id", userId.toString())
                }
            }

            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            val txArray = json["transactions"]?.jsonArray ?: return emptyList()

            txArray.map {
                Json.decodeFromJsonElement<EwalletTransaction>(it)
            }
        } catch (e: Exception) {
            println("❌ Error al cargar transacciones: ${e.message}")
            emptyList()
        }
    }


}