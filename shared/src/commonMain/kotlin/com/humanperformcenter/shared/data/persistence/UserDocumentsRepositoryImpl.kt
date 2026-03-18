package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.user.UploadResponse
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.data.persistence.common.safeErrorBody
import com.humanperformcenter.shared.domain.repository.UserDocumentsRepository
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

object UserDocumentsRepositoryImpl : UserDocumentsRepository {
    override suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String> =
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

                when (response.status) {
                    HttpStatusCode.Created, HttpStatusCode.OK -> response.body<UploadResponse>().message
                    HttpStatusCode.BadRequest -> throw Exception("Error en la solicitud: ${response.safeErrorBody()}")
                    HttpStatusCode.Unauthorized -> throw Exception("No autorizado. Verifica tu token")
                    HttpStatusCode.Forbidden -> throw Exception("Acceso denegado")
                    HttpStatusCode.RequestHeaderFieldTooLarge -> throw Exception("El archivo es demasiado grande")
                    HttpStatusCode.UnsupportedMediaType -> throw Exception("Tipo no soportado: $contentType")
                    HttpStatusCode.InternalServerError -> throw Exception("Error interno del servidor")
                    else -> throw Exception("HTTP ${response.status.value} → ${response.safeErrorBody()}")
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
}
