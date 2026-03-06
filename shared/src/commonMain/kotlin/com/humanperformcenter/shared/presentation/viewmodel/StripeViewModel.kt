package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.payment.CreatePaymentIntentRequest
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import com.humanperformcenter.shared.presentation.ui.ActionUiState
import com.humanperformcenter.shared.presentation.ui.AddPaymentMethodSheetData
import com.humanperformcenter.shared.presentation.ui.AddPaymentMethodUiState
import com.humanperformcenter.shared.presentation.ui.PaymentMethodsUiState
import com.humanperformcenter.shared.presentation.ui.StartStripeCheckoutState
import com.humanperformcenter.shared.presentation.ui.StripeCheckoutConfig
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

    private val _viewPaymentMethodsUiState = MutableStateFlow<PaymentMethodsUiState>(PaymentMethodsUiState.Empty)
    @NativeCoroutinesState
    val viewPaymentMethodsUiState: StateFlow<PaymentMethodsUiState> = _viewPaymentMethodsUiState.asStateFlow()

    private val _startStripeCheckout = MutableStateFlow<StartStripeCheckoutState>(StartStripeCheckoutState.Idle)
    @NativeCoroutinesState
    val startStripeCheckout: StateFlow<StartStripeCheckoutState> = _startStripeCheckout.asStateFlow()

    // Estado para acciones puntuales (Borrar tarjeta, guardar tarjeta, cancelar sub, refund)
    private val _actionUiState = MutableStateFlow<ActionUiState>(ActionUiState.Idle)
    @NativeCoroutinesState
    val actionUiState = _actionUiState.asStateFlow()

    private val _addPaymentMethodUiState = MutableStateFlow<AddPaymentMethodUiState>(AddPaymentMethodUiState.Idle)
    @NativeCoroutinesState
    val addPaymentMethodUiState: StateFlow<AddPaymentMethodUiState> = _addPaymentMethodUiState.asStateFlow()

    fun startStripeCheckout(
        amount: Double,
        currency: String,
        customerId: String,
        productId: Int? = null,
        userId: Int? = null,
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
                        "product_id" to productId.toString(),
                        "user_id" to userId.toString(),
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

                val publishableKeyDeferred = async { stripeUseCase.getPublishableKey() }

                // En StripeViewModel -> startStripeCheckout
                val piResult = paymentIntentDeferred.await()
                log.debug{ "STRIPE_DEBUG Resultado PI: $piResult" }

                if (piResult.isFailure) {
                    log.error { "STRIPE_DEBUG  Error detallado PI: ${piResult.exceptionOrNull()}" }
                }
                val ekResult = ephemeralKeyDeferred.await()

                val publishableKeyResult = publishableKeyDeferred.await()
                if (publishableKeyResult.isFailure) {
                    log.error { "STRIPE_DEBUG  Error detallado Publishable Key: " +
                            "${publishableKeyResult.exceptionOrNull()}"
                    }
                }

                // 2. Evaluamos los resultados
                if (piResult.isSuccess && ekResult.isSuccess) {
                    val paymentIntent = piResult.getOrThrow().data
                    val ephemeralKey = ekResult.getOrThrow().data
                    val publishableKey = publishableKeyResult.getOrThrow()

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
                        billingCity = billing?.city,
                        publishableKey = publishableKey
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

    fun onCheckoutCanceled() {
        _startStripeCheckout.value = StartStripeCheckoutState.Canceled
    }

    fun onCheckoutCompleted() {
        _startStripeCheckout.value = StartStripeCheckoutState.Completed
    }

    fun onCheckoutFailed(message: String) {
        _startStripeCheckout.value = StartStripeCheckoutState.Failed(message)
    }

    fun resetStartCheckoutState() {
        _startStripeCheckout.value = StartStripeCheckoutState.Idle
    }

    fun prepareAddPaymentMethod(userId: Int) {
        viewModelScope.launch {
            _addPaymentMethodUiState.value = AddPaymentMethodUiState.Loading

            val setupConfigResult = stripeUseCase.createSetupConfig(userId)
            setupConfigResult.fold(
                onSuccess = { response ->
                    val data = response.data
                    val customerId = data?.customerId
                    val clientSecret = data?.clientSecret
                    val ephemeralKey = data?.ephemeralKey

                    if (customerId.isNullOrBlank() || clientSecret.isNullOrBlank() || ephemeralKey.isNullOrBlank()) {
                        _addPaymentMethodUiState.value = AddPaymentMethodUiState.Failed(
                            "Respuesta inválida en setup-config"
                        )
                        return@fold
                    }

                    val publishableKeyResult = stripeUseCase.getPublishableKey()
                    if (publishableKeyResult.isFailure) {
                        _addPaymentMethodUiState.value = AddPaymentMethodUiState.Failed(
                            publishableKeyResult.exceptionOrNull()?.message
                                ?: "Error al preparar el formulario de tarjeta"
                        )
                        return@fold
                    }

                    val publishableKey = publishableKeyResult.getOrThrow()

                    if (publishableKey.isBlank()) {
                        _addPaymentMethodUiState.value = AddPaymentMethodUiState.Failed(
                            "Respuesta inválida al crear credenciales de Stripe"
                        )
                        return@fold
                    }

                    _addPaymentMethodUiState.value = AddPaymentMethodUiState.Ready(
                        AddPaymentMethodSheetData(
                            setupIntentClientSecret = clientSecret,
                            ephemeralKeySecret = ephemeralKey,
                            customerId = customerId,
                            publishableKey = publishableKey
                        )
                    )
                },
                onFailure = {
                    _addPaymentMethodUiState.value = AddPaymentMethodUiState.Failed(
                        it.message ?: "Error al preparar el formulario de tarjeta"
                    )
                }
            )
        }
    }

    fun onAddPaymentMethodCompleted() {
        _addPaymentMethodUiState.value = AddPaymentMethodUiState.Completed
        loadPaymentMethods()
    }

    fun onAddPaymentMethodCanceled() {
        _addPaymentMethodUiState.value = AddPaymentMethodUiState.Canceled
    }

    fun onAddPaymentMethodFailed(message: String) {
        _addPaymentMethodUiState.value = AddPaymentMethodUiState.Failed(message)
    }

    fun resetAddPaymentMethodState() {
        _addPaymentMethodUiState.value = AddPaymentMethodUiState.Idle
    }

    // --- GESTIÓN DE TARJETAS (CARD MANAGEMENT) ---

    fun loadPaymentMethods() {
        viewModelScope.launch {
            _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Loading

            val customerResult = stripeUseCase.createOrGetCustomer()

            customerResult.fold(
                onSuccess = { customerResponse ->
                    val customerId = customerResponse.data?.customerId
                    if (customerId != null) {
                        // 2. Llamamos a la nueva función del repositorio que trae TODO junto
                        fetchCards(customerId)
                    } else {
                        _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Error("No se pudo obtener el ID de cliente")
                    }
                },
                onFailure = {
                    _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Error("Error al identificar usuario")
                }
            )
        }
    }

    private suspend fun fetchCards(customerId: String) {
        stripeUseCase.getUserCards(customerId).fold(
            onSuccess = { container ->
                // Si no hay tarjetas, mostramos estado Empty
                if (container.methods.isEmpty()) {
                    _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Empty
                } else {
                    // Si hay, pasamos la lista y el ID predeterminado directamente
                    _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Success(
                        paymentMethods = container.methods,
                        defaultPaymentMethodId = container.defaultPaymentMethodId
                    )
                }
            },
            onFailure = { error ->
                _viewPaymentMethodsUiState.value = PaymentMethodsUiState.Error(
                    error.message ?: "Error desconocido al cargar métodos de pago"
                )
            }
        )
    }

    // --- SUSCRIPCIONES ---

    fun startStripeSubscription(
        priceId: String,
        customerId: String,
        userId: Int,
        productId: Int,
        couponCode: String? = null
    ) {
        viewModelScope.launch {
            _startStripeCheckout.value = StartStripeCheckoutState.Loading

            stripeUseCase.createSubscription(priceId, userId, productId, couponCode).fold(
                onSuccess = { subDto ->
                    val ephemeralKeyDeferred = async { stripeUseCase.createEphemeralKey(customerId) }
                    val publishableKeyDeferred = async { stripeUseCase.getPublishableKey() }

                    if (publishableKeyDeferred.isCancelled) {
                        _startStripeCheckout.value = StartStripeCheckoutState.Failed(
                            "No se pudo obtener la clave pública"
                        )
                        return@launch
                    }

                    if(ephemeralKeyDeferred.isCancelled) {
                        _startStripeCheckout.value = StartStripeCheckoutState.Failed(
                            "No se pudo obtener la clave efímera"
                        )
                        return@launch
                    }

                    val ephemeralKeyResult = ephemeralKeyDeferred.await()
                    val publishableKeyResult = publishableKeyDeferred.await()

                    if(ephemeralKeyResult.isSuccess && publishableKeyResult.isSuccess) {
                        val ephemeralKey = ephemeralKeyResult.getOrThrow().data
                        val publishableKey = publishableKeyResult.getOrThrow()

                        val checkoutConfig = StripeCheckoutConfig(
                            merchantDisplayName = "HumanPerformCenter",
                            allowsDelayedPaymentMethods = true,
                            customerId = customerId,
                            customerEphemeralKeySecret = ephemeralKey?.secret,
                            googlePayEnabled = true,
                            googlePayCountryCode = "ES",
                            googlePayCurrencyCode = "EUR",
                            publishableKey = publishableKey
                        )

                        _startStripeCheckout.value = StartStripeCheckoutState.Ready(
                            clientSecret = subDto.clientSecret ?: "",
                            config = checkoutConfig
                        )
                    } else {
                        val errorMsg = publishableKeyResult.exceptionOrNull()?.message
                            ?: ephemeralKeyResult.exceptionOrNull()?.message
                            ?: "Error al configurar el pago"
                        _startStripeCheckout.value = StartStripeCheckoutState.Failed(errorMsg)
                        log.error { errorMsg }
                    }
                },
                onFailure = {
                    _startStripeCheckout.value = StartStripeCheckoutState.Failed(it.message ?: "Error")
                    log.error { it.message }
                }
            )
        }
    }

    fun cancelSubscription(subscriptionId: String, productId: Int, userId: Int) {
        performAction(
            action = { stripeUseCase.cancelSubscription(subscriptionId, productId, userId) }
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

    fun detachPaymentMethod(paymentMethodId: String) {
        performAction(
            action = { stripeUseCase.detachPaymentMethod(paymentMethodId) },
            onSuccess = { loadPaymentMethods() }
        )
    }

    fun setDefaultPaymentMethod(paymentMethodId: String) {
        viewModelScope.launch {
            _actionUiState.value = ActionUiState.Loading

            val customerId = stripeUseCase
                .createOrGetCustomer()
                .getOrNull()
                ?.data
                ?.customerId

            if (customerId.isNullOrBlank()) {
                _actionUiState.value = ActionUiState.Error("No se pudo obtener el cliente de Stripe")
                return@launch
            }

            stripeUseCase.setDefaultPaymentMethod(paymentMethodId, customerId).fold(
                onSuccess = {
                    _actionUiState.value = ActionUiState.Success
                    loadPaymentMethods()
                },
                onFailure = {
                    _actionUiState.value = ActionUiState.Error(
                        it.message ?: "No se pudo establecer el método predeterminado"
                    )
                }
            )
        }
    }

    fun resetActionState() {
        _actionUiState.value = ActionUiState.Idle
    }

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

    fun onStripeFailed(message: String) {
        _startStripeCheckout.value = StartStripeCheckoutState.Failed(message)
    }

    fun onSheetPresented() {
        if (_startStripeCheckout.value is StartStripeCheckoutState.Ready) {
            _startStripeCheckout.value = StartStripeCheckoutState.Processing
        }
    }
}
