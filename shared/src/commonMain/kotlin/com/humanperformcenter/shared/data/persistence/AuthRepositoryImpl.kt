package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ChangePasswordRequest
import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.data.model.RegisterResponse
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.AuthRepository
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
    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            // Evitamos que Ktor lance excepción automática en 4xx/5xx
            val response: HttpResponse = ApiClient.httpClient.post("${ApiClient.baseUrl}/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to email, "password" to password))
                // Deshabilita el “throwOnError” automático
                expectSuccess = false
            }

            return when (response.status) {
                HttpStatusCode.OK -> {
                    val loginResponse: LoginResponse = response.body()
                    Result.success(loginResponse)
                }
                HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized -> {
                    // Leer el JSON de error que envía el backend
                    val errorBody: ErrorResponse = try {
                        response.body()
                    } catch (_: Exception) {
                        ErrorResponse("Credenciales inválidas")
                    }
                    Result.failure(Exception(errorBody.message))
                }
                else -> {
                    Result.failure(Exception("Hubo un problema al iniciar sesión. Por favor, inténtalo más tarde."))
                }
            }
        } catch (_: Exception) {
            // Timeout, sin red, JSON mal formado, etc.
            Result.failure(Exception("Error de servidor o conexión. Revisa tu conexión a internet e/o inténtalo de nuevo."))
        }
    }

    override suspend fun register(datos: RegisterRequest): Result<RegisterResponse> {
        return try {
            // 1) Hacemos la petición y, si es 201, body() se deserializa a RegisterResponse
            val httpResponse: HttpResponse = ApiClient.httpClient.post("${ApiClient.baseUrl}/register") {
                contentType(ContentType.Application.Json)
                setBody(datos)
            }

            return if (httpResponse.status == HttpStatusCode.Created) {
                val registerResponse: RegisterResponse = httpResponse.body()
                Result.success(registerResponse)
            } else {
                // 2) Si no es 201, intentamos extraer el JSON {"error": "..."}
                val errorBody: ErrorResponse = httpResponse.body()
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
            val response: HttpResponse = ApiClient.httpClient.put("${ApiClient.baseUrl}/user/change-password") {
                contentType(ContentType.Application.Json)
                setBody(ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    userId = userId
                ))

                expectSuccess = false
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    Result.success(Unit)
                }
                HttpStatusCode.BadRequest -> {
                    // Error de validación (contraseña muy débil, etc.)
                    val errorBody: ErrorResponse = try {
                        response.body()
                    } catch (_: Exception) {
                        ErrorResponse("Error de validación en los datos enviados")
                    }
                    Result.failure(Exception(errorBody.message))
                }
                HttpStatusCode.Unauthorized -> {
                    // Contraseña actual incorrecta
                    val errorBody: ErrorResponse = try {
                        response.body()
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
