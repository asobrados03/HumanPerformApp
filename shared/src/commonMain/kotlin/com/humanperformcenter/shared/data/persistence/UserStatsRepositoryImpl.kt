package com.humanperformcenter.shared.data.persistence

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserStatsRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

object UserStatsRepositoryImpl : UserStatsRepository {
    private val log = logging()

    override suspend fun getUserStats(customerId: Int): Result<UserStatistics> = withContext(Dispatchers.IO) {
        try {
            if (customerId <= 0) {
                return@withContext Result.failure(Exception("ID de usuario inválido"))
            }

            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/users/$customerId/stats") {
                expectSuccess = false
            }

            if (response.status.value in 200..299) {
                Result.success(response.body())
            } else {
                val errorBody = runCatching { response.bodyAsText() }.getOrNull().orEmpty()
                log.error { "🔴 Error API stats ${response.status}: $errorBody" }
                val detail = errorBody.ifBlank { "sin detalle" }
                Result.failure(Exception("Error HTTP ${response.status.value}: $detail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
