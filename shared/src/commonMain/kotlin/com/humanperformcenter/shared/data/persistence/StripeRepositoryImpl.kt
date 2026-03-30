package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.model.payment.CreateStripeCustomerResponse
import com.humanperformcenter.shared.data.model.payment.StripeEphemeralKeyResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentIntentResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentMethodsContainer
import com.humanperformcenter.shared.data.model.payment.StripeSetupConfigResponse
import com.humanperformcenter.shared.data.model.payment.SubscriptionDto
import com.humanperformcenter.shared.data.model.payment.TransactionDto
import com.humanperformcenter.shared.data.remote.StripeRemoteDataSource
import com.humanperformcenter.shared.domain.repository.StripeRepository

class StripeRepositoryImpl(
    private val remoteDataSource: StripeRemoteDataSource,
) : StripeRepository {
    override suspend fun getPublishableKey(): Result<String> = remoteDataSource.getPublishableKey()
    override suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse> = remoteDataSource.createOrGetCustomer()
    override suspend fun createEphemeralKey(customerId: String): Result<StripeEphemeralKeyResponse> = remoteDataSource.createEphemeralKey(customerId)
    override suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit> = remoteDataSource.detachPaymentMethod(paymentMethodId)
    override suspend fun setDefaultPaymentMethod(paymentMethodId: String, customerId: String): Result<Unit> =
        remoteDataSource.setDefaultPaymentMethod(paymentMethodId, customerId)
    override suspend fun createPaymentIntent(intentRequest: CreatePaymentIntentRequest): Result<StripePaymentIntentResponse> =
        remoteDataSource.createPaymentIntent(intentRequest)
    override suspend fun createSetupConfig(userId: Int): Result<StripeSetupConfigResponse> = remoteDataSource.createSetupConfig(userId)
    override suspend fun createRefund(paymentIntentId: String, amount: Double?): Result<Unit> =
        remoteDataSource.createRefund(paymentIntentId, amount)
    override suspend fun createSubscription(priceId: String, userId: Int, productId: Int, couponCode: String?): Result<SubscriptionDto> =
        remoteDataSource.createSubscription(priceId, userId, productId, couponCode)
    override suspend fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int): Result<Unit> =
        remoteDataSource.cancelSubscription(subscriptionId, productId, userId)
    override suspend fun getUserTransactions(): Result<List<TransactionDto>> = remoteDataSource.getUserTransactions()
    override suspend fun getUserCards(customerId: String): Result<StripePaymentMethodsContainer> = remoteDataSource.getUserCards(customerId)
}
