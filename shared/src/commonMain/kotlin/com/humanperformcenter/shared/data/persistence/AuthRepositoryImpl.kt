package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ChangePasswordRequest
import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.data.model.RegisterResponse
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

object AuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): Result<LoginResponse> = try {
        val resp: HttpResponse = ApiClient.httpClient.post("${ApiClient.baseUrl}/mobile/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "password" to password))
            expectSuccess = false
        }

        when (resp.status) {
            HttpStatusCode.OK -> {
                val data: LoginResponse = resp.body()
                val userData = User(
                    id = data.id,
                    fullName = data.fullName,
                    email = data.email,
                    phone = data.phone,
                    sex = data.sex,
                    dateOfBirth = data.dateOfBirth,
                    postcode = data.postcode,
                    dni = data.dni,
                    profilePictureUrl = data.profilePictureUrl
                )
                SecureStorage.saveUser(userData)
                Result.success(data)
            }
            HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized -> {
                val err: ErrorResponse = resp.body() ?: ErrorResponse("Credenciales inválidas")
                Result.failure(Exception(err.message))
            }
            else -> Result.failure(Exception("Error al iniciar sesión"))
        }
    } catch (err: Throwable) {
        Result.failure(err)
    }

    override suspend fun register(datos: RegisterRequest): Result<RegisterResponse> {
        return try {
            // 1) Hacemos la petición y, si es 201, body() se deserializa a RegisterResponse
            val resp: HttpResponse = ApiClient.httpClient.post("${ApiClient.baseUrl}/mobile/register") {
                contentType(ContentType.Application.Json)
                setBody(datos)
            }

            return if (resp.status == HttpStatusCode.Created) {
                val registerResponse: RegisterResponse = resp.body()
                Result.success(registerResponse)
            } else {
                // 2) Si no es 201, intentamos extraer el JSON {"error": "..."}
                val errorBody: ErrorResponse = resp.body()
                Result.failure(Exception(errorBody.message))
            }
        } catch (e: ClientRequestException) {
            // En caso de 400, 409 u otro cliente, extraemos el JSON de error
            val errorMessage = try {
                e.response.body<ErrorResponse>().message
            } catch (_: Exception) {
                e.message
            }
            Result.failure(Exception(errorMessage))
        } catch (_: Exception) {
            // Timeout, sin red, JSON mal formado, etc.
            Result.failure(Exception("Error de conexión. Revisa tu conexión a internet e inténtalo de nuevo."))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int): Result<Unit> {
        return try {
            val resp: HttpResponse = ApiClient.httpClient.put("${ApiClient.baseUrl}/mobile/change-password") {
                contentType(ContentType.Application.Json)
                setBody(ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    userId = userId
                ))

                expectSuccess = false
            }

            when (resp.status) {
                HttpStatusCode.OK -> {
                    Result.success(Unit)
                }
                HttpStatusCode.BadRequest -> {
                    // Error de validación (contraseña muy débil, etc.)
                    val errorBody: ErrorResponse = try {
                        resp.body()
                    } catch (_: Exception) {
                        ErrorResponse("Error de validación en los datos enviados")
                    }
                    Result.failure(Exception(errorBody.message))
                }
                HttpStatusCode.Unauthorized -> {
                    // Contraseña actual incorrecta
                    val errorBody: ErrorResponse = try {
                        resp.body()
                    } catch (_: Exception) {
                        ErrorResponse("Contraseña actual incorrecta")
                    }
                    Result.failure(Exception(errorBody.message))
                }
                HttpStatusCode.Forbidden -> {
                    // Usuario no tiene permisos o sesión expirada
                    Result.failure(Exception("Sesión expirada. Por favor, inicia sesión nuevamente"))
                }
                else -> {
                    Result.failure(Exception("Error al cambiar contraseña. Inténtalo más tarde"))
                }
            }
        } catch (e: ClientRequestException) {
            // Manejo específico de errores del cliente (4xx)
            val errorMessage = try {
                e.response.body<ErrorResponse>().message
            } catch (_: Exception) {
                when (e.response.status) {
                    HttpStatusCode.BadRequest -> "Datos inválidos"
                    HttpStatusCode.Unauthorized -> "Contraseña actual incorrecta"
                    HttpStatusCode.Forbidden -> "Sesión expirada"
                    else -> "Error en la solicitud"
                }
            }
            Result.failure(Exception(errorMessage))
        } catch (_: ServerResponseException) {
            // Errores del servidor (5xx)
            Result.failure(Exception("Error del servidor. Inténtalo más tarde"))
        } catch (_: RedirectResponseException) {
            // Redirecciones inesperadas (3xx)
            Result.failure(Exception("Error de redirección. Contacta soporte"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Error de conexión. Revisa tu conexión a internet e inténtalo de nuevo"))
        }
    }
}
