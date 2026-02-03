package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.payment.*
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.StripeRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.collections.mapOf

object StripeRepositoryImpl : StripeRepository {

    // ==================== CLIENTES ====================
    override suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse> = runCatching {
        ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/customer").body()
    }

    override suspend fun getCustomer(customerId: String): Result<GetStripeCustomerResponse> = runCatching {
        ApiClient.apiClient.get("${ApiClient.baseUrl}/stripe/customer/$customerId").body()
    }

    // ==================== EPHEMERAL KEYS ====================
    override suspend fun createEphemeralKey(customerId: String, apiVersion: String)
    : Result<EphemeralKeyDto> = runCatching {

        ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/ephemeral-keys") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("customer_id" to customerId, "apiVersion" to apiVersion))
        }.body()
    }

    // ==================== PAYMENT METHODS ====================
    override suspend fun attachPaymentMethod(paymentMethodId: String, customerId: String)
    : Result<Unit> = runCatching {
        ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/payment-method/attach") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("paymentMethodId" to paymentMethodId, "customerId" to customerId))
        }.body()
    }

    override suspend fun listPaymentMethods(customerId: String)
    : Result<List<PaymentMethodDto>> = runCatching {
        ApiClient.apiClient.get("${ApiClient.baseUrl}/stripe/payment-methods/$customerId").body()
    }

    override suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit> = runCatching {
        ApiClient.apiClient.delete(
            "${ApiClient.baseUrl}/stripe/payment-method/$paymentMethodId"
        ).body()
    }

    // ==================== PAYMENT INTENTS ====================
    override suspend fun createPaymentIntent(createPaymentIntentRequest: CreatePaymentIntentRequest)
    : Result<CreatePiDto> = runCatching {
        ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/payment-intent") {
            contentType(ContentType.Application.Json)
            setBody(createPaymentIntentRequest)
        }.body()
    }

    override suspend fun confirmPaymentIntent(id: String): Result<Unit> = runCatching {
        ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/payment-intent/$id/confirm")
            .body()
    }

    override suspend fun cancelPaymentIntent(id: String): Result<Unit> = runCatching {
        ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/payment-intent/$id/cancel")
            .body()
    }

    override suspend fun getPaymentIntent(id: String): Result<CreatePiDto> {
        TODO("Not yet implemented")
    }

    // ==================== COMPRA DE PRODUCTO ====================
    override suspend fun purchaseProduct(productId: Int, paymentMethodId: String)
    : Result<Unit> = runCatching {
        ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/purchase") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("product_id" to productId, "paymentMethodId" to paymentMethodId))
        }.body()
    }

    // ==================== REEMBOLSOS ====================
    override suspend fun createRefund(paymentIntentId: String, amount: Int?)
    : Result<Unit> = runCatching {
        ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/refund") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("paymentIntentId" to paymentIntentId, "amount" to amount))
        }.body()
    }

    // ==================== SUSCRIPCIONES ====================
    override suspend fun createSubscription(priceId: String)
    : Result<SubscriptionDto> = runCatching {
        ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/subscription") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("priceId" to priceId))
        }.body()
    }

    override suspend fun cancelSubscription(id: String): Result<Unit> = runCatching {
        ApiClient.apiClient.delete("${ApiClient.baseUrl}/stripe/subscription/$id").body()
    }

    override suspend fun getSubscription(id: String): Result<SubscriptionDto> = runCatching {
        ApiClient.apiClient.get("${ApiClient.baseUrl}/stripe/subscription/$id").body()
    }

    // ==================== TRANSACCIONES ====================
    override suspend fun getUserTransactions(): Result<List<TransactionDto>> = runCatching {
        ApiClient.apiClient.get("${ApiClient.baseUrl}/stripe/transactions").body()
    }

    // ==================== TARJETAS ====================
    override suspend fun saveCard(paymentMethodId: String): Result<Unit> = runCatching {
        ApiClient.apiClient.post("${ApiClient.baseUrl}/stripe/cards") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("paymentMethodId" to paymentMethodId))
        }.body()
    }

    override suspend fun getUserCards(): Result<List<CardDto>> = runCatching {
        ApiClient.apiClient.get("${ApiClient.baseUrl}/stripe/cards").body()
    }

    override suspend fun deleteCard(cardId: String): Result<Unit> = runCatching {
        ApiClient.apiClient.delete("${ApiClient.baseUrl}/stripe/cards/$cardId").body()
    }

    override suspend fun setDefaultCard(cardId: String): Result<Unit> = runCatching {
        ApiClient.apiClient.put("${ApiClient.baseUrl}/stripe/cards/$cardId/default").body()
    }
}
