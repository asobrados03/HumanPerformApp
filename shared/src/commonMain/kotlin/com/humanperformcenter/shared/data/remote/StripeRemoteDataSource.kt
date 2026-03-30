package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.model.payment.CreateStripeCustomerResponse
import com.humanperformcenter.shared.data.model.payment.StripeEphemeralKeyResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentIntentResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentMethodsContainer
import com.humanperformcenter.shared.data.model.payment.StripeSetupConfigResponse
import com.humanperformcenter.shared.data.model.payment.SubscriptionDto
import com.humanperformcenter.shared.data.model.payment.TransactionDto

interface StripeRemoteDataSource {
    suspend fun getPublishableKey(): Result<String>
    suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse>
    suspend fun createEphemeralKey(customerId: String): Result<StripeEphemeralKeyResponse>
    suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit>
    suspend fun setDefaultPaymentMethod(paymentMethodId: String, customerId: String): Result<Unit>
    suspend fun createPaymentIntent(intentRequest: CreatePaymentIntentRequest): Result<StripePaymentIntentResponse>
    suspend fun createSetupConfig(userId: Int): Result<StripeSetupConfigResponse>
    suspend fun createRefund(paymentIntentId: String, amount: Double?): Result<Unit>
    suspend fun createSubscription(
        priceId: String,
        userId: Int,
        productId: Int,
        couponCode: String? = null,
    ): Result<SubscriptionDto>
    suspend fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int): Result<Unit>
    suspend fun getUserTransactions(): Result<List<TransactionDto>>
    suspend fun getUserCards(customerId: String): Result<StripePaymentMethodsContainer>
}
