package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.payment.*
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.data.network.ApiClient.baseUrl
import com.humanperformcenter.shared.domain.repository.StripeRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.collections.mapOf

object StripeRepositoryImpl: StripeRepository {
    override suspend fun getConfig(): Result<StripeConfigDto> =
        ApiClient.apiClient.get("$baseUrl/stripe/mobile/config").body()

    override suspend fun createPaymentIntent(createPaymentIntentRequest: CreatePaymentIntentRequest)
    : Result<CreatePiDto> {
        return ApiClient.apiClient.post("$baseUrl/stripe/mobile/create-payment-intent") {
            contentType(ContentType.Application.Json)
            setBody(createPaymentIntentRequest)
        }.body()
    }

    override suspend fun createEphemeralKey(
        customerId: String,
        apiVersion: String
    ): Result<EphemeralKeyDto> {
        return ApiClient.apiClient.post("$baseUrl/stripe/mobile/ephemeral-keys") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("customer_id" to customerId, "apiVersion" to apiVersion))
        }.body()
    }
}