package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.payment.*


interface StripeRepository {
    suspend fun getPublishableKey(): Result<String>

    suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse>
    suspend fun getCustomer(customerId: String): Result<GetStripeCustomerResponse>

    suspend fun createEphemeralKey(customerId: String): Result<StripeEphemeralKeyResponse>

    suspend fun attachPaymentMethod(paymentMethodId: String, customerId: String): Result<Unit>
    suspend fun listPaymentMethods(customerId: String): Result<List<PaymentMethodDto>>
    suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit>

    suspend fun createPaymentIntent(intentRequest: CreatePaymentIntentRequest)
    : Result<StripePaymentIntentResponse>
    suspend fun cancelPaymentIntent(id: String): Result<Unit>
    suspend fun getPaymentIntent(id: String): Result<CreatePiDto>

    suspend fun createRefund(paymentIntentId: String, amount: Int?): Result<Unit>

    suspend fun createSubscription(priceId: String): Result<SubscriptionDto>
    suspend fun cancelSubscription(id: String): Result<Unit>
    suspend fun getSubscription(id: String): Result<SubscriptionDto>

    suspend fun getUserTransactions(): Result<List<TransactionDto>>

    suspend fun saveCard(paymentMethodId: String): Result<Unit>
    suspend fun getUserCards(userId: Int): Result<List<PaymentMethod>>
    suspend fun deleteCard(cardId: String): Result<Unit>
    suspend fun setDefaultCard(cardId: String): Result<Unit>
}