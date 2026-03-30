package com.humanperformcenter.shared.data.remote.implementation

import com.humanperformcenter.shared.data.model.payment.EwalletResponse
import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.UserWalletRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class UserWalletRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : UserWalletRemoteDataSource {
    override suspend fun getEwalletBalance(userId: Int): Result<Double?> = runCatching {
        val response = clientProvider.apiClient.get(
            "${clientProvider.baseUrl}/mobile/user/e-wallet-balance"
        ) {
            parameter("user_id", userId)
        }
        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        json["balance"]?.jsonPrimitive?.doubleOrNull
    }

    override suspend fun getEwalletTransactions(userId: Int)
    : Result<List<EwalletTransaction>> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/user/transactions") {
            parameter("user_id", userId)
        }.body<EwalletResponse>().transactions
    }
}
