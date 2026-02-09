package com.humanperformcenter.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.PaymentSuccess
import com.humanperformcenter.app.navigation.StripeSinglePayment
import com.humanperformcenter.shared.presentation.ui.StartStripeCheckoutState
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel
import com.humanperformcenter.ui.components.app.LogoAppBar
import com.humanperformcenter.ui.components.hire_product.CheckoutErrorView
import com.humanperformcenter.ui.components.hire_product.StatusView
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheet.Builder
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.delay

private const val TAG = "STRIPE_DEBUG"

@Composable
fun StripeSinglePaymentScreen(
    navController: NavHostController,
    stripeViewModel: StripeViewModel,
    userId: Int,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val checkoutState by stripeViewModel.startStripeCheckout.collectAsStateWithLifecycle()

    val paymentResultCallback: (PaymentSheetResult) -> Unit = { paymentSheetResult ->
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                Log.d(TAG, "PaymentSheet: El usuario canceló")
                stripeViewModel.onCheckoutCanceled()
            }

            is PaymentSheetResult.Failed -> {
                Log.e(TAG, "PaymentSheet Error: ${paymentSheetResult.error.message}")
                stripeViewModel.onCheckoutFailed(paymentSheetResult.error.message
                    ?: "Error desconocido"
                )
                stripeViewModel.onStripeFailed(
                    paymentSheetResult.error.message ?: "Error desconocido"
                )
            }

            is PaymentSheetResult.Completed -> {
                Log.d(TAG, "PaymentSheet: Completado con éxito")
                stripeViewModel.onCheckoutCompleted()
            }
        }
    }

    val paymentSheet = remember(paymentResultCallback) {
        Builder(paymentResultCallback)
    }.build()

    // LÓGICA DE INICIALIZACIÓN
    LaunchedEffect(Unit) {
        Log.d(TAG, "BackStack actual: ${navController.currentBackStackEntry?.destination?.route}")
        Log.d(TAG, "BackStack anterior: ${navController.previousBackStackEntry?.destination?.route}")

        val handle = navController.previousBackStackEntry?.savedStateHandle

        if (handle == null) {
            Log.e(TAG, "previousBackStackEntry.savedStateHandle es NULL!")
        } else {
            Log.d(TAG, "savedStateHandle encontrado, leyendo datos...")
        }

        val productPrice = handle?.get<Double>("selected_product_price")
        val coupon = handle?.get<String>("selected_coupon")
        val productId = handle?.get<Int>("selected_product_id")

        Log.d(TAG, "Lanzando Checkout -> Precio: $productPrice, Cupón: $coupon, ID: $productId")

        if (productPrice == null) {
            Log.e(TAG, "Error: Datos insuficientes en el Navigation Handle")
            stripeViewModel.onStripeFailed("Información de producto incompleta.")
            return@LaunchedEffect
        }

        // 1. Obtenemos el cliente (Ya vimos en Postman que el endpoint funciona)
        Log.d(TAG, "Pidiendo CustomerId al servidor...")
        val customerId = stripeViewModel.createOrGetCustomer()

        // 2. Verificamos que el mapeo del CustomerId haya funcionado en la App
        if (customerId != null) {
            Log.d(TAG, "CustomerId recibido correctamente: $customerId. Iniciando Checkout...")

            // 3. Disparamos la pasarela con el precio que ya tenemos
            stripeViewModel.startStripeCheckout(
                amount = productPrice,
                currency = "eur",
                customerId = customerId,
                couponCode = coupon,
                productId = productId,
                userId = userId
            )
        } else {
            // Si aquí llega null, revisa el mapeo del JSON en tu Data Class (StripeCustomerResponse)
            Log.e(TAG, "Error: El CustomerId llegó como null al código Kotlin")
            stripeViewModel.onStripeFailed("Error al identificar el usuario en la pasarela.")
        }
    }

    // ABRIR PAYMENT SHEET CUANDO EL VM ESTÉ READY
    LaunchedEffect(checkoutState) {
        if (checkoutState is StartStripeCheckoutState.Ready) {
            val state = checkoutState as StartStripeCheckoutState.Ready
            Log.d(TAG, "Estado READY detectado. Preparando PaymentSheet...")

            Log.d(TAG, "Configuración recibida: PublishableKey=" +
                    "${state.config.publishableKey}, CustomerId=${state.config.customerId}, " +
                    "ClientSecret=${state.clientSecret}")

            val activity = context.findActivity()
            if (activity == null || activity.isFinishing || activity.isDestroyed) {
                Log.e(TAG, "Activity no válida, cancelando presentación del PaymentSheet")
                stripeViewModel.onStripeFailed("Error al abrir la pasarela de pago")
                return@LaunchedEffect
            }
            // Inicializamos Stripe
            PaymentConfiguration.init(context, state.config.publishableKey)

            val customerConfig = state.config.customerId?.let {
                PaymentSheet.CustomerConfiguration(
                    id = it,
                    ephemeralKeySecret = state.config.customerEphemeralKeySecret ?: ""
                )
            }

            delay(200)

            presentPaymentSheet(
                paymentSheet = paymentSheet,
                customerConfig = customerConfig,
                paymentIntentClientSecret = state.clientSecret,
                merchantName = state.config.merchantDisplayName,
                countryCode = state.config.googlePayCountryCode ?: "ES",
                currencyCode = state.config.googlePayCurrencyCode ?: "EUR"
            )

            stripeViewModel.onSheetPresented()
        }
    }

    // RESULTADOS FINALES Y NAVEGACIÓN
    LaunchedEffect(checkoutState) {
        Log.d(TAG, "LaunchedEffect triggered, estado actual: $checkoutState")
        when (val state = checkoutState) {
            is StartStripeCheckoutState.Completed -> {
                Log.d(TAG, "Pago completado. Navegando a success...")
                stripeViewModel.onCheckoutCompleted()
                delay(5000) // Pequeña pausa para mostrar feedback
                navController.navigate(PaymentSuccess) {
                    popUpTo(StripeSinglePayment) { inclusive = true }
                }
            }
            is StartStripeCheckoutState.Failed -> {
                Log.e(TAG, "Checkout fallido: ${state.message}")
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            is StartStripeCheckoutState.Canceled -> {
                Log.d(TAG, "Checkout cancelado por el usuario")
                stripeViewModel.resetStartCheckoutState() // ← Resetear a Idle
                onClose()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = onClose // Usamos onClose para consistencia
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = checkoutState) {
                is StartStripeCheckoutState.Idle,
                is StartStripeCheckoutState.Loading -> {
                    StatusView(isLoading = true, message = "Preparando conexión segura...")
                }
                is StartStripeCheckoutState.Processing -> {
                    StatusView(isLoading = true, message = "Procesando tu pago...")
                }
                is StartStripeCheckoutState.Ready -> {
                    // Mantenemos feedback visual mientras carga el modal de Stripe
                    StatusView(isLoading = true, message = "Abriendo pasarela de pago...")
                }
                is StartStripeCheckoutState.Completed -> {
                    StatusView(
                        isLoading = false,
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF4CAF50),
                        message = "¡Pago exitoso!"
                    )
                }
                is StartStripeCheckoutState.Failed -> {
                    CheckoutErrorView(
                        message = state.message,
                        onRetry = {
                            // Lógica para reintentar (ej. volver a pedir el intent o reiniciar el estado)
                            stripeViewModel.resetStartCheckoutState()
                        },
                        onClose = onClose
                    )
                }
                else -> {}
            }
        }
    }
}

private fun presentPaymentSheet(
    paymentSheet: PaymentSheet,
    customerConfig: PaymentSheet.CustomerConfiguration?,
    paymentIntentClientSecret: String,
    merchantName: String,
    countryCode: String = "ES", // Por defecto España
    currencyCode: String = "EUR" // Por defecto Euro
) {
    // Configuración de Google Pay
    val googlePayConfiguration = PaymentSheet.GooglePayConfiguration(
        environment = PaymentSheet.GooglePayConfiguration.Environment.Test, // Cambiar a Production al publicar
        countryCode = countryCode,
        currencyCode = currencyCode
    )

    val configuration = PaymentSheet.Configuration.Builder(merchantName)
        .customer(customerConfig)
        .allowsDelayedPaymentMethods(true)
        .googlePay(googlePayConfiguration) // <--- Aquí activamos Google Pay
        .build()

    paymentSheet.presentWithPaymentIntent(
        paymentIntentClientSecret,
        configuration
    )
}

fun Context.findActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}
