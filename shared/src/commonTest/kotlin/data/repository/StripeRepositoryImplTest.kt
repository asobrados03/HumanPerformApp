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
import com.humanperformcenter.shared.domain.DomainException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StripeRepositoryImplTest {

    @Test
    fun getpublishablekey_when_success_returns_key() = runTest {
        val remote = FakeStripeRemoteDataSource(publishableKeyResult = Result.success("pk_test_123"))
        val repository = StripeRepositoryImpl(remote)

        val result = repository.getPublishableKey()

        assertTrue(result.isSuccess)
        assertEquals("pk_test_123", result.getOrNull())
    }

    @Test
    fun getpublishablekey_when_backend_error_maps_to_domain_server() = runTest {
        val remote = FakeStripeRemoteDataSource(
            publishableKeyResult = Result.failure(IllegalStateException("HTTP 500 Internal Server Error")),
        )
        val repository = StripeRepositoryImpl(remote)

        val result = repository.getPublishableKey()

        assertTrue(result.isFailure)
        assertIs<DomainException.Server>(result.exceptionOrNull())
    }

    @Test
    fun getusercards_when_network_exception_maps_to_domain_network() = runTest {
        val remote = FakeStripeRemoteDataSource(
            userCardsResult = Result.failure(IOException("No connection")),
        )
        val repository = StripeRepositoryImpl(remote)

        val result = repository.getUserCards("cus_123")

        assertTrue(result.isFailure)
        assertIs<DomainException.Network>(result.exceptionOrNull())
    }

    private class FakeStripeRemoteDataSource(
        private val publishableKeyResult: Result<String> = Result.success("pk_default"),
        private val createOrGetCustomerResult: Result<CreateStripeCustomerResponse> =
            Result.success(CreateStripeCustomerResponse(success = true)),
        private val createEphemeralKeyResult: Result<StripeEphemeralKeyResponse> =
            Result.success(StripeEphemeralKeyResponse(success = true)),
        private val detachPaymentMethodResult: Result<Unit> = Result.success(Unit),
        private val setDefaultPaymentMethodResult: Result<Unit> = Result.success(Unit),
        private val createPaymentIntentResult: Result<StripePaymentIntentResponse> =
            Result.success(StripePaymentIntentResponse(success = true)),
        private val createSetupConfigResult: Result<StripeSetupConfigResponse> =
            Result.success(StripeSetupConfigResponse(success = true)),
        private val createRefundResult: Result<Unit> = Result.success(Unit),
        private val createSubscriptionResult: Result<SubscriptionDto> = Result.success(
            SubscriptionDto(subscriptionId = "sub_1", clientSecret = "secret", customerId = "cus_1"),
        ),
        private val cancelSubscriptionResult: Result<Unit> = Result.success(Unit),
        private val userTransactionsResult: Result<List<TransactionDto>> = Result.success(emptyList()),
        private val userCardsResult: Result<StripePaymentMethodsContainer> = Result.success(
            StripePaymentMethodsContainer(methods = emptyList(), defaultPaymentMethodId = null),
        ),
    ) : StripeRemoteDataSource {
        override suspend fun getPublishableKey(): Result<String> = publishableKeyResult

        override suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse> = createOrGetCustomerResult

        override suspend fun createEphemeralKey(customerId: String): Result<StripeEphemeralKeyResponse> = createEphemeralKeyResult

        override suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit> = detachPaymentMethodResult

        override suspend fun setDefaultPaymentMethod(paymentMethodId: String, customerId: String): Result<Unit> =
            setDefaultPaymentMethodResult

        override suspend fun createPaymentIntent(intentRequest: CreatePaymentIntentRequest): Result<StripePaymentIntentResponse> =
            createPaymentIntentResult

        override suspend fun createSetupConfig(userId: Int): Result<StripeSetupConfigResponse> = createSetupConfigResult

        override suspend fun createRefund(paymentIntentId: String, amount: Double?): Result<Unit> = createRefundResult

        override suspend fun createSubscription(
            priceId: String,
            userId: Int,
            productId: Int,
            couponCode: String?,
        ): Result<SubscriptionDto> = createSubscriptionResult

        override suspend fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int): Result<Unit> =
            cancelSubscriptionResult

        override suspend fun getUserTransactions(): Result<List<TransactionDto>> = userTransactionsResult

        override suspend fun getUserCards(customerId: String): Result<StripePaymentMethodsContainer> = userCardsResult
    }
}
