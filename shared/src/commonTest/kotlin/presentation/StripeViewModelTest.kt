package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StripeViewModelTest {

    private class FakeStripeRepository(
        private val publishableKeyResult: Result<String> = Result.success("pk_test_123"),
        private val customerResult: Result<CreateStripeCustomerResponse> = Result.success(sampleCustomerResponse()),
        private val ephemeralKeyResult: Result<StripeEphemeralKeyResponse> = Result.success(sampleEphemeralKeyResponse()),
        private val detachResult: Result<Unit> = Result.success(Unit),
        private val setDefaultResult: Result<Unit> = Result.success(Unit),
        private val paymentIntentResult: Result<StripePaymentIntentResponse> = Result.success(samplePiResponse()),
        private val setupConfigResult: Result<StripeSetupConfigResponse> = Result.success(sampleSetupConfigResponse()),
        private val refundResult: Result<Unit> = Result.success(Unit),
        private val createSubResult: Result<SubscriptionDto> = Result.success(sampleSubscription()),
        private val cancelSubResult: Result<Unit> = Result.success(Unit),
        private val userCardsResult: Result<StripePaymentMethodsContainer> = Result.success(sampleCards())
    ) : StripeRepository {
        override suspend fun getPublishableKey(): Result<String> = publishableKeyResult
        override suspend fun createOrGetCustomer(): Result<CreateStripeCustomerResponse> = customerResult
        override suspend fun createEphemeralKey(customerId: String): Result<StripeEphemeralKeyResponse> = ephemeralKeyResult
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

    @Test
    fun checkout_state_transitions_and_reset_methods_work() = runTest {
        val vm = StripeViewModel(StripeUseCase(FakeStripeRepository()))

        vm.startStripeCheckout(amount = 10.0, currency = "eur", customerId = "cus_1")
        assertTrue(vm.startStripeCheckout.value is StartStripeCheckoutState.Ready)

        vm.onSheetPresented()
        assertEquals(StartStripeCheckoutState.Processing, vm.startStripeCheckout.value)

        vm.onCheckoutCompleted()
        assertEquals(StartStripeCheckoutState.Completed, vm.startStripeCheckout.value)

        vm.onCheckoutCanceled()
        assertEquals(StartStripeCheckoutState.Canceled, vm.startStripeCheckout.value)

        vm.onCheckoutFailed("fallo")
        assertEquals(StartStripeCheckoutState.Failed("fallo"), vm.startStripeCheckout.value)

        vm.onStripeFailed("stripe fallo")
        assertEquals(StartStripeCheckoutState.Failed("stripe fallo"), vm.startStripeCheckout.value)

        vm.resetStartCheckoutState()
        assertEquals(StartStripeCheckoutState.Idle, vm.startStripeCheckout.value)
    }

    @Test
    fun addPaymentMethod_and_paymentMethods_flows_work() = runTest {
        val vm = StripeViewModel(StripeUseCase(FakeStripeRepository()))

        vm.prepareAddPaymentMethod(7)
        assertTrue(vm.addPaymentMethodUiState.value is AddPaymentMethodUiState.Ready)

        vm.onAddPaymentMethodCompleted()
        assertEquals(AddPaymentMethodUiState.Completed, vm.addPaymentMethodUiState.value)
        assertTrue(vm.viewPaymentMethodsUiState.value is PaymentMethodsUiState.Success)

        vm.onAddPaymentMethodCanceled()
        assertEquals(AddPaymentMethodUiState.Canceled, vm.addPaymentMethodUiState.value)

        vm.onAddPaymentMethodFailed("oops")
        assertEquals(AddPaymentMethodUiState.Failed("oops"), vm.addPaymentMethodUiState.value)

        vm.resetAddPaymentMethodState()
        assertEquals(AddPaymentMethodUiState.Idle, vm.addPaymentMethodUiState.value)

        vm.loadPaymentMethods()
        assertTrue(vm.viewPaymentMethodsUiState.value is PaymentMethodsUiState.Success)
    }

    @Test
    fun subscription_refund_actions_and_customer_helpers_work() = runTest {
        val vm = StripeViewModel(StripeUseCase(FakeStripeRepository()))

        vm.startStripeSubscription("price_1", "cus_1", 10, 20)
        assertTrue(vm.startStripeCheckout.value is StartStripeCheckoutState.Ready)

        vm.cancelSubscription("sub_1", 20, 10)
        assertEquals(ActionUiState.Success, vm.actionUiState.value)

        val customerId = vm.createOrGetCustomer()
        assertEquals("cus_123", customerId)

        vm.createRefund("pi_1", 20, amount = 10.0)
        assertEquals(RefundUiState.Success(20), vm.refundUiState.value)

        vm.createRefund("pi_1", 20, amount = 0.0)
        assertEquals(RefundUiState.Error("El importe del reembolso debe ser mayor que cero"), vm.refundUiState.value)

        vm.resetRefundState()
        assertEquals(RefundUiState.Idle, vm.refundUiState.value)

        vm.detachPaymentMethod("pm_1")
        assertEquals(ActionUiState.Success, vm.actionUiState.value)

        vm.setDefaultPaymentMethod("pm_1")
        assertEquals(ActionUiState.Success, vm.actionUiState.value)

        vm.resetActionState()
        assertEquals(ActionUiState.Idle, vm.actionUiState.value)
    }

    @Test
    fun failure_paths_for_key_flows_emit_error_states() = runTest {
        val vm = StripeViewModel(
            StripeUseCase(
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
        )

        vm.startStripeCheckout(amount = 10.0, currency = "eur", customerId = "cus_1")
        assertEquals(StartStripeCheckoutState.Failed("pi fail"), vm.startStripeCheckout.value)

        vm.prepareAddPaymentMethod(7)
        assertEquals(AddPaymentMethodUiState.Failed("setup fail"), vm.addPaymentMethodUiState.value)

        vm.startStripeSubscription("price_1", "cus_1", 10, 20)
        assertEquals(StartStripeCheckoutState.Failed("sub fail"), vm.startStripeCheckout.value)

        vm.cancelSubscription("sub_1", 20, 10)
        assertEquals(ActionUiState.Error("cancel fail"), vm.actionUiState.value)

        assertEquals(null, vm.createOrGetCustomer())

        vm.createRefund("pi_1", 20, 10.0)
        assertEquals(RefundUiState.Error("refund fail"), vm.refundUiState.value)

        vm.detachPaymentMethod("pm_1")
        assertEquals(ActionUiState.Error("detach fail"), vm.actionUiState.value)

        vm.setDefaultPaymentMethod("pm_1")
        assertEquals(ActionUiState.Error("No se pudo obtener el cliente de Stripe"), vm.actionUiState.value)
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
