package com.humanperformcenter.shared.data.remote.impl

import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.UserAccountRemoteDataSource
import io.ktor.client.request.delete
import io.ktor.client.request.parameter

class UserAccountRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : UserAccountRemoteDataSource {
    override suspend fun deleteUser(email: String): Result<Unit> = runCatching {
        clientProvider.apiClient.delete("${clientProvider.baseUrl}/mobile/user") { parameter("email", email) }
        Unit
    }
}
