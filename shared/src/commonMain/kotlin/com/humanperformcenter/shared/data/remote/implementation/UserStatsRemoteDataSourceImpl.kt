package com.humanperformcenter.shared.data.remote.implementation

import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.UserStatsRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.request.get

class UserStatsRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : UserStatsRemoteDataSource {
    override suspend fun getUserStats(customerId: Int): Result<UserStatistics> = runCatching {
        clientProvider.apiClient.get(
            "${clientProvider.baseUrl}/mobile/users/$customerId/stats"
        ).body()
    }
}
