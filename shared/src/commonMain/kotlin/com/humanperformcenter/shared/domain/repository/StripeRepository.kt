package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.payment.*


interface StripeRepository {
    suspend fun getPublishableKey(): Result<String>

    suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse>
    suspend fun getCustomer(customerId: String): Result<GetStripeCustomerResponse>

    suspend fun createEphemeralKey(customerId: String): Result<StripeEphemeralKeyResponse>

    suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit>
    suspend fun setDefaultPaymentMethod(paymentMethodId: String, customerId: String): Result<Unit>

    suspend fun createPaymentIntent(intentRequest: CreatePaymentIntentRequest)
    : Result<StripePaymentIntentResponse>
    suspend fun createSetupConfig(userId: Int): Result<StripeSetupConfigResponse>

    /**
     * `amount` viaja en unidad mayor (p. ej. euros) y el backend la convierte a céntimos.
     * Si es `null`, el backend decide el importe (full refund).
     */
    suspend fun createRefund(paymentIntentId: String, amount: Double?): Result<Unit>

    suspend fun createSubscription(
        priceId: String,
        userId: Int,
        productId: Int,
        couponCode: String? = null
    ): Result<SubscriptionDto>
    suspend fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int): Result<Unit>
    suspend fun getSubscription(id: String): Result<SubscriptionDto>

    suspend fun getUserTransactions(): Result<List<TransactionDto>>

    suspend fun getUserCards(customerId: String): Result<StripePaymentMethodsContainer>
}
