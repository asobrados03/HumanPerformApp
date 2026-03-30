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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.PaymentSuccess
import com.humanperformcenter.shared.data.local.impl.AuthLocalDataSourceImpl
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
import kotlinx.coroutines.flow.firstOrNull

private const val TAG = "STRIPE_DEBUG"

@Composable
fun StripeCheckoutGate(
    checkoutState: StartStripeCheckoutState,
    stripeViewModel: StripeViewModel,
    navController: NavHostController,
    onClose: () -> Unit,
    isSubscription: Boolean,
    price: Double? = null
) {
    val context = LocalContext.current

    // 1. Configurar el PaymentSheet
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

    // 2. Lógica para abrir la pasarela (Tu primer LaunchedEffect)
    // ABRIR PAYMENT SHEET CUANDO EL VM ESTÉ READY
    LaunchedEffect(checkoutState) {
        if (checkoutState is StartStripeCheckoutState.Ready) {
            Log.d(TAG, "Estado READY detectado. Preparando PaymentSheet...")

            Log.d(TAG, "Configuración recibida: PublishableKey=" +
                    "${checkoutState.config.publishableKey}, " +
                    "CustomerId=${checkoutState.config.customerId}, " +
                    "ClientSecret=${checkoutState.clientSecret}")

            val activity = context.findActivity()
            if (activity == null || activity.isFinishing || activity.isDestroyed) {
                Log.e(TAG, "Activity no válida, cancelando presentación del PaymentSheet")
                stripeViewModel.onStripeFailed("Error al abrir la pasarela de pago")
                return@LaunchedEffect
            }
            // Inicializamos Stripe
            PaymentConfiguration.init(context, checkoutState.config.publishableKey)

            val customerConfig = checkoutState.config.customerId?.let {
                PaymentSheet.CustomerConfiguration(
                    id = it,
                    ephemeralKeySecret = checkoutState.config.customerEphemeralKeySecret ?: ""
                )
            }

            val userData = AuthLocalDataSourceImpl.userFlow().firstOrNull()
            val userEmail = userData?.email ?: ""
            val userName = userData?.fullName ?: ""
            val userPhone = userData?.phone ?: ""

            delay(200)

            presentPaymentSheet(
                paymentSheet = paymentSheet,
                customerConfig = customerConfig,
                paymentIntentClientSecret = checkoutState.clientSecret,
                merchantName = checkoutState.config.merchantDisplayName,
                countryCode = checkoutState.config.googlePayCountryCode ?: "ES",
                currencyCode = checkoutState.config.googlePayCurrencyCode ?: "EUR",
                isSubscription = isSubscription,
                price = price,
                userEmail = userEmail,
                userName = userName,
                userPhone = userPhone
            )

            stripeViewModel.onSheetPresented()
        }
    }

    // RESULTADOS FINALES Y NAVEGACIÓN
    LaunchedEffect(checkoutState) {
        Log.d(TAG, "LaunchedEffect triggered, estado actual: $checkoutState")
        when (checkoutState) {
            is StartStripeCheckoutState.Completed -> {
                Log.d(TAG, "Pago completado. Navegando a success...")
                stripeViewModel.onCheckoutCompleted()
                delay(500) // Pequeña pausa para mostrar feedback
                navController.navigate(PaymentSuccess) {
                    popUpTo(navController.currentBackStackEntry?.destination?.route ?: "")
                    { inclusive = true }
                }
            }
            is StartStripeCheckoutState.Failed -> {
                Log.e(TAG, "Checkout fallido: ${checkoutState.message}")
                Toast.makeText(context, checkoutState.message, Toast.LENGTH_LONG).show()
            }
            is StartStripeCheckoutState.Canceled -> {
                Log.d(TAG, "Checkout cancelado por el usuario")
                stripeViewModel.resetStartCheckoutState() // ← Resetear a Idle
                onClose()
            }
            else -> Unit
        }
    }

    // 4. El Scaffold y las vistas de estado que ya tienes
    Scaffold(
        topBar = { LogoAppBar(showBackArrow = true, onBackNavClicked = onClose) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (checkoutState) {
                is StartStripeCheckoutState.Loading -> {
                    StatusView(true, "Conectando...")
                }
                is StartStripeCheckoutState.Processing -> {
                    StatusView(true, "Procesando...")
                }
                is StartStripeCheckoutState.Completed -> {
                    StatusView(false, message = "¡Éxito!", icon = Icons.Default.CheckCircle)
                }
                is StartStripeCheckoutState.Failed -> {
                    CheckoutErrorView(
                        message = checkoutState.message,
                        onRetry = { stripeViewModel.resetStartCheckoutState() },
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
    countryCode: String = "ES",
    currencyCode: String = "EUR",
    isSubscription: Boolean = false,
    price: Double? = null,
    userEmail: String? = null, // Añadimos parámetros dinámicos
    userName: String? = null,
    userPhone: String? = null
) {
    val googlePayConfiguration = PaymentSheet.GooglePayConfiguration(
        environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
        countryCode = countryCode,
        currencyCode = currencyCode
    )

    val configBuilder = PaymentSheet.Configuration.Builder(merchantName)
        .customer(customerConfig)
        .allowsDelayedPaymentMethods(true)
        .googlePay(googlePayConfiguration)
        .defaultBillingDetails(PaymentSheet.BillingDetails(
            email = userEmail,
            name = userName,
            phone = userPhone
        ))

    if (isSubscription) {
        configBuilder.primaryButtonLabel("Suscríbete por ${"%.2f".format(price ?: 0.0)}€/mes")
    }

    val configuration = configBuilder.build()

    Log.d(TAG, "Lanzando PaymentSheet para suscripción: $isSubscription")

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
