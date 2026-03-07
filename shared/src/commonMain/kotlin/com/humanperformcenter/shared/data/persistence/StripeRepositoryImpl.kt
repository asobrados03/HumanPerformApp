package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.payment.*
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.StripeRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.collections.mapOf

object StripeRepositoryImpl : StripeRepository {
    override suspend fun getPublishableKey(): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val response = ApiClient.apiClient
                .get("${ApiClient.baseUrl}/stripe/publishable-key")
                .body<PublishableKeyResponse>()

            response.publishableKey.trim()
        }
    }

    override suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse> = runCatching {
        withContext(Dispatchers.IO) {
            ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/customer").body()
        }
    }

    override suspend fun getCustomer(customerId: String): Result<GetStripeCustomerResponse> = runCatching {
        withContext(Dispatchers.IO) {
            ApiClient.apiClient.get("${ApiClient.baseUrl}/stripe/customer/$customerId").body()
        }
    }

    override suspend fun createEphemeralKey(customerId: String)
    : Result<StripeEphemeralKeyResponse> = runCatching {
        withContext(Dispatchers.IO) {
            ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/ephemeral-keys") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("customer_id" to customerId))
            }.body()
        }
    }

    override suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            ApiClient.apiClient.delete(
                "${ApiClient.baseUrl}/stripe/payment-method/$paymentMethodId"
            ).body()
        }
    }

    override suspend fun setDefaultPaymentMethod(
        paymentMethodId: String,
        customerId: String
    ): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.IO) {
                ApiClient.apiClient.put("${ApiClient.baseUrl}/stripe/payment-method/default") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("paymentMethodId" to paymentMethodId, "customerId" to customerId))
                }.body()
            }
        }
    }

    override suspend fun createPaymentIntent(intentRequest: CreatePaymentIntentRequest)
    : Result<StripePaymentIntentResponse> = runCatching {
        withContext(Dispatchers.IO) {
            ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/payment-intent") {
                contentType(ContentType.Application.Json)
                setBody(intentRequest)
            }.body()
        }
    }

    override suspend fun createSetupConfig(userId: Int)
    : Result<StripeSetupConfigResponse> = runCatching {
        withContext(Dispatchers.IO) {
            ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/payments/setup-config") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("user_id" to userId))
            }.body()
        }
    }

    override suspend fun createRefund(paymentIntentId: String, amount: Double?)
    : Result<Unit> = runCatching {
        require(amount == null || amount > 0.0) {
            "Refund amount must be a positive amount"
        }

        withContext(Dispatchers.IO) {
            ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/refund") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateRefundRequest(
                        paymentIntentId = paymentIntentId,
                        amount = amount
                    )
                )
            }.body()
        }
    }

    override suspend fun createSubscription(
        priceId: String,
        userId: Int,
        productId: Int,
        couponCode: String?
    ): Result<SubscriptionDto> = runCatching {
        withContext(Dispatchers.IO) {
            ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/subscription") {
                contentType(ContentType.Application.Json)
                setBody(buildMap {
                    put("priceId", priceId)
                    put("userId", userId.toString())
                    put("productId", productId.toString())
                    if (!couponCode.isNullOrBlank()) {
                        put("couponCode", couponCode)
                    }
                })
            }.body()
        }
    }

    override suspend fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int)
    : Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            ApiClient.apiClient.delete(
                "${ApiClient.baseUrl}/stripe/subscription/$subscriptionId"
            ){
                url {
                    parameters.append("user_id", userId.toString())
                    parameters.append("product_id", productId.toString())
                }
            }.body()
        }
    }

    override suspend fun getSubscription(id: String): Result<SubscriptionDto> = runCatching {
        withContext(Dispatchers.IO)  {
            ApiClient.apiClient.get("${ApiClient.baseUrl}/stripe/subscription/$id").body()
        }
    }

    override suspend fun getUserTransactions(): Result<List<TransactionDto>> = runCatching {
        withContext(Dispatchers.IO) {
            ApiClient.apiClient.get("${ApiClient.baseUrl}/stripe/transactions").body()
        }
    }

    override suspend fun getUserCards(customerId: String): Result<StripePaymentMethodsContainer> = runCatching {
        withContext(Dispatchers.IO) {
            val response: StripePaymentMethodsResponse = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/stripe/payment-methods/$customerId"
            ).body()

            response.data // Esto ahora devuelve el objeto StripePaymentMethodsContainer
        }
    }
}
