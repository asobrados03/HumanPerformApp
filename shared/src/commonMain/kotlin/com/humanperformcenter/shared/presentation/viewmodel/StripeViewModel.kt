package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.data.model.payment.SharedPaymentResult
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import com.humanperformcenter.shared.presentation.ui.ActionUiState
import com.humanperformcenter.shared.presentation.ui.PaymentMethodsUiState
import com.humanperformcenter.shared.presentation.ui.StartStripeCheckoutState
import com.humanperformcenter.shared.presentation.ui.StripeCheckoutConfig
import com.humanperformcenter.shared.presentation.ui.SubscriptionUiState
import com.humanperformcenter.shared.presentation.ui.models.BillingPrefill
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StripeViewModel(
    private val stripeUseCase: StripeUseCase
) : ViewModel() {
    companion object {
        val log = logging()
    }

    // --- STATES ---

    // 1. Estado de la lista de tarjetas
    private val _viewPaymentMethodsUiState = MutableStateFlow<PaymentMethodsUiState>(PaymentMethodsUiState.Empty)
    @NativeCoroutinesState
    val viewPaymentMethodsUiState: StateFlow<PaymentMethodsUiState> = _viewPaymentMethodsUiState.asStateFlow()

    // 2. Estado del Checkout (PaymentSheet)
    private val _startStripeCheckout = MutableStateFlow<StartStripeCheckoutState>(StartStripeCheckoutState.Idle)
    @NativeCoroutinesState
    val startStripeCheckout: StateFlow<StartStripeCheckoutState> = _startStripeCheckout.asStateFlow()

    // 3. Estado para acciones puntuales (Borrar carta, guardar carta, cancelar sub, refund)
    private val _actionUiState = MutableStateFlow<ActionUiState>(ActionUiState.Idle)
    @NativeCoroutinesState
    val actionUiState = _actionUiState.asStateFlow()

    // 4. Estado para Suscripciones
    private val _subscriptionUiState = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Idle)
    @NativeCoroutinesState
    val subscriptionUiState = _subscriptionUiState.asStateFlow()

    fun startStripeCheckout(
        amount: Double,
        currency: String,
        customerId: String,
        paymentMethodId: String? = null,
        couponCode: String? = null,
        billing: BillingPrefill? = null
    ) {
        viewModelScope.launch {
            _startStripeCheckout.value = StartStripeCheckoutState.Loading

            try {
                val request = CreatePaymentIntentRequest(
                    amount = amount,
                    currency = currency,
                    customerId = customerId,
                    paymentMethodId = paymentMethodId,
                    metadata = mapOf(
                        "coupon_code" to (couponCode ?: ""),
                        "billing_name" to (billing?.name ?: ""),
                        "billing_email" to (billing?.email ?: ""),
                        "billing_address" to (billing?.addressLine1 ?: ""),
                        "billing_postal_code" to (billing?.postalCode ?: ""),
                        "billing_city" to (billing?.city ?: "")
                    )
                )

                // 1. Ejecutamos ambas peticiones en paralelo para mayor velocidad
                val paymentIntentDeferred = async {
                    stripeUseCase.createPaymentIntent(request)
                }
                val ephemeralKeyDeferred = async { stripeUseCase.createEphemeralKey(customerId) }

                // En StripeViewModel -> startStripeCheckout
                val piResult = paymentIntentDeferred.await()
                log.debug{ "STRIPE_DEBUG Resultado PI: $piResult" }

                if (piResult.isFailure) {
                    log.error { "STRIPE_DEBUG  Error detallado PI: ${piResult.exceptionOrNull()}" }
                }
                val ekResult = ephemeralKeyDeferred.await()

                // 2. Evaluamos los resultados
                if (piResult.isSuccess && ekResult.isSuccess) {
                    val paymentIntent = piResult.getOrThrow().data
                    val ephemeralKey = ekResult.getOrThrow().data

                    val checkoutConfig = StripeCheckoutConfig(
                        merchantDisplayName = "HumanPerformCenter",
                        allowsDelayedPaymentMethods = true,
                        customerId = customerId,
                        customerEphemeralKeySecret = ephemeralKey?.secret, // Asignamos la llave
                        googlePayEnabled = true,
                        googlePayCountryCode = "ES",
                        googlePayCurrencyCode = currency.uppercase(),
                        billingName = billing?.name,
                        billingEmail = billing?.email,
                        billingAddressLine1 = billing?.addressLine1,
                        billingPostalCode = billing?.postalCode,
                        billingCity = billing?.city
                    )

                    _startStripeCheckout.value = StartStripeCheckoutState.Ready(
                        clientSecret = paymentIntent?.clientSecret ?: "",
                        config = checkoutConfig
                    )
                } else {
                    // 3. Manejo de errores de cualquiera de las dos peticiones
                    val errorMsg = piResult.exceptionOrNull()?.message
                        ?: ekResult.exceptionOrNull()?.message
                        ?: "Error al configurar el pago"
                    _startStripeCheckout.value = StartStripeCheckoutState.Failed(errorMsg)
                }

            } catch (t: Throwable) {
                _startStripeCheckout.value = StartStripeCheckoutState.Failed(
                    t.message ?: "Error inesperado al iniciar checkout"
                )
            }
        }
    }

    /**
     * Procesa el resultado final de la pasarela de Stripe.
     * @param result El resultado traducido desde la UI (Completed, Canceled, Failed).
     */
    fun onPaymentResult(result: SharedPaymentResult) {
        when (result) {
            is SharedPaymentResult.Completed -> {
                // El pago fue exitoso en los servidores de Stripe
                _startStripeCheckout.value = StartStripeCheckoutState.Completed
            }

            is SharedPaymentResult.Canceled -> {
                // El usuario cerró el modal sin completar el pago
                _startStripeCheckout.value = StartStripeCheckoutState.Canceled
            }

            is SharedPaymentResult.Failed -> {
                // Ocurrió un error (tarjeta rechazada, error de red, etc.)
                _startStripeCheckout.value = StartStripeCheckoutState.Failed(
                    result.message ?: "El pago no pudo procesarse"
                )
            }
        }
    }

    // --- GESTIÓN DE TARJETAS (CARD MANAGEMENT) ---

    // Obtener tarjetas (Corregido: quitamos Dispatchers.IO del launch para StateFlow)
    fun getUserCards(userId: Int) {
        viewModelScope.launch {
            _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Loading

            stripeUseCase.getUserCards(userId).fold(
                onSuccess = { methods ->
                    _viewPaymentMethodsUiState.value = if (methods.isEmpty()) {
                        PaymentMethodsUiState.Empty
                    } else {
                        PaymentMethodsUiState.Success(methods)
                    }
                },
                onFailure = {
                    _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Error(it.message ?: "Error al cargar tarjetas")
                }
            )
        }
    }

    // Guardar una nueva tarjeta
    fun saveCard(paymentMethodId: String, userIdToRefresh: Int? = null) {
        performAction(
            action = { stripeUseCase.saveCard(paymentMethodId) },
            onSuccess = {
                // Si pasamos el ID, refrescamos la lista automáticamente
                if (userIdToRefresh != null) getUserCards(userIdToRefresh)
            }
        )
    }

    // Borrar una tarjeta
    fun deleteCard(cardId: String, userIdToRefresh: Int? = null) {
        performAction(
            action = { stripeUseCase.deleteCard(cardId) },
            onSuccess = {
                if (userIdToRefresh != null) getUserCards(userIdToRefresh)
            }
        )
    }

    // Establecer tarjeta por defecto
    fun setDefaultCard(cardId: String, userIdToRefresh: Int? = null) {
        performAction(
            action = { stripeUseCase.setDefaultCard(cardId) },
            onSuccess = {
                if (userIdToRefresh != null) getUserCards(userIdToRefresh)
            }
        )
    }

    // --- SUSCRIPCIONES ---

    fun createSubscription(priceId: String) {
        viewModelScope.launch {
            _subscriptionUiState.value = SubscriptionUiState.Loading
            stripeUseCase.createSubscription(priceId).fold(
                onSuccess = {
                    _subscriptionUiState.value = SubscriptionUiState.Success(it)
                },
                onFailure = {
                    _subscriptionUiState.value = SubscriptionUiState.Error(it.message ?: "Error en suscripción")
                }
            )
        }
    }

    fun cancelSubscription(subscriptionId: String) {
        performAction(
            action = { stripeUseCase.cancelSubscription(subscriptionId) }
        )
    }

    // --- CUSTOMER & OTROS ---

    suspend fun createOrGetCustomer(): String? {
        val result = stripeUseCase.createOrGetCustomer()
        return result.fold(
            onSuccess = { response ->
                // Podrías guardar el ID en una variable interna o StateFlow si lo necesitas después
                response.data?.customerId
            },
            onFailure = {
                // Manejar error (ej. log o actualizar un estado de error)
                null
            }
        )
    }

    fun createRefund(paymentIntentId: String, amount: Int? = null) {
        performAction (
            action = { stripeUseCase.createRefund(paymentIntentId, amount) }
        )
    }

    // --- UTILIDAD PRIVADA PARA REDUCIR CÓDIGO ---

    /**
     * Función helper para ejecutar acciones que no devuelven datos (Unit),
     * solo Éxito o Fallo (como borrar, guardar, cancelar, setear default).
     */
    private fun performAction(
        action: suspend () -> Result<Unit>,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _actionUiState.value = ActionUiState.Loading
            action().fold(
                onSuccess = {
                    _actionUiState.value = ActionUiState.Success
                    onSuccess()
                },
                onFailure = {
                    _actionUiState.value = ActionUiState.Error(it.message ?: "Error en la operación")
                }
            )
        }
    }

    // --- RESET STATES ---

    fun resetActionState() {
        _actionUiState.value = ActionUiState.Idle
    }

    fun onStripeCompleted() {
        _startStripeCheckout.value = StartStripeCheckoutState.Completed
    }

    fun onStripeCanceled() {
        _startStripeCheckout.value = StartStripeCheckoutState.Canceled
    }

    fun onStripeFailed(message: String) {
        _startStripeCheckout.value = StartStripeCheckoutState.Failed(message)
    }

    fun onSheetPresented() {
        if (_startStripeCheckout.value is StartStripeCheckoutState.Ready) {
            _startStripeCheckout.value = StartStripeCheckoutState.Processing
        }
    }
}
