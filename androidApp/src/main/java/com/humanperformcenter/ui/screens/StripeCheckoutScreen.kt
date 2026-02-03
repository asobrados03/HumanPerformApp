package com.humanperformcenter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.PaymentSuccess
import com.humanperformcenter.app.navigation.ProductDetail
import com.humanperformcenter.shared.presentation.ui.StripeCheckoutConfig
import com.humanperformcenter.shared.presentation.ui.StripeUiState
import com.humanperformcenter.shared.presentation.viewmodel.PaymentViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheet.Configuration
import com.stripe.android.paymentsheet.PaymentSheet.CustomerConfiguration
import com.stripe.android.paymentsheet.PaymentSheet.GooglePayConfiguration
import com.stripe.android.paymentsheet.PaymentSheetResult

@Composable
fun StripeCheckoutScreen(
    navController: NavHostController,
    paymentViewModel: PaymentViewModel,
    serviceProductViewModel: ServiceProductViewModel,   // ⬅️ nuevo
    userId: Int,                                        // ⬅️ nuevo
    paymentSheet: PaymentSheet,
    registerPaymentSheetResult: ((PaymentSheetResult) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val state by paymentViewModel.stripeUi.collectAsStateWithLifecycle()

    // Evita condición de carrera
    var registered by remember { mutableStateOf(false) }

    // 1) Registrar handler de resultado
    LaunchedEffect(Unit) {
        registerPaymentSheetResult { result ->
            paymentViewModel.onPaymentSheetResult(result)
        }
        registered = true
    }

    // 2) Presentar PaymentSheet solo tras registrar handler
    LaunchedEffect(state, registered) {
        val s = state
        if (registered && s is StripeUiState.Ready) {
            val config = buildStripeConfig(s.config)
            paymentSheet.presentWithPaymentIntent(s.clientSecret, config)
            paymentViewModel.resetStripeReady()
        }
    }

    // 3) Reaccionar al resultado → asignar producto, navegar y refrescar
    LaunchedEffect(state) {
        val result = (state as? StripeUiState.Result)?.result ?: return@LaunchedEffect
        when (result) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(context, "Pago exitoso", Toast.LENGTH_SHORT).show()

                // productId desde savedState (o guárdalo en el VM si prefieres)
                val productId: Int? =
                    navController.currentBackStackEntry?.savedStateHandle?.get("selected_product_id")
                        ?: navController.previousBackStackEntry?.savedStateHandle?.get("selected_product_id")

                // cupón (si lo guardaste)
                val coupon: String? =
                    navController.currentBackStackEntry?.savedStateHandle?.get<String>("selected_coupon")
                        ?: navController.previousBackStackEntry?.savedStateHandle?.get<String>("selected_coupon")

                if (productId == null) {
                    navController.popBackStack()
                    navController.navigate(PaymentSuccess)
                    return@LaunchedEffect
                }

                // 🔗 Asigna el producto en tu backend y luego navega
                serviceProductViewModel.assignProductToUser(
                    userId = userId,
                    productId = productId,
                    paymentMethod = "card",
                    couponCode = coupon
                ) { success, error ->
                    if (success) {
                        Toast.makeText(context, "Producto asignado", Toast.LENGTH_SHORT).show()

                        // refresca productos del usuario
                        serviceProductViewModel.loadUserProducts(userId)

                        // limpia savedState
                        navController.currentBackStackEntry?.savedStateHandle?.set("selected_coupon", null)
                        navController.currentBackStackEntry?.savedStateHandle?.set("selected_product_id", null)

                        // navega a detalle
                        navController.popBackStack()
                        navController.navigate(ProductDetail(productId = productId))
                    } else {
                        Toast.makeText(
                            context,
                            error ?: "Error al asignar producto",
                            Toast.LENGTH_LONG
                        ).show()
                        // decide si navegar igualmente o volver atrás
                        navController.popBackStack()
                        navController.navigate(PaymentSuccess)
                    }
                }
            }
            is PaymentSheetResult.Canceled -> {
                navController.previousBackStackEntry?.savedStateHandle?.set("payment_result", false)
                navController.popBackStack()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(
                    context,
                    "Error en el pago: ${result.error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                navController.popBackStack()
            }
        }
    }

    // Loading
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

private fun buildStripeConfig(config: StripeCheckoutConfig): Configuration {
    val customerConfig = if (!config.customerId.isNullOrBlank() && !config.customerEphemeralKeySecret.isNullOrBlank()) {
        CustomerConfiguration(
            id = config.customerId,
            ephemeralKeySecret = config.customerEphemeralKeySecret
        )
    } else {
        null
    }

    val googlePay = if (config.googlePayEnabled && !config.googlePayCountryCode.isNullOrBlank() && !config.googlePayCurrencyCode.isNullOrBlank()) {
        GooglePayConfiguration(
            environment = GooglePayConfiguration.Environment.Test,
            countryCode = config.googlePayCountryCode,
            currencyCode = config.googlePayCurrencyCode
        )
    } else {
        null
    }

    return Configuration(
        merchantDisplayName = config.merchantDisplayName,
        customer = customerConfig,
        allowsDelayedPaymentMethods = config.allowsDelayedPaymentMethods,
        googlePay = googlePay,
        defaultBillingDetails = PaymentSheet.BillingDetails(
            name = config.billingName,
            email = config.billingEmail,
            address = PaymentSheet.Address(
                line1 = config.billingAddressLine1,
                postalCode = config.billingPostalCode,
                city = config.billingCity
            )
        )
    )
}
