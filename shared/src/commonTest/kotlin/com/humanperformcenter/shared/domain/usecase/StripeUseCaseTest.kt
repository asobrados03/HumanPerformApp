package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.model.payment.CreateStripeCustomerResponse
import com.humanperformcenter.shared.data.model.payment.CustomerData
import com.humanperformcenter.shared.data.model.payment.StripeEphemeralKeyResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentIntentResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentMethodsContainer
import com.humanperformcenter.shared.data.model.payment.StripeSetupConfigResponse
import com.humanperformcenter.shared.data.model.payment.SubscriptionDto
import com.humanperformcenter.shared.data.model.payment.TransactionDto
import com.humanperformcenter.shared.domain.repository.StripeRepository
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatform.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StripeUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun createOrGetCustomer_whenResponseIsValid_returnsCustomer() = runTest {
        val expected = CreateStripeCustomerResponse(true, "ok", CustomerData("cus_123", true))
        val useCase = buildUseCase(FakeRepo(customerResult = Result.success(expected)))
        assertEquals(expected, useCase.createOrGetCustomer().getOrNull())
    }

    @Test
    fun createEphemeralKey_whenCustomerIdIsEmpty_returnsFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(ephemeralKeyResult = Result.failure(IllegalArgumentException("customerId required"))))
        assertTrue(useCase.createEphemeralKey("").isFailure)
    }

    @Test
    fun detachPaymentMethod_whenMethodIsValid_returnsSuccess() = runTest {
        val useCase = buildUseCase(FakeRepo(detachResult = Result.success(Unit)))
        assertTrue(useCase.detachPaymentMethod("pm_1").isSuccess)
    }

    @Test
    fun setDefaultPaymentMethod_whenDataIsValid_returnsSuccess() = runTest {
        val useCase = buildUseCase(FakeRepo(setDefaultResult = Result.success(Unit)))
        assertTrue(useCase.setDefaultPaymentMethod("pm_1", "cus_1").isSuccess)
    }

    @Test
    fun createPaymentIntent_whenRequestIsValid_returnsIntentResponse() = runTest {
        val useCase = buildUseCase(FakeRepo(paymentIntentResult = Result.success(StripePaymentIntentResponse(true))))
        val req = CreatePaymentIntentRequest(20.0, "eur", "cus_1")
        assertTrue(useCase.createPaymentIntent(req).getOrNull()?.success == true)
    }

    @Test
    fun createSetupConfig_whenUserIsValid_returnsConfig() = runTest {
        val useCase = buildUseCase(FakeRepo(setupConfigResult = Result.success(StripeSetupConfigResponse(true))))
        assertTrue(useCase.createSetupConfig(1).getOrNull()?.success == true)
    }

    @Test
    fun createRefund_whenAmountIsNull_returnsSuccess() = runTest {
        val useCase = buildUseCase(FakeRepo(refundResult = Result.success(Unit)))
        assertTrue(useCase.createRefund("pi_123", null).isSuccess)
    }

    @Test
    fun createSubscription_whenDataIsValid_returnsSubscription() = runTest {
        val useCase = buildUseCase(FakeRepo(subscriptionResult = Result.success(SubscriptionDto("sub_1"))))
        assertEquals("sub_1", useCase.createSubscription("price_1", 1, 2, null).getOrNull()?.subscriptionId)
    }

    @Test
    fun cancelSubscription_whenDataIsValid_returnsSuccess() = runTest {
        val useCase = buildUseCase(FakeRepo(cancelSubscriptionResult = Result.success(Unit)))
        assertTrue(useCase.cancelSubscription("sub_1", 2, 1).isSuccess)
    }

    @Test
    fun getUserCards_whenCustomerIsValid_returnsCards() = runTest {
        val container = StripePaymentMethodsContainer(emptyList())
        val useCase = buildUseCase(FakeRepo(cardsResult = Result.success(container)))
        assertEquals(container, useCase.getUserCards("cus_1").getOrNull())
    }

    @Test
    fun getPublishableKey_whenRepositoryFails_propagatesFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(publishableKeyResult = Result.failure(RuntimeException("stripe down"))))
        assertTrue(useCase.getPublishableKey().isFailure)
    }

    private fun buildUseCase(repo: StripeRepository): StripeUseCase {
        startKoin { modules(module { single<StripeRepository> { repo }; single { StripeUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(
        private val publishableKeyResult: Result<String> = Result.success("pk_test"),
        private val customerResult: Result<CreateStripeCustomerResponse> = Result.success(CreateStripeCustomerResponse(true)),
        private val ephemeralKeyResult: Result<StripeEphemeralKeyResponse> = Result.success(StripeEphemeralKeyResponse(true)),
        private val detachResult: Result<Unit> = Result.success(Unit),
        private val setDefaultResult: Result<Unit> = Result.success(Unit),
        private val paymentIntentResult: Result<StripePaymentIntentResponse> = Result.success(StripePaymentIntentResponse(true)),
        private val setupConfigResult: Result<StripeSetupConfigResponse> = Result.success(StripeSetupConfigResponse(true)),
        private val refundResult: Result<Unit> = Result.success(Unit),
        private val subscriptionResult: Result<SubscriptionDto> = Result.success(SubscriptionDto("sub_1")),
        private val cancelSubscriptionResult: Result<Unit> = Result.success(Unit),
        private val cardsResult: Result<StripePaymentMethodsContainer> = Result.success(StripePaymentMethodsContainer(emptyList())),
    ) : StripeRepository {
        override suspend fun getPublishableKey() = publishableKeyResult
        override suspend fun createOrGetCustomer() = customerResult
        override suspend fun createEphemeralKey(customerId: String) = ephemeralKeyResult
        override suspend fun detachPaymentMethod(paymentMethodId: String) = detachResult
        override suspend fun setDefaultPaymentMethod(paymentMethodId: String, customerId: String) = setDefaultResult
        override suspend fun createPaymentIntent(intentRequest: CreatePaymentIntentRequest) = paymentIntentResult
        override suspend fun createSetupConfig(userId: Int) = setupConfigResult
        override suspend fun createRefund(paymentIntentId: String, amount: Double?) = refundResult
        override suspend fun createSubscription(priceId: String, userId: Int, productId: Int, couponCode: String?) = subscriptionResult
        override suspend fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int) = cancelSubscriptionResult
        override suspend fun getUserTransactions(): Result<List<TransactionDto>> = Result.success(emptyList())
        override suspend fun getUserCards(customerId: String) = cardsResult
    }
}
