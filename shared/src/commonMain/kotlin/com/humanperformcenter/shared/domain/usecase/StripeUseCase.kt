package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.data.model.Stripe.*
import com.humanperformcenter.shared.domain.repository.StripeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class StripeUseCase (private val stripeRepository: StripeRepository) {
    suspend fun getConfig (): StripeConfigDto = withContext(Dispatchers.IO) {
        return@withContext stripeRepository.getConfig()
    }
    suspend fun createPaymentIntent (CreatePaymentIntentRequest: CreatePaymentIntentRequest): CreatePiDto {
        return withContext(Dispatchers.IO) {
            return@withContext stripeRepository.createPaymentIntent(CreatePaymentIntentRequest)
        }
    }
    suspend fun createEphemeralKey (customerId: String, apiVersion: String): EphemeralKeyDto {
        return withContext(Dispatchers.IO) {
            return@withContext stripeRepository.createEphemeralKey(customerId, apiVersion)
        }
    }
}