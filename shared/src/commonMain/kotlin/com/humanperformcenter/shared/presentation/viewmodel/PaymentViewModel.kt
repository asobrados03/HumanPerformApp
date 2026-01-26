package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.model.payment.PaymentRequest
import com.humanperformcenter.shared.data.model.payment.RebillRequest
import com.humanperformcenter.shared.data.model.product_service.ServiceItem
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.usecase.GooglePayUseCase
import com.humanperformcenter.shared.domain.usecase.PaymentUseCase
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import com.humanperformcenter.shared.presentation.ui.PaymentMethodsUiState
import com.humanperformcenter.shared.presentation.ui.PaymentState
import com.humanperformcenter.shared.presentation.ui.StripeUiState
import com.humanperformcenter.shared.presentation.ui.models.BillingPrefill
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaymentViewModel(
    private val paymentUseCase: PaymentUseCase,
    private val googlePayUseCase: GooglePayUseCase,
    private val stripeUseCase: StripeUseCase
): ViewModel() {

    private val _paymentUrl = MutableStateFlow<String?>(null)
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    private val _paymentMethod = MutableStateFlow<String?>(null)
    val paymentMethod: StateFlow<String?> = _paymentMethod.asStateFlow()

    private val _viewPaymentMethodsUiState = MutableStateFlow<PaymentMethodsUiState>(
        PaymentMethodsUiState.Empty
    )

    // StateFlow público de solo lectura para observar desde Compose
    val viewPaymentMethodsUiState: StateFlow<PaymentMethodsUiState> = _viewPaymentMethodsUiState.asStateFlow()


    fun generatePaymentURL(request: PaymentRequest) {
        println("🔧 Generando URL de pago...")
        viewModelScope.launch(Dispatchers.IO) {
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
    fun payWithGooglePay(requestJson: String, amount: Int, currency: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _paymentState.value = PaymentState.Loading
            println("🟡 [GPay] VM.payWithGooglePay(amount=$amount, currency=$currency)")
            googlePayUseCase(requestJson, amount, currency).fold(
                onSuccess = { token -> _paymentState.value = PaymentState.Success(token)
                    println("🟢 [GPay] VM.onSuccess: token.len=${token.length}")
                },
                onFailure = { e -> _paymentState.value = PaymentState.Error(e.message ?: "Error" )
                    println("🔴 [GPay] VM.onFailure: ${e.message}")
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

    fun onPaymentSheetResult(result: Any) {
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                println("$amountInCents")
                println(currency)
                println("$userId")
                println("$productId")
                println("$couponCode")
                println("$billing")

                val cfg = stripeUseCase.getConfig()

                val createPaymentIntentRequest = CreatePaymentIntentRequest(
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
                val pi = stripeUseCase.createPaymentIntent(createPaymentIntentRequest).getOrThrow()

                // 3) (Opcional) Customer para métodos guardados
                var customerConfig: Any? = null
                /*if (!pi.customerId.isNullOrBlank()) {
                    // Usa la versión del SDK actual
                    val apiVersion = ""
                    val ek = stripeUseCase.createEphemeralKey(pi.customerId!!, apiVersion).getOrThrow()
                    customerConfig = Any.CustomerConfiguration(
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
                )*/

                _stripeUi.value = StripeUiState.Ready(
                    clientSecret = pi.clientSecret,
                    config = null
                )
            } catch (t: Throwable) {
                _stripeUi.value = StripeUiState.Error(t.message ?: "Error iniciando pago")
            }
        }
    }

    fun rebillWithSavedCard(rebillRequest: RebillRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun retry(userId: Int) {
        getPaymentMethods(userId)
    }

    fun clearViewPaymentMethodsState() {
        _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Empty
    }

    fun createHppPaymentRequest(
        product: ServiceItem,
        user: User?,
        showStored:
        Boolean,
        saveCard: Boolean): PaymentRequest {

        return paymentUseCase.createHppPaymentRequest(product, user, showStored, saveCard)
    }

    val allowedPaymentMethods = googlePayUseCase.obtenerConfiguracionGPay()

    fun ejecutarPagoGPay(precio: Double) {
        val requestJson = googlePayUseCase.prepararJsonPago(precio)

        // SOLUCIÓN AL ERROR DE IMAGEN:
        // Convertimos a Int aquí antes de llamar a la función de la pasarela
        val centimos = (precio * 100).toInt()

        //launchGooglePay(requestJson, centimos)
    }
}