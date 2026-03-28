package com.humanperformcenter.shared.presentation

import com.humanperformcenter.shared.data.model.payment.AssociatedObject
import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.model.payment.CreatePiDto
import com.humanperformcenter.shared.data.model.payment.CreateStripeCustomerResponse
import com.humanperformcenter.shared.data.model.payment.CustomerData
import com.humanperformcenter.shared.data.model.payment.EphemeralKeyDto
import com.humanperformcenter.shared.data.model.payment.StripeCardDetails
import com.humanperformcenter.shared.data.model.payment.StripeEphemeralKeyResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentIntentResponse
import com.humanperformcenter.shared.data.model.payment.StripePaymentMethod
import com.humanperformcenter.shared.data.model.payment.StripePaymentMethodsContainer
import com.humanperformcenter.shared.data.model.payment.StripeSetupConfigData
import com.humanperformcenter.shared.data.model.payment.StripeSetupConfigResponse
import com.humanperformcenter.shared.data.model.payment.SubscriptionDto
import com.humanperformcenter.shared.domain.repository.StripeRepository
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import com.humanperformcenter.shared.presentation.ui.ActionUiState
import com.humanperformcenter.shared.presentation.ui.AddPaymentMethodUiState
import com.humanperformcenter.shared.presentation.ui.PaymentMethodsUiState
import com.humanperformcenter.shared.presentation.ui.RefundUiState
import com.humanperformcenter.shared.presentation.ui.StartStripeCheckoutState
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StripeViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeStripeRepository(
        private val customerResult: Result<CreateStripeCustomerResponse> = Result.success(sampleCustomerResponse()),
        private val detachResult: Result<Unit> = Result.success(Unit),
        private val setDefaultResult: Result<Unit> = Result.success(Unit),
        private val paymentIntentResult: Result<StripePaymentIntentResponse> = Result.success(samplePiResponse()),
        private val setupConfigResult: Result<StripeSetupConfigResponse> = Result.success(sampleSetupConfigResponse()),
        private val refundResult: Result<Unit> = Result.success(Unit),
        private val createSubResult: Result<SubscriptionDto> = Result.success(sampleSubscription()),
        private val cancelSubResult: Result<Unit> = Result.success(Unit),
        private val userCardsResult: Result<StripePaymentMethodsContainer> = Result.success(sampleCards())
    ) : StripeRepository {
        override suspend fun getPublishableKey(): Result<String> = Result.success("pk_test_123")
        override suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse> = customerResult
        override suspend fun createEphemeralKey(customerId: String): Result<StripeEphemeralKeyResponse> = Result.success(sampleEphemeralKeyResponse())
        override suspend fun detachPaymentMethod(paymentMethodId: String): Result<Unit> = detachResult
        override suspend fun setDefaultPaymentMethod(paymentMethodId: String, customerId: String): Result<Unit> = setDefaultResult
        override suspend fun createPaymentIntent(intentRequest: CreatePaymentIntentRequest): Result<StripePaymentIntentResponse> = paymentIntentResult
        override suspend fun createSetupConfig(userId: Int): Result<StripeSetupConfigResponse> = setupConfigResult
        override suspend fun createRefund(paymentIntentId: String, amount: Double?): Result<Unit> = refundResult
        override suspend fun createSubscription(priceId: String, userId: Int, productId: Int, couponCode: String?): Result<SubscriptionDto> = createSubResult
        override suspend fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int): Result<Unit> = cancelSubResult
        override suspend fun getUserTransactions() = Result.success(emptyList<com.humanperformcenter.shared.data.model.payment.TransactionDto>())
        override suspend fun getUserCards(customerId: String): Result<StripePaymentMethodsContainer> = userCardsResult
    }

    private fun buildViewModel(repository: FakeStripeRepository = FakeStripeRepository()) =
        StripeViewModel(StripeUseCase(repository))

    @Test
    fun checkout_state_transitions_ready_processing_completed_canceled_failed_and_reset_to_idle() = runTest(mainDispatcher.scheduler) {
        val viewModel = buildViewModel()

        viewModel.startStripeCheckout(amount = 10.0, currency = "eur", customerId = "cus_1")
        advanceUntilIdle()
        assertTrue(viewModel.startStripeCheckout.value is StartStripeCheckoutState.Ready)

        viewModel.onSheetPresented()
        assertEquals(StartStripeCheckoutState.Processing, viewModel.startStripeCheckout.value)

        viewModel.onCheckoutCompleted()
        assertEquals(StartStripeCheckoutState.Completed, viewModel.startStripeCheckout.value)

        viewModel.onCheckoutCanceled()
        assertEquals(StartStripeCheckoutState.Canceled, viewModel.startStripeCheckout.value)

        viewModel.onCheckoutFailed("fallo")
        assertEquals(StartStripeCheckoutState.Failed("fallo"), viewModel.startStripeCheckout.value)

        viewModel.onStripeFailed("stripe fallo")
        assertEquals(StartStripeCheckoutState.Failed("stripe fallo"), viewModel.startStripeCheckout.value)

        viewModel.resetStartCheckoutState()
        assertEquals(StartStripeCheckoutState.Idle, viewModel.startStripeCheckout.value)
    }

    @Test
    fun add_payment_method_flow_emits_ready_completed_canceled_failed_and_reset() = runTest(mainDispatcher.scheduler) {
        val viewModel = buildViewModel()

        viewModel.prepareAddPaymentMethod(7)
        advanceUntilIdle()
        assertTrue(viewModel.addPaymentMethodUiState.value is AddPaymentMethodUiState.Ready)

        viewModel.onAddPaymentMethodCompleted()
        assertEquals(AddPaymentMethodUiState.Completed, viewModel.addPaymentMethodUiState.value)
        assertTrue(viewModel.viewPaymentMethodsUiState.value is PaymentMethodsUiState.Success)

        viewModel.onAddPaymentMethodCanceled()
        assertEquals(AddPaymentMethodUiState.Canceled, viewModel.addPaymentMethodUiState.value)

        viewModel.onAddPaymentMethodFailed("oops")
        assertEquals(AddPaymentMethodUiState.Failed("oops"), viewModel.addPaymentMethodUiState.value)

        viewModel.resetAddPaymentMethodState()
        assertEquals(AddPaymentMethodUiState.Idle, viewModel.addPaymentMethodUiState.value)
    }

    @Test
    fun subscription_refund_and_action_helpers_when_success_update_states() = runTest(mainDispatcher.scheduler) {
        val viewModel = buildViewModel()

        viewModel.startStripeSubscription("price_1", "cus_1", 10, 20)
        advanceUntilIdle()
        assertTrue(viewModel.startStripeCheckout.value is StartStripeCheckoutState.Ready)

        viewModel.cancelSubscription("sub_1", 20, 10)
        advanceUntilIdle()
        assertEquals(ActionUiState.Success, viewModel.actionUiState.value)

        assertEquals("cus_123", viewModel.createOrGetCustomer())

        viewModel.createRefund("pi_1", 20, amount = 10.0)
        advanceUntilIdle()
        assertEquals(RefundUiState.Success(20), viewModel.refundUiState.value)

        viewModel.createRefund("pi_1", 20, amount = 0.0)
        advanceUntilIdle()
        assertEquals(RefundUiState.Error("El importe del reembolso debe ser mayor que cero"), viewModel.refundUiState.value)

        viewModel.resetRefundState()
        assertEquals(RefundUiState.Idle, viewModel.refundUiState.value)

        viewModel.detachPaymentMethod("pm_1")
        advanceUntilIdle()
        assertEquals(ActionUiState.Success, viewModel.actionUiState.value)

        viewModel.setDefaultPaymentMethod("pm_1")
        advanceUntilIdle()
        assertEquals(ActionUiState.Success, viewModel.actionUiState.value)

        viewModel.resetActionState()
        assertEquals(ActionUiState.Idle, viewModel.actionUiState.value)
    }

    @Test
    fun failure_paths_emit_expected_failed_states() = runTest(mainDispatcher.scheduler) {
        val viewModel = buildViewModel(
            FakeStripeRepository(
                paymentIntentResult = Result.failure(IllegalStateException("pi fail")),
                setupConfigResult = Result.failure(IllegalStateException("setup fail")),
                createSubResult = Result.failure(IllegalStateException("sub fail")),
                cancelSubResult = Result.failure(IllegalStateException("cancel fail")),
                customerResult = Result.failure(IllegalStateException("customer fail")),
                refundResult = Result.failure(IllegalStateException("refund fail")),
                detachResult = Result.failure(IllegalStateException("detach fail")),
                setDefaultResult = Result.failure(IllegalStateException("default fail"))
            )
        )

        viewModel.startStripeCheckout(amount = 10.0, currency = "eur", customerId = "cus_1")
        advanceUntilIdle()
        assertEquals(StartStripeCheckoutState.Failed("pi fail"), viewModel.startStripeCheckout.value)

        viewModel.prepareAddPaymentMethod(7)
        advanceUntilIdle()
        assertEquals(AddPaymentMethodUiState.Failed("setup fail"), viewModel.addPaymentMethodUiState.value)

        viewModel.startStripeSubscription("price_1", "cus_1", 10, 20)
        advanceUntilIdle()
        assertEquals(StartStripeCheckoutState.Failed("sub fail"), viewModel.startStripeCheckout.value)

        viewModel.cancelSubscription("sub_1", 20, 10)
        advanceUntilIdle()
        assertEquals(ActionUiState.Error("cancel fail"), viewModel.actionUiState.value)

        assertEquals(null, viewModel.createOrGetCustomer())

        viewModel.createRefund("pi_1", 20, 10.0)
        advanceUntilIdle()
        assertEquals(RefundUiState.Error("refund fail"), viewModel.refundUiState.value)

        viewModel.detachPaymentMethod("pm_1")
        advanceUntilIdle()
        assertEquals(ActionUiState.Error("detach fail"), viewModel.actionUiState.value)

        viewModel.setDefaultPaymentMethod("pm_1")
        advanceUntilIdle()
        assertEquals(ActionUiState.Error("No se pudo obtener el cliente de Stripe"), viewModel.actionUiState.value)
    }

    private companion object {
        fun samplePiResponse() = StripePaymentIntentResponse(
            success = true,
            data = CreatePiDto(
                id = "pi_1",
                amount = 1000,
                currency = "eur",
                clientSecret = "cs_test",
                status = "requires_payment_method",
                created = 1L
            )
        )

        fun sampleEphemeralKeyResponse() = StripeEphemeralKeyResponse(
            success = true,
            data = EphemeralKeyDto(
                id = "eph_1",
                objectType = "ephemeral_key",
                associatedObjects = listOf(AssociatedObject("cus_123", "customer")),
                created = 1L,
                expires = 2L,
                livemode = false,
                secret = "eph_secret"
            )
        )

        fun sampleCustomerResponse() = CreateStripeCustomerResponse(
            success = true,
            data = CustomerData(customerId = "cus_123", isNew = false)
        )

        fun sampleSetupConfigResponse() = StripeSetupConfigResponse(
            success = true,
            data = StripeSetupConfigData(
                customerId = "cus_123",
                clientSecret = "seti_secret",
                ephemeralKey = "eph_secret"
            )
        )

        fun sampleSubscription() = SubscriptionDto(
            subscriptionId = "sub_1",
            clientSecret = "sub_secret",
            customerId = "cus_123"
        )

        fun sampleCards() = StripePaymentMethodsContainer(
            methods = listOf(
                StripePaymentMethod(
                    id = "pm_1",
                    customer = "cus_123",
                    card = StripeCardDetails("visa", "4242", 12, 2030),
                    type = "card"
                )
            ),
            defaultPaymentMethodId = "pm_1"
        )
    }
}
