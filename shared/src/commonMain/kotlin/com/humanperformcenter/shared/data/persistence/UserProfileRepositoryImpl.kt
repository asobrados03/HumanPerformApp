package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserProfileRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object UserProfileRepositoryImpl : UserProfileRepository {
    override suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> =
        withContext(Dispatchers.IO) {
            val userJson = Json.encodeToString(User.serializer(), user)
            val formData = formData {
                append(
                    key = "user",
                    value = userJson,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                )

                profilePicBytes?.let { bytes ->
                    append("profile_pic", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"${user.profilePictureName}\"")
                    })
                }
            }

            val response: HttpResponse = ApiClient.apiClient.put("${ApiClient.baseUrl}/mobile/user") {
                setBody(MultiPartFormDataContent(formData))
            }

            if (response.status == HttpStatusCode.OK) {
                val updatedUser: User = response.body()
                SecureStorage.saveUser(updatedUser)
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Error al actualizar usuario: código HTTP ${response.status.value}"))
            }
        }

    override suspend fun getUserById(id: Int): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user") {
                contentType(ContentType.Application.Json)
                parameter("user_id", id)
            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error al leer el usuario: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = ApiClient.apiClient.delete("${ApiClient.baseUrl}/mobile/user/photo") {
                url {
                    parameters.append("profilePictureName", req.profilePictureName.toString())
                    parameters.append("email", req.email)
                }
            }

            when (response.status) {
                HttpStatusCode.OK -> Result.success(response.body())
                HttpStatusCode.NotFound -> Result.failure(Exception(response.body<ErrorResponse>().error))
                else -> Result.failure(
                    Exception("Error al eliminar la foto de perfil: código HTTP ${response.status.value}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
