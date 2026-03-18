package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserAccountRepository
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

object UserAccountRepositoryImpl : UserAccountRepository {
    override suspend fun deleteUser(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = ApiClient.apiClient.delete("${ApiClient.baseUrl}/mobile/user") {
                parameter("email", email)
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.success(Unit)
                HttpStatusCode.NotFound -> Result.failure(Exception("Usuario no encontrado"))
                else -> Result.failure(Exception("Error al eliminar usuario: código HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
