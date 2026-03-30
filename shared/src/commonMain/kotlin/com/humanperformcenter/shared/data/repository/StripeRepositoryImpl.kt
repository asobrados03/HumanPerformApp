package com.humanperformcenter.shared.data.repository

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
    private val remote: StripeRemoteDataSource,
) : StripeRepository {
    override suspend fun getPublishableKey(): Result<String> = remote.getPublishableKey().mapDomainError()
    override suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse> = remote.createOrGetCustomer().mapDomainError()
    override suspend fun createEphemeralKey(customerId: String): Result<StripeEphemeralKeyResponse> = remote.createEphemeralKey(customerId).mapDomainError()
    override suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit> = remote.detachPaymentMethod(paymentMethodId).mapDomainError()
    override suspend fun setDefaultPaymentMethod(paymentMethodId: String, customerId: String): Result<Unit> =
        remote.setDefaultPaymentMethod(paymentMethodId, customerId).mapDomainError()
    override suspend fun createPaymentIntent(intentRequest: CreatePaymentIntentRequest): Result<StripePaymentIntentResponse> =
        remote.createPaymentIntent(intentRequest).mapDomainError()
    override suspend fun createSetupConfig(userId: Int): Result<StripeSetupConfigResponse> = remote.createSetupConfig(userId).mapDomainError()
    override suspend fun createRefund(paymentIntentId: String, amount: Double?): Result<Unit> =
        remote.createRefund(paymentIntentId, amount).mapDomainError()
    override suspend fun createSubscription(priceId: String, userId: Int, productId: Int, couponCode: String?): Result<SubscriptionDto> =
        remote.createSubscription(priceId, userId, productId, couponCode).mapDomainError()
    override suspend fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int): Result<Unit> =
        remote.cancelSubscription(subscriptionId, productId, userId).mapDomainError()
    override suspend fun getUserTransactions(): Result<List<TransactionDto>> = remote.getUserTransactions().mapDomainError()
    override suspend fun getUserCards(customerId: String): Result<StripePaymentMethodsContainer> = remote.getUserCards(customerId).mapDomainError()
}
