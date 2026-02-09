package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.model.payment.CreatePiDto
import com.humanperformcenter.shared.data.model.payment.CreateStripeCustomerResponse
import com.humanperformcenter.shared.data.model.payment.GetStripeCustomerResponse
import com.humanperformcenter.shared.data.model.payment.PaymentMethod
import com.humanperformcenter.shared.data.model.payment.PaymentMethodDto
import com.humanperformcenter.shared.data.model.payment.StripeEphemeralKeyResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentIntentResponse
import com.humanperformcenter.shared.data.model.payment.SubscriptionDto
import com.humanperformcenter.shared.domain.repository.StripeRepository

class StripeUseCase (private val stripeRepository: StripeRepository) {
    suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse> {
        return stripeRepository.createOrGetCustomer()
    }

    suspend fun getCustomer(customerId: String): Result<GetStripeCustomerResponse> {
        return stripeRepository.getCustomer(customerId)
    }

    suspend fun createEphemeralKey (customerId: String): Result<StripeEphemeralKeyResponse> {
        return stripeRepository.createEphemeralKey(customerId)
    }

    suspend fun attachPaymentMethod(paymentMethodId: String, customerId: String): Result<Unit> {
        return stripeRepository.attachPaymentMethod(paymentMethodId, customerId)
    }

    suspend fun listPaymentMethods(customerId: String): Result<List<PaymentMethodDto>> {
        return stripeRepository.listPaymentMethods(customerId)
    }

    suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit> {
        return stripeRepository.detachPaymentMethod(paymentMethodId)
    }

    suspend fun createPaymentIntent (createPaymentIntentRequest: CreatePaymentIntentRequest)
    : Result<StripePaymentIntentResponse> {
        return stripeRepository.createPaymentIntent(createPaymentIntentRequest)
    }

    suspend fun cancelPaymentIntent(id: String): Result<Unit> {
        return stripeRepository.cancelPaymentIntent(id)
    }

    suspend fun getPaymentIntent(id: String): Result<CreatePiDto> {
        return stripeRepository.getPaymentIntent(id)
    }

    suspend fun createRefund(paymentIntentId: String, amount: Int?): Result<Unit> {
        return stripeRepository.createRefund(paymentIntentId, amount)
    }

    suspend fun createSubscription(priceId: String): Result<SubscriptionDto> {
        return stripeRepository.createSubscription(priceId)
    }

    suspend fun cancelSubscription(id: String): Result<Unit> {
        return stripeRepository.cancelSubscription(id)
    }

    suspend fun getSubscription(id: String): Result<SubscriptionDto> {
        return stripeRepository.getSubscription(id)
    }

    suspend fun saveCard(paymentMethodId: String): Result<Unit> {
        return stripeRepository.saveCard(paymentMethodId)
    }

    suspend fun getUserCards(userId: Int): Result<List<PaymentMethod>> {
        return stripeRepository.getUserCards(userId)
    }

    suspend fun deleteCard(cardId: String): Result<Unit> {
        return stripeRepository.deleteCard(cardId)
    }

    suspend fun setDefaultCard(cardId: String): Result<Unit> {
        return stripeRepository.setDefaultCard(cardId)
    }

    suspend fun getPublishableKey(): Result<String> {
        return stripeRepository.getPublishableKey()
    }
}
