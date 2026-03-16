package com.humanperformcenter.shared.data.persistence

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.payment.EwalletResponse
import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.data.model.user.AssignPreferredCoachRequest
import com.humanperformcenter.shared.data.model.user.AssignPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.data.model.user.UploadResponse
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.expectSuccess
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object UserRepositoryImpl: UserRepository {
    private val log = logging()

    /**
     * Envía un PUT a "/user" con el objeto LoginResponse completo (excepto token, que no cambia).
     * Si el servidor responde 200 OK, devuelve el `User` actualizado.
     * Si responde otro código o ocurre una excepción, se lanza dicho error.
     */
    override suspend fun updateUser(user: User, profilePicBytes: ByteArray?)
    : Result<User> = withContext(Dispatchers.IO) {
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
            return@withContext Result.success(updatedUser)
        } else {
            return@withContext Result.failure(Exception("Error al actualizar usuario: código HTTP " +
                    "${resp.status.value}"))
        }
    }

    override suspend fun getUserById(id: Int): Result<User> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp: HttpResponse = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/mobile/user"
            ) {
                contentType(ContentType.Application.Json)
                parameter("user_id", id)
            }

            if (resp.status == HttpStatusCode.OK) {
                val user: User = resp.body()
                Result.success(user)
            } else {
                Result.failure(Exception("Error al leer el usuario: ${resp.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp: HttpResponse = ApiClient.apiClient.delete(
                "${ApiClient.baseUrl}/mobile/user") {
                parameter("email", email)
            }
            return@withContext when (resp.status) {
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

    override suspend fun getCoaches(): Result<List<Professional>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/coaches") {
                contentType(ContentType.Application.Json)
                expectSuccess = false
            }

            val effectiveResponse = if (resp.status == HttpStatusCode.NotFound) {
                ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/list_coaches") {
                    contentType(ContentType.Application.Json)
                    expectSuccess = false
                }
            } else {
                resp
            }

            if (effectiveResponse.status == HttpStatusCode.OK) {
                Result.success(effectiveResponse.body<List<Professional>>())
            } else {
                Result.failure(Exception("Error al leer entrenadores: código HTTP ${effectiveResponse.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markFavorite(
        coachId: Int,
        serviceName: String?,
        userId: Int?
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
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

    override suspend fun getPreferredCoach(customerId: Int)
    : Result<GetPreferredCoachResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Llamada al endpoint, pasando el customerId como query parameter
            val response: HttpResponse = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/mobile/user/preferred-coach"
            ) {
                contentType(ContentType.Application.Json)
                parameter("customer_id", customerId)
            }

            if (response.status == HttpStatusCode.OK) {
                val body: GetPreferredCoachResponse = response.body()
                Result.success(body)
            } else {
                // parsea el mensaje de error o lanza uno genérico
                val errorResponse = response.body<ErrorResponse>()
                Result.failure(RuntimeException(errorResponse.error))
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

    override suspend fun deleteProfilePic(req: DeleteProfilePicRequest)
    : Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp: HttpResponse = ApiClient.apiClient.delete(
                "${ApiClient.baseUrl}/mobile/user/photo"
            ) {
                url {
                    parameters.append("profilePictureName", req.profilePictureName
                        .toString())
                    parameters.append("email", req.email)
                }
            }
            return@withContext when (resp.status) {
                HttpStatusCode.OK -> Result.success(resp.body())
                HttpStatusCode.NotFound -> Result.failure(Exception(resp.body<ErrorResponse>().error))
                else -> Result.failure(
                    Exception("Error al eliminar la foto de perfil: código HTTP ${resp.status.value}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserBookings(userId: Int)
    : Result<List<UserBooking>> = withContext(Dispatchers.IO) {
        require(userId > 0) { "customerId debe ser mayor que 0" }

        return@withContext try {
            val response = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/mobile/user-bookings"
            ) {
                parameter("user_id", userId)
            }

            when (response.status.value) {
                200 -> {
                    val bookings =
                        response.body<List<UserBooking>>()

                    if (bookings.isEmpty()) {
                        Result.success(emptyList())
                    } else {
                        Result.success(bookings)
                    }
                }
                404 -> Result.success(emptyList()) // Usuario sin reservas
                else -> {
                    log.error { "getUserBookings failed for customerId=$userId: $response" }
                    Result.failure(Exception("Error ${response.status.value}: $response"))
                }
            }
        } catch (e: IOException) {
            log.error { "Network error for customerId=$userId" }
            Result.failure(Exception("Error de red: ${e.message}", e))
        } catch (e: Exception) {
            log.error { "Unexpected error for customerId=$userId" }
            Result.failure(e)
        }
    }

    override suspend fun cancelUserBooking(bookingId: Int)
    : Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response: HttpResponse = ApiClient.apiClient.delete(
                "${ApiClient.baseUrl}/mobile/bookings/$bookingId"
            )

            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(
                    "Error al cancelar la reserva: código HTTP ${response.status.value}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getUserStats(customerId: Int)
    : Result<UserStatistics> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (customerId <= 0) {
                return@withContext Result.failure(Exception("ID de usuario inválido"))
            }

            val primaryResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/users/$customerId/stats") {
                expectSuccess = false
            }

            val needsLegacyFallback = primaryResponse.status == HttpStatusCode.NotFound ||
                primaryResponse.status == HttpStatusCode.BadRequest

            val effectiveResponse = if (needsLegacyFallback) {
                ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-stats") {
                    url { parameters.append("user_id", customerId.toString()) }
                    expectSuccess = false
                }
            } else {
                primaryResponse
            }

            if (effectiveResponse.status.value in 200..299) {
                Result.success(effectiveResponse.body<UserStatistics>())
            } else {
                Result.failure(Exception("Error HTTP: ${effectiveResponse.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCouponToUser(
        userId: Int,
        couponCode: String
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/users/$userId/coupons") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("coupon_code" to couponCode))
                expectSuccess = false
            }

            val effectiveResponse = if (response.status == HttpStatusCode.NotFound) {
                ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/user/$userId/coupon") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("coupon_code" to couponCode))
                    expectSuccess = false
                }
            } else {
                response
            }

            when (effectiveResponse.status) {
                HttpStatusCode.NoContent, HttpStatusCode.OK, HttpStatusCode.Created -> Unit
                else -> {
                    val error = try {
                        effectiveResponse.body<ErrorResponse>()
                    } catch (_: Exception) {
                        null
                    }
                    val message = error?.error ?: "HTTP ${effectiveResponse.status.value}"
                    throw Exception(message)
                }
            }
        }
    }





    override suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> = runCatching {
        withContext(Dispatchers.IO) {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/users/$userId/coupons") {
                expectSuccess = false
            }

            val effectiveResponse = if (response.status == HttpStatusCode.NotFound) {
                ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user/$userId/coupon") {
                    expectSuccess = false
                }
            } else {
                response
            }

            when (effectiveResponse.status) {
                HttpStatusCode.NoContent -> emptyList()
                HttpStatusCode.OK -> effectiveResponse.body<List<Coupon>>()
                HttpStatusCode.Forbidden -> {
                    val errorResponse = effectiveResponse.body<ErrorResponse>()
                    throw Exception(errorResponse.error)
                }
                else -> {
                    val raw = effectiveResponse.bodyAsText()
                    throw Exception("Error obteniendo cupones: HTTP ${effectiveResponse.status.value} $raw")
                }
            }
        }
    }

    override suspend fun uploadDocument(
        userId: Int,
        name: String,
        data: ByteArray
    ): Result<String> =
        runCatching {
            withContext(Dispatchers.IO) {

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

                val parts = formData {
                    append("file", data, Headers.build {
                        append(HttpHeaders.ContentType, contentType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$name\"")
                    })
                }

                val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/users/$userId/documents") {
                    setBody(MultiPartFormDataContent(parts))
                    expectSuccess = false
                }

                val effectiveResponse = if (response.status == HttpStatusCode.NotFound) {
                    ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/user/document") {
                        setBody(MultiPartFormDataContent(parts))
                        expectSuccess = false
                    }
                } else {
                    response
                }

                when (effectiveResponse.status) {
                    HttpStatusCode.Created -> effectiveResponse.body<UploadResponse>().message

                    HttpStatusCode.OK -> effectiveResponse.body<UploadResponse>().message

                    HttpStatusCode.BadRequest -> {
                        throw Exception("Error en la solicitud: ${effectiveResponse.safeErrorBody()}")
                    }

                    HttpStatusCode.Unauthorized -> {
                        throw Exception("No autorizado. Verifica tu token")
                    }

                    HttpStatusCode.Forbidden -> throw Exception("Acceso denegado")


                    HttpStatusCode.RequestHeaderFieldTooLarge -> {
                        throw Exception("El archivo es demasiado grande")
                    }

                    HttpStatusCode.UnsupportedMediaType -> {
                        throw Exception("Tipo no soportado: $contentType")
                    }

                    HttpStatusCode.InternalServerError -> {
                        throw Exception("Error interno del servidor")
                    }

                    else ->
                        throw Exception(
                            "HTTP ${effectiveResponse.status.value} → ${effectiveResponse.safeErrorBody()}"
                        )
                }
            }
        }.recoverCatching { e ->
            when (e) {
                is HttpRequestTimeoutException -> throw Exception("Timeout al subir el archivo")

                is ConnectTimeoutException -> throw Exception("No se pudo conectar al servidor")

                is SocketTimeoutException -> throw Exception("Timeout de conexión")

                else -> throw e
            }
        }



    private suspend fun HttpResponse.safeErrorBody(): String =
        try {
            body<ErrorResponse>().error
        } catch (_: Exception) {
            bodyAsText()
        }

    override suspend fun getEwalletBalance(userId: Int)
    : Result<Double?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/mobile/user/e-wallet-balance"
            ) {
                url {
                    parameters.append("user_id", userId.toString())
                }
            }

            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            val balance = json["balance"]?.jsonPrimitive?.doubleOrNull
            Result.success(balance)
        } catch (e: Exception) {
            log.error { "❌ Error al obtener saldo e-wallet para userId=$userId: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getEwalletTransactions(userId: Int)
    : Result<List<EwalletTransaction>> = runCatching {
        withContext(Dispatchers.IO) {
            val response: EwalletResponse =
                ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user/transactions") {
                    parameter("user_id", userId)
                }.body()

            response.transactions
        }
    }

}