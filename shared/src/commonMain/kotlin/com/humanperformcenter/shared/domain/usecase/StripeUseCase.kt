package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.model.payment.CreatePiDto
import com.humanperformcenter.shared.data.model.payment.EphemeralKeyDto
import com.humanperformcenter.shared.data.model.payment.StripeConfigDto
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
