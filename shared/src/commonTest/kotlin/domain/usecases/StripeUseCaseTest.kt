package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.domain.repository.StripeRepository
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StripeUseCaseTest {
    private class FakeStripeRepository(
        private val publishableKeyResult: Result<String>
    ) : StripeRepository {
        override suspend fun getPublishableKey(): Result<String> = publishableKeyResult
        override suspend fun createOrGetCustomer() = error("Not used")
        override suspend fun createEphemeralKey(customerId: String) = error("Not used")
        override suspend fun detachPaymentMethod(paymentMethodId: String) = error("Not used")
        override suspend fun setDefaultPaymentMethod(paymentMethodId: String, customerId: String) = error("Not used")
        override suspend fun createPaymentIntent(intentRequest: com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest) = error("Not used")
        override suspend fun createSetupConfig(userId: Int) = error("Not used")
        override suspend fun createRefund(paymentIntentId: String, amount: Double?) = error("Not used")
        override suspend fun createSubscription(priceId: String, userId: Int, productId: Int, couponCode: String?) = error("Not used")
        override suspend fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int) = error("Not used")
        override suspend fun getUserTransactions() = error("Not used")
        override suspend fun getUserCards(customerId: String) = error("Not used")
    }

    @Test
    fun stripeUseCase_whenRepositoryReturnsKey_returnsSuccess() = runBlocking {
        // Arrange
        val useCase = StripeUseCase(FakeStripeRepository(Result.success("pk_test_123")))

        // Act
        val result = useCase.getPublishableKey()

        // Assert
        assertEquals("pk_test_123", result.getOrNull())
    }

    @Test
    fun stripeUseCase_whenRepositoryReturnsBlankKey_returnsBlank() = runBlocking {
        // Arrange
        val useCase = StripeUseCase(FakeStripeRepository(Result.success("")))

        // Act
        val result = useCase.getPublishableKey()

        // Assert
        assertEquals("", result.getOrNull())
    }

    @Test
    fun stripeUseCase_whenRepositoryFails_propagatesFailure() = runBlocking {
        // Arrange
        val useCase = StripeUseCase(FakeStripeRepository(Result.failure(RuntimeException("network"))))

        // Act
        val result = useCase.getPublishableKey()

        // Assert
        assertTrue(result.isFailure)
    }
}
