package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.payment.*


interface StripeRepository {
    suspend fun getConfig(): Result<StripeConfigDto>
    suspend fun createPaymentIntent(createPaymentIntentRequest: CreatePaymentIntentRequest): Result<CreatePiDto>
    suspend fun createEphemeralKey(customerId: String, apiVersion: String): Result<EphemeralKeyDto>
}