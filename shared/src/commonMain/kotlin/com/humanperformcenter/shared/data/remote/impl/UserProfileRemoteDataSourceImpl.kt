package com.humanperformcenter.shared.data.remote.impl

import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.UserProfileRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

class UserProfileRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : UserProfileRemoteDataSource {
    override suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> = runCatching {
        val userJson = Json.encodeToString(User.serializer(), user)
        val body = formData {
            append("user", userJson, Headers.build {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            })
            profilePicBytes?.let { bytes ->
                append("profile_pic", bytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"${user.profilePictureName}\"")
                })
            }
        }

        clientProvider.apiClient.put("${clientProvider.baseUrl}/mobile/user") {
            setBody(MultiPartFormDataContent(body))
        }.body()
    }

    override suspend fun getUserById(id: Int): Result<User> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/user") {
            parameter("user_id", id)
        }.body()
    }

    override suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> = runCatching {
        clientProvider.apiClient.delete("${clientProvider.baseUrl}/mobile/user/photo") {
            url {
                parameters.append("profilePictureName", req.profilePictureName.toString())
                parameters.append("email", req.email)
            }
        }
        Unit
    }
}
