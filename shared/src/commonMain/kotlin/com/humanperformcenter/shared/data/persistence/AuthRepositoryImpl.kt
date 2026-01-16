package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ChangePasswordRequest
import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.data.model.RegisterResponse
import com.humanperformcenter.shared.data.model.ResetPasswordRequest
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

object AuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): Result<LoginResponse> = try {
        val resp: HttpResponse = ApiClient.authClient.post("${ApiClient.baseUrl}/mobile/login") {
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
                    postAddress = data.postAddress,
                    dni = data.dni,
                    profilePictureName = data.profilePictureName
                )
                SecureStorage.saveTokens(data.accessToken, data.refreshToken)
                SecureStorage.saveUser(userData)
                Result.success(data)
            }
            HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized -> {
                val err: ErrorResponse = resp.body() ?: ErrorResponse("Credenciales inválidas")
                Result.failure(Exception(err.error))
            }
            else -> Result.failure(Exception("Error al iniciar sesión"))
        }
    } catch (err: Throwable) {
        Result.failure(err)
    }

    override suspend fun register(data: RegisterRequest): Result<RegisterResponse> {
        return try {
            // 1) Construye la lista de partes
            val parts = formData {
                data.profilePicBytes?.let { bytes ->
                    append("profile_pic", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition,
                            "filename=\"${data.profilePicName}\"")
                    })
                }
                append("nombre",       data.nombre)
                append("apellidos",    data.apellidos)
                append("email",        data.email)
                append("telefono",     data.telefono)
                append("password",     data.password)
                append("sexo",         data.sexo)
                append("fecha_nacimiento", data.fechaNacimiento) // ddMMyyyy
                append("codigo_postal",   data.codigoPostal)
                append("direccion_postal", data.direccionPostal)
                append("dni",             data.dni)
                append("device_type",      data.deviceType)
            }

            // 2) Envío con MULTIPART, sin tocar el Content-Type manualmente
            val response: HttpResponse = ApiClient.authClient.post("${ApiClient.baseUrl}/mobile/register") {
                setBody(MultiPartFormDataContent(parts))
            }

            // 3) Procesa la respuesta
            when (response.status) {
                HttpStatusCode.Created -> {
                    Result.success(response.body())
                }
                HttpStatusCode.InternalServerError -> {
                    Result.failure(Exception("Ha habido un error interno. Vuelva a intentarlo."))
                }
                else -> {
                    val err = response.body<ErrorResponse>()
                    Result.failure(Exception(err.error))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al registrar: ${e.message}"))
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            val resp: HttpResponse = ApiClient.apiClient.put("${ApiClient.baseUrl}/mobile/reset-password") {
                contentType(ContentType.Application.Json)
                setBody(ResetPasswordRequest(
                    email = email
                ))

                expectSuccess = false
            }

            when (resp.status) {
                HttpStatusCode.OK -> {
                    Result.success(Unit)
                }
                HttpStatusCode.NotFound -> {
                    // Error de validación (contraseña muy débil, etc.)
                    val errorBody: ErrorResponse = try {
                        resp.body()
                    } catch (_: Exception) {
                        ErrorResponse("Error no se ha encontrado el usuario asociado al email")
                    }
                    Result.failure(Exception(errorBody.error))
                }
                else -> {
                    Result.failure(Exception("Error al restablecer la contraseña. Inténtalo más tarde"))
                }
            }
        } catch (e: ClientRequestException) {
            val errorMessage = try {
                e.response.body<ErrorResponse>().error
            } catch (_: Exception) {
                when (e.response.status) {
                    HttpStatusCode.NotFound -> "Usuario no encontrado"
                    else -> "Error en la solicitud"
                }
            }

            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Error de conexión. Revisa tu conexión a internet e inténtalo de nuevo"))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int): Result<Unit> {
        return try {
            val resp: HttpResponse = ApiClient.apiClient.put("${ApiClient.baseUrl}/mobile/change-password") {
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
                    Result.failure(Exception(errorBody.error))
                }
                HttpStatusCode.Unauthorized -> {
                    // Contraseña actual incorrecta
                    val errorBody: ErrorResponse = try {
                        resp.body()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ErrorResponse("Contraseña actual incorrecta")
                    }
                    Result.failure(Exception(errorBody.error))
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
                e.response.body<ErrorResponse>().error
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
