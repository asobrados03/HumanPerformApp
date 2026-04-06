package com.humanperformcenter.shared.data.remote.implementation

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.auth.ChangePasswordRequest
import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.data.model.auth.ResetPasswordRequest
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.AuthRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class AuthRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
    private val localDataSource: AuthLocalDataSource,
) : AuthRemoteDataSource {
    override suspend fun login(email: String, password: String): Result<LoginResponse> = runCatching {
        val response = clientProvider.authClient.post(
            "${clientProvider.baseUrl}/mobile/sessions"
        ) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "password" to password))
            expectSuccess = false
        }

        check(response.status == HttpStatusCode.OK) { "HTTP ${response.status.value}" }
        response.body<LoginResponse>()
    }

    override suspend fun register(data: RegisterRequest): Result<RegisterResponse> = runCatching {
        val parts = formData {
            data.profilePicBytes?.let { bytes ->
                append("profile_pic", bytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(
                        HttpHeaders.ContentDisposition,
                        "form-data; name=\"profile_pic\"; filename=\"${data.profilePicName}\"",
                    )
                })
            }
            append("nombre", data.name)
            append("apellidos", data.surnames)
            append("email", data.email)
            append("telefono", data.phone)
            append("password", data.password)
            append("sexo", data.sex)
            append("fecha_nacimiento", data.dateOfBirth)
            append("codigo_postal", data.postCode)
            // Backend registerUserService valida contrato legacy camelCase para todos los dispositivos.
            append("rawEmail", data.email)
            append("fechaNacimientoRaw", toLegacyFechaNacimientoRaw(data.dateOfBirth))
            append("codigoPostal", data.postCode)
            append("direccionPostal", data.postAddress)
            append("deviceType", data.deviceType)
            append("direccion_postal", data.postAddress)
            append("dni", data.dni)
            append("device_type", data.deviceType)
        }

        clientProvider.authClient.post("${clientProvider.baseUrl}/mobile/users") {
            setBody(MultiPartFormDataContent(parts))
        }.body()
    }

    internal fun toLegacyFechaNacimientoRaw(dateOfBirth: String): String {
        val trimmed = dateOfBirth.trim()
        if (trimmed.isEmpty()) return trimmed

        return when {
            // yyyy-MM-dd -> ddMMyyyy
            trimmed.length == 10 && trimmed[4] == '-' && trimmed[7] == '-' -> {
                val y = trimmed.substring(0, 4)
                val m = trimmed.substring(5, 7)
                val d = trimmed.substring(8, 10)
                "$d$m$y"
            }

            // dd/MM/yyyy -> ddMMyyyy
            trimmed.length == 10 && trimmed[2] == '/' && trimmed[5] == '/' -> {
                val d = trimmed.substring(0, 2)
                val m = trimmed.substring(3, 5)
                val y = trimmed.substring(6, 10)
                "$d$m$y"
            }

            else -> trimmed.filter { it.isDigit() }.ifEmpty { trimmed }
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        val response = clientProvider.apiClient.put(
            "${clientProvider.baseUrl}/mobile/reset-password"
        ) {
            contentType(ContentType.Application.Json)
            setBody(ResetPasswordRequest(email))
            expectSuccess = false
        }
        check(response.status == HttpStatusCode.OK) { "HTTP ${response.status.value}" }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        userId: Int,
    ): Result<Unit> = runCatching {
        val response = clientProvider.apiClient.put(
            "${clientProvider.baseUrl}/mobile/change-password"
        ) {
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(currentPassword, newPassword, userId))
            expectSuccess = false
        }
        check(response.status == HttpStatusCode.OK) { "HTTP ${response.status.value}" }
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        val accessToken = localDataSource.getAccessToken().orEmpty()
        val response = clientProvider.authClient.delete(
            "${clientProvider.baseUrl}/mobile/sessions/current"
        ) {
            bearerAuth(accessToken)
            expectSuccess = false
        }
        check(response.status.value in 200..299 || response.status == HttpStatusCode.Unauthorized) {
            "HTTP ${response.status.value}"
        }
    }
}
