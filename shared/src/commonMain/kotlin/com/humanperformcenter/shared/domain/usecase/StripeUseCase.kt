package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.stripe.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.model.stripe.CreatePiDto
import com.humanperformcenter.shared.data.model.stripe.EphemeralKeyDto
import com.humanperformcenter.shared.data.model.stripe.StripeConfigDto
import com.humanperformcenter.shared.domain.repository.StripeRepository

class StripeUseCase (private val stripeRepository: StripeRepository) {
    suspend fun getConfig (): StripeConfigDto {
        return stripeRepository.getConfig()
    }

    suspend fun createPaymentIntent (createPaymentIntentRequest: CreatePaymentIntentRequest): CreatePiDto {
        return stripeRepository.createPaymentIntent(createPaymentIntentRequest)
    }

    suspend fun createEphemeralKey (customerId: String, apiVersion: String): EphemeralKeyDto {
        return stripeRepository.createEphemeralKey(customerId, apiVersion)
    }
}
