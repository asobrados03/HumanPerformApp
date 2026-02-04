package com.humanperformcenter.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.presentation.ui.StartStripeCheckoutState
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

private const val TAG = "STRIPE_DEBUG"

@Composable
fun StripeCheckoutScreen(
    navController: NavHostController,
    stripeViewModel: StripeViewModel,
    serviceProductViewModel: ServiceProductViewModel,
    userId: Int
) {
    val context = LocalContext.current
    val checkoutState by stripeViewModel.startStripeCheckout.collectAsStateWithLifecycle()

    val paymentSheet = remember { PaymentSheet.Builder(::onPaymentSheetResult) }.build()

    // --- 1. LÓGICA DE INICIALIZACIÓN ---
    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle
        val productPrice = handle?.get<Double>("selected_product_price")
        val coupon = handle?.get<String>("selected_coupon")

        Log.d(TAG, "Lanzando Checkout -> Precio: $productPrice, Cupón: $coupon")

        // Validación temprana: si no hay precio o ID, no podemos seguir
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
                couponCode = coupon
            )
        } else {
            // Si aquí llega null, revisa el mapeo del JSON en tu Data Class (StripeCustomerResponse)
            Log.e(TAG, "Error: El CustomerId llegó como null al código Kotlin")
            stripeViewModel.onStripeFailed("Error al identificar el usuario en la pasarela.")
        }
    }

    // --- 2. MOSTRAR EL MODAL CUANDO EL VM DIGA "READY" ---
    LaunchedEffect(checkoutState) {
        if (checkoutState is StartStripeCheckoutState.Ready) {
            val state = checkoutState as StartStripeCheckoutState.Ready
            Log.d(TAG, "Estado READY detectado. Preparando PaymentSheet...")

            // Sustituye con tu clave real de Stripe
            PaymentConfiguration.init(context, "pk_test_51SvlGzB2ovMjVN6tdZD5PPw4F5YBwyTVBvnwnDAl7LHO56HNLlpSKXQyNBjTYBC5FrpsHGT1eddIVWnxvt7CLdWO00nAEdbtM2")

            val customerConfig = state.config.customerId?.let {
                PaymentSheet.CustomerConfiguration(
                    id = it,
                    ephemeralKeySecret = state.config.customerEphemeralKeySecret ?: ""
                )
            }

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

    // --- 3. MANEJAR RESULTADOS FINALES Y NAVEGACIÓN ---
    LaunchedEffect(checkoutState) {
        when (val state = checkoutState) {
            is StartStripeCheckoutState.Completed -> {
                Log.d(TAG, "Pago completado. Asignando producto...")
                val productId = navController.currentBackStackEntry?.savedStateHandle?.get<Int>("selected_product_id")
                val coupon = navController.currentBackStackEntry?.savedStateHandle?.get<String>("selected_coupon")

                if (productId != null) {
                    serviceProductViewModel.assignProductToUser(userId, productId, "card", coupon)
                }
                navController.navigate("PaymentSuccess") {
                    popUpTo("StripeCheckout") { inclusive = true }
                }
            }
            is StartStripeCheckoutState.Failed -> {
                Log.e(TAG, "Checkout fallido: ${state.message}")
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
            is StartStripeCheckoutState.Canceled -> {
                Log.d(TAG, "Checkout cancelado por el usuario")
                navController.popBackStack()
            }
            else -> Unit
        }
    }

    // --- 4. UI ---
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (checkoutState) {
            is StartStripeCheckoutState.Loading,
            is StartStripeCheckoutState.Processing -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Procesando pago...")
                }
            }
            is StartStripeCheckoutState.Idle -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(strokeWidth = 2.dp) // Spinner sutil
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Iniciando pasarela...")
                }
            }
            else -> { /* Estados finalizados o Ready no muestran UI base */ }
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

private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
    when(paymentSheetResult) {
        is PaymentSheetResult.Canceled -> Log.d(TAG, "PaymentSheet: El usuario canceló")
        is PaymentSheetResult.Failed -> Log.e(TAG, "PaymentSheet Error: ${paymentSheetResult.error.message}")
        is PaymentSheetResult.Completed -> Log.d(TAG, "PaymentSheet: Completado con éxito")
    }
}
