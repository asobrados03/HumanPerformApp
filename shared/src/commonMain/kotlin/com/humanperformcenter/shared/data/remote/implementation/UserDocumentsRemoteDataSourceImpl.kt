package com.humanperformcenter.shared.data.remote.implementation

import com.humanperformcenter.shared.data.model.user.UploadResponse
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.UserDocumentsRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class UserDocumentsRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : UserDocumentsRemoteDataSource {
    override suspend fun uploadDocument(userId: Int, name: String, data: ByteArray)
    : Result<String> = runCatching {
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

        clientProvider.apiClient.post(
            "${clientProvider.baseUrl}/mobile/users/$userId/documents"
        ) {
            setBody(MultiPartFormDataContent(parts))
        }.body<UploadResponse>().message
    }
}
