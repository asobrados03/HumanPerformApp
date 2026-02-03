package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.domain.usecase.PaymentUseCase
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import com.humanperformcenter.shared.presentation.ui.PaymentMethodsUiState
import com.humanperformcenter.shared.presentation.ui.StripeCheckoutConfig
import com.humanperformcenter.shared.presentation.ui.StripeUiState
import com.humanperformcenter.shared.presentation.ui.models.BillingPrefill
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaymentViewModel(
    private val paymentUseCase: PaymentUseCase,
    private val stripeUseCase: StripeUseCase
): ViewModel() {

    private val _paymentUrl = MutableStateFlow<String?>(null)
    @NativeCoroutinesState
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _paymentMethod = MutableStateFlow<String?>(null)
    @NativeCoroutinesState
    val paymentMethod: StateFlow<String?> = _paymentMethod.asStateFlow()

    private val _viewPaymentMethodsUiState = MutableStateFlow<PaymentMethodsUiState>(
        PaymentMethodsUiState.Empty
    )

    @NativeCoroutinesState
    val viewPaymentMethodsUiState: StateFlow<PaymentMethodsUiState> = _viewPaymentMethodsUiState.asStateFlow()


    fun clearState() {
        _paymentUrl.value = null
        _error.value = null
    }

    private val _stripeUi = MutableStateFlow<StripeUiState>(StripeUiState.Idle)
    val stripeUi = _stripeUi.asStateFlow()

    fun startStripeCheckout(
        amountInCents: Int,
        currency: String,
        userId: Int? = null,
        productId: Int? = null,
        couponCode: String? = null,
        billing: BillingPrefill? = null
    ) {
        viewModelScope.launch {
            try {
                _stripeUi.value = StripeUiState.Loading

                val request = CreatePaymentIntentRequest(
                    amount = amountInCents,
                    currency = currency,
                    user_id = userId,
                    product_id = productId,
                    metadata = mapOf(
                        "coupon_code" to (couponCode ?: ""),
                        "billing_name" to (billing?.name ?: ""),
                        "billing_email" to (billing?.email ?: ""),
                        "billing_address" to (billing?.addressLine1 ?: ""),
                        "billing_postal_code" to (billing?.postalCode ?: ""),
                        "billing_city" to (billing?.city ?: "")
                    )
                )

                // ✅ Asegúrate de manejar el Result correctamente
                val result = stripeUseCase.createPaymentIntent(request)

                result.fold(
                    onSuccess = { paymentIntent ->
                        val checkoutConfig = StripeCheckoutConfig(
                            merchantDisplayName = "HumanPerformCenter",
                            allowsDelayedPaymentMethods = true,
                            googlePayEnabled = true,
                            googlePayCountryCode = "ES",
                            googlePayCurrencyCode = currency,
                            billingName = billing?.name,
                            billingEmail = billing?.email,
                            billingAddressLine1 = billing?.addressLine1,
                            billingPostalCode = billing?.postalCode,
                            billingCity = billing?.city
                        )

                        _stripeUi.value = StripeUiState.Ready(
                            clientSecret = paymentIntent.clientSecret,
                            config = checkoutConfig
                        )
                    },
                    onFailure = { exception ->
                        _stripeUi.value = StripeUiState.Failed(
                            exception.message ?: "Error iniciando el pago"
                        )
                    }
                )

            } catch (t: Throwable) {
                _stripeUi.value = StripeUiState.Failed(
                    t.message ?: "Error iniciando el pago"
                )
            }
        }
    }

    fun onStripeCompleted() {
        _stripeUi.value = StripeUiState.Completed
    }

    fun onStripeCanceled() {
        _stripeUi.value = StripeUiState.Canceled
    }

    fun onStripeFailed(message: String) {
        _stripeUi.value = StripeUiState.Failed(message)
    }

    fun reset() {
        _stripeUi.value = StripeUiState.Idle
    }

    fun getPaymentMethods(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Loading

            try {
                val methods = paymentUseCase.getPaymentMethods(userId)
                _viewPaymentMethodsUiState.value = if (methods.isEmpty()) {
                    PaymentMethodsUiState.Empty
                } else {
                    PaymentMethodsUiState.Success(methods)
                }
            } catch (e: Exception) {
                _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Error(
                    e.message ?: "Error al obtener métodos de pago"
                )
            }
        }
    }
}
