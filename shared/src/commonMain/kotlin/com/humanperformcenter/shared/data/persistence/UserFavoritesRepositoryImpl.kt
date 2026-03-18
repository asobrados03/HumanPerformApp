package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.user.AssignPreferredCoachRequest
import com.humanperformcenter.shared.data.model.user.AssignPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserFavoritesRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

object UserFavoritesRepositoryImpl : UserFavoritesRepository {
    override suspend fun getCoaches(): Result<List<Professional>> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/coaches") {
                contentType(ContentType.Application.Json)
                expectSuccess = false
            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error al leer entrenadores: código HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String> = withContext(Dispatchers.IO) {
        if (userId == null) {
            return@withContext Result.failure(
                IllegalStateException("No se pudo marcar favorito: usuario no disponible")
            )
        }

        try {
            val response: HttpResponse = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/user/preferred-coach") {
                contentType(ContentType.Application.Json)
                setBody(
                    AssignPreferredCoachRequest(
                        serviceName = serviceName.orEmpty(),
                        customerId = userId,
                        coachId = coachId
                    )
                )
            }
            val bodyResponse: AssignPreferredCoachResponse = response.body()
            Result.success(bodyResponse.message)
        } catch (e: ClientRequestException) {
            Result.failure(RuntimeException("Error de cliente: ${e.response.status}"))
        } catch (e: ServerResponseException) {
            Result.failure(RuntimeException("Error de servidor: ${e.response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user/preferred-coach") {
                contentType(ContentType.Application.Json)
                parameter("customer_id", customerId)
            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                val errorResponse = response.body<ErrorResponse>()
                Result.failure(RuntimeException(errorResponse.error))
            }
        } catch (e: ClientRequestException) {
            Result.failure(RuntimeException("Error de cliente: ${e.response.status}"))
        } catch (e: ServerResponseException) {
            Result.failure(RuntimeException("Error de servidor: ${e.response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
