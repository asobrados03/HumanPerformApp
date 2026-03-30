package com.humanperformcenter.shared.data.remote.implementation

import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.model.payment.CreateRefundRequest
import com.humanperformcenter.shared.data.model.payment.CreateStripeCustomerResponse
import com.humanperformcenter.shared.data.model.payment.PublishableKeyResponse
import com.humanperformcenter.shared.data.model.payment.StripeEphemeralKeyResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentIntentResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentMethodsContainer
import com.humanperformcenter.shared.data.model.payment.StripePaymentMethodsResponse
import com.humanperformcenter.shared.data.model.payment.StripeSetupConfigResponse
import com.humanperformcenter.shared.data.model.payment.SubscriptionDto
import com.humanperformcenter.shared.data.model.payment.TransactionDto
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.StripeRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class StripeRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : StripeRemoteDataSource {
    override suspend fun getPublishableKey(): Result<String> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/stripe/publishable-key").body<PublishableKeyResponse>().publishableKey.trim()
    }

    override suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse> = runCatching {
        clientProvider.apiClient.post("${clientProvider.baseUrl}/stripe/customer").body()
    }

    override suspend fun createEphemeralKey(customerId: String): Result<StripeEphemeralKeyResponse> = runCatching {
        clientProvider.apiClient.post("${clientProvider.baseUrl}/stripe/ephemeral-keys") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("customer_id" to customerId))
        }.body()
    }

    override suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit> = runCatching {
        clientProvider.apiClient.delete("${clientProvider.baseUrl}/stripe/payment-method/$paymentMethodId")
    }

    override suspend fun setDefaultPaymentMethod(paymentMethodId: String, customerId: String): Result<Unit> = runCatching {
        clientProvider.apiClient.put("${clientProvider.baseUrl}/stripe/payment-method/default") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("paymentMethodId" to paymentMethodId, "customerId" to customerId))
        }
    }

    override suspend fun createPaymentIntent(intentRequest: CreatePaymentIntentRequest): Result<StripePaymentIntentResponse> = runCatching {
        clientProvider.apiClient.post("${clientProvider.baseUrl}/stripe/payment-intents") {
            contentType(ContentType.Application.Json)
            setBody(intentRequest)
        }.body()
    }

    override suspend fun createSetupConfig(userId: Int): Result<StripeSetupConfigResponse> = runCatching {
        clientProvider.apiClient.post("${clientProvider.baseUrl}/stripe/payments/setup-config") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("user_id" to userId))
        }.body()
    }

    override suspend fun createRefund(paymentIntentId: String, amount: Double?): Result<Unit> = runCatching {
        clientProvider.apiClient.post("${clientProvider.baseUrl}/stripe/refund") {
            contentType(ContentType.Application.Json)
            setBody(CreateRefundRequest(paymentIntentId, amount))
        }
    }

    override suspend fun createSubscription(priceId: String, userId: Int, productId: Int, couponCode: String?): Result<SubscriptionDto> = runCatching {
        clientProvider.apiClient.post("${clientProvider.baseUrl}/stripe/subscription") {
            contentType(ContentType.Application.Json)
            setBody(buildMap {
                put("priceId", priceId)
                put("userId", userId.toString())
                put("productId", productId.toString())
                if (!couponCode.isNullOrBlank()) put("couponCode", couponCode)
            })
        }.body()
    }

    override suspend fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int): Result<Unit> = runCatching {
        clientProvider.apiClient.delete("${clientProvider.baseUrl}/stripe/subscription/$subscriptionId") {
            url {
                parameters.append("user_id", userId.toString())
                parameters.append("product_id", productId.toString())
            }
        }
    }

    override suspend fun getUserTransactions(): Result<List<TransactionDto>> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/stripe/transactions").body()
    }

    override suspend fun getUserCards(customerId: String): Result<StripePaymentMethodsContainer> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/stripe/payment-methods/$customerId").body<StripePaymentMethodsResponse>().data
    }
}
