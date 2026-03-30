package com.humanperformcenter.shared.data.remote.implementation

import com.humanperformcenter.shared.data.model.user.AssignPreferredCoachRequest
import com.humanperformcenter.shared.data.model.user.AssignPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.UserFavoritesRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UserFavoritesRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : UserFavoritesRemoteDataSource {
    override suspend fun getCoaches(): Result<List<Professional>> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/coaches").body()
    }

    override suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String> = runCatching {
        val response = clientProvider.apiClient.post("${clientProvider.baseUrl}/mobile/user/preferred-coach") {
            contentType(ContentType.Application.Json)
            setBody(AssignPreferredCoachRequest(serviceName.orEmpty(), userId ?: 0, coachId))
        }.body<AssignPreferredCoachResponse>()
        response.message
    }

    override suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/user/preferred-coach") {
            parameter("customer_id", customerId)
        }.body()
    }
}
