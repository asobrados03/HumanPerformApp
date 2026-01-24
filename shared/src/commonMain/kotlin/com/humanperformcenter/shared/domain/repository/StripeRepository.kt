package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.payment.*


interface StripeRepository {
    suspend fun getConfig(): StripeConfigDto
    suspend fun createPaymentIntent(CreatePaymentIntentRequest: CreatePaymentIntentRequest): CreatePiDto
    suspend fun createEphemeralKey(customerId: String, apiVersion: String): EphemeralKeyDto
}