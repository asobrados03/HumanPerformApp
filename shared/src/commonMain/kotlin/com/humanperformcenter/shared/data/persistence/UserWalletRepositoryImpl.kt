package com.humanperformcenter.shared.data.persistence

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.payment.EwalletResponse
import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserWalletRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object UserWalletRepositoryImpl : UserWalletRepository {
    private val log = logging()

    override suspend fun getEwalletBalance(userId: Int): Result<Double?> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user/e-wallet-balance") {
                url {
                    parameters.append("user_id", userId.toString())
                }
            }

            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            val balance = json["balance"]?.jsonPrimitive?.doubleOrNull
            Result.success(balance)
        } catch (e: Exception) {
            log.error { "❌ Error al obtener saldo e-wallet para userId=$userId: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>> = runCatching {
        withContext(Dispatchers.IO) {
            val response: EwalletResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user/transactions") {
                parameter("user_id", userId)
            }.body()

            response.transactions
        }
    }
}
