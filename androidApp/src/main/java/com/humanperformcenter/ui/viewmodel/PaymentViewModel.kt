package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.data.model.RebillRequest
import com.humanperformcenter.shared.data.model.Stripe.CreatePaymentIntentRequest
import com.humanperformcenter.shared.domain.usecase.GooglePayUseCase
import com.humanperformcenter.shared.domain.usecase.PaymentUseCase
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import com.humanperformcenter.ui.viewmodel.state.PaymentState
import com.stripe.android.Stripe.Companion.API_VERSION
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class PaymentViewModel(
    private val paymentUseCase: PaymentUseCase,
    private val googlePayUseCase: GooglePayUseCase,
    private val stripeUseCase: StripeUseCase // NEW
): ViewModel() {

    private val _paymentUrl = MutableStateFlow<String?>(null)
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    private val _paymentMethod = MutableStateFlow<String?>(null)
    val paymentMethod: StateFlow<String?> = _paymentMethod.asStateFlow()

    fun generatePaymentURL(request: PaymentRequest) {
        println("🔧 Generando URL de pago...")
        viewModelScope.launch {
            try {
                val url = paymentUseCase.generatePaymentUrl(request)
                _paymentUrl.value = url
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al generar el pago"
            }
        }
    }

    /**
     * Lanza el flujo de Google Pay a partir del JSON de PaymentDataRequest,
     * y luego envía el token al backend dentro del use case.
     */
    fun payWithGooglePay(requestJson: String) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            googlePayUseCase(requestJson).fold(
                onSuccess = { token ->
                    _paymentState.value = PaymentState.Success(token)
                },
                onFailure = { e ->
                    _paymentState.value = PaymentState.Error(e.localizedMessage ?: "Error")
                }
            )
        }
    }


    fun clearState() {
        _paymentUrl.value = null
        _error.value = null
    }

    private val _stripeUi = MutableStateFlow<StripeUiState>(StripeUiState.Idle)
    val stripeUi: StateFlow<StripeUiState> = _stripeUi

    fun onPaymentSheetResult(result: PaymentSheetResult) {
        _stripeUi.value = StripeUiState.Result(result)
    }
    fun resetStripeReady() {
        if (_stripeUi.value is StripeUiState.Ready) _stripeUi.value = StripeUiState.Idle
    }

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
                println("$amountInCents")
                println("$currency")
                println("$userId")
                println("$productId")
                println("$couponCode")
                println("$billing")

                val cfg = stripeUseCase.getConfig()

                val CreatePaymentIntentRequest = CreatePaymentIntentRequest(
                    amount = amountInCents,
                    currency = currency,
                    user_id = userId,
                    product_id = productId,
                    metadata = mapOf(
                        "coupon_code" to (couponCode ?: ""),
                        "billingname" to (billing?.name ?: ""),
                        "billingemail" to (billing?.email ?: ""),
                        "billingaddress" to (billing?.addressLine1 ?: ""),
                        "billingpostalcode" to (billing?.postalCode ?: ""),
                        "billingcity" to (billing?.city ?: "")
                    )
                )
                // 2) Crear PaymentIntent
                val pi = stripeUseCase.createPaymentIntent(CreatePaymentIntentRequest)

                // 3) (Opcional) Customer para métodos guardados
                var customerConfig: PaymentSheet.CustomerConfiguration? = null
                if (!pi.customerId.isNullOrBlank()) {
                    // Usa la versión del SDK actual
                    val apiVersion = API_VERSION
                    val ek = stripeUseCase.createEphemeralKey(pi.customerId!!, apiVersion)
                    customerConfig = PaymentSheet.CustomerConfiguration(
                        id = pi.customerId!!,
                        ephemeralKeySecret = ek.secret
                    )
                }

                // 4) Config de PaymentSheet
                val paymentConfig = PaymentSheet.Configuration(
                    merchantDisplayName = "HumanPerformCenter",
                    customer = customerConfig,
                    allowsDelayedPaymentMethods = true,
                    defaultBillingDetails = PaymentSheet.BillingDetails(
                        name = billing?.name,
                        email = billing?.email,
                        address = PaymentSheet.Address(
                            line1 = billing?.addressLine1,
                            postalCode = billing?.postalCode,
                            city = billing?.city
                        )
                    )
                )

                _stripeUi.value = StripeUiState.Ready(
                    clientSecret = pi.clientSecret,
                    config = paymentConfig
                )
            } catch (t: Throwable) {
                _stripeUi.value = StripeUiState.Error(t.message ?: "Error iniciando pago")
            }
        }
    }

    fun getPaymentMethod(user_id: Int){
        viewModelScope.launch {
            try {
                val method = paymentUseCase.getPaymentMethod(user_id)
                _paymentMethod.value = method
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al obtener el método de pago"
            }
        }
    }
    fun rebillWithSavedCard(rebillRequest: RebillRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = paymentUseCase.rebillPayment(rebillRequest)
                if (success) {
                    onSuccess()
                } else {
                    onError("Error al procesar el pago")
                }
            } catch (e: Exception) {
                onError("Excepción en rebill: ${e.message}")
            }
        }
    }

}

data class BillingPrefill(
    val name: String? = null,
    val email: String? = null,
    val addressLine1: String? = null,
    val postalCode: String? = null,
    val city: String? = null
)
sealed class StripeUiState {
    object Idle : StripeUiState()
    data class Ready(val clientSecret: String, val config: PaymentSheet.Configuration) : StripeUiState()
    data class Result(val result: PaymentSheetResult) : StripeUiState()
    data class Error(val message: String) : StripeUiState()
}
