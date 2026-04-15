package com.humanperformcenter.shared.data.remote.implementation

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.auth.ChangePasswordRequest
import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.data.model.auth.ResetPasswordRequest
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.AuthRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
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

        if (response.status != HttpStatusCode.OK) {
            throw DomainException.BadRequest(details = response.errorMessageOrFallback())
        }
        response.body<LoginResponse>()
    }

    override suspend fun register(data: RegisterRequest): Result<RegisterResponse> = runCatching {
        val parts = formData {
            data.profilePicBytes?.let { bytes ->
                val profilePicName = data.profilePicName?.takeIf { it.isNotBlank() } ?: "profile.jpg"
                val contentType = when (profilePicName.substringAfterLast('.', "").lowercase()) {
                    "png" -> "image/png"
                    "jpg", "jpeg" -> "image/jpeg"
                    "gif" -> "image/gif"
                    "webp" -> "image/webp"
                    "heic" -> "image/heic"
                    "heif" -> "image/heif"
                    else -> "application/octet-stream"
                }
                append("profile_pic", bytes, Headers.build {
                    append(HttpHeaders.ContentType, contentType)
                    append(HttpHeaders.ContentDisposition, "filename=\"$profilePicName\"")
                })
            }
            append("nombre", data.name)
            append("apellidos", data.surnames)
            append("email", data.email)
            append("telefono", data.phone)
            append("password", data.password)
            append("sexo", data.sex)
            append("fecha_nacimiento", toLegacyFechaNacimientoRaw(data.dateOfBirth))
            append("codigo_postal", data.postCode)
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

        return when (trimmed.length) {
            // yyyy-MM-dd -> ddMMyyyy
            10 if trimmed[4] == '-' && trimmed[7] == '-' -> {
                val y = trimmed.substring(0, 4)
                val m = trimmed.substring(5, 7)
                val d = trimmed.substring(8, 10)
                "$d$m$y"
            }

            // dd/MM/yyyy -> ddMMyyyy
            10 if trimmed[2] == '/' && trimmed[5] == '/' -> {
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
        if (response.status != HttpStatusCode.OK) {
            error("HTTP ${response.status.value}: ${response.errorMessageOrFallback()}")
        }
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
        if (response.status != HttpStatusCode.OK) {
            error("HTTP ${response.status.value}: ${response.errorMessageOrFallback()}")
        }
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

    private suspend fun HttpResponse.errorMessageOrFallback(): String {
        val body = bodyAsText().trim()
        if (body.isBlank()) return "HTTP ${status.value}"

        val jsonMessage = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            .orEmpty()
        if (jsonMessage.isNotBlank()) return jsonMessage

        val jsonError = Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(body)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            .orEmpty()
        if (jsonError.isNotBlank()) return jsonError

        return body.removeSurrounding("\"").trim().ifBlank { "HTTP ${status.value}" }
    }
}
