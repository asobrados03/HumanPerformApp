package com.humanperformcenter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.PaymentSuccess
import com.humanperformcenter.app.navigation.ProductDetail
import com.humanperformcenter.shared.presentation.ui.AssignEvent
import com.humanperformcenter.shared.presentation.ui.StripeCheckoutConfig
import com.humanperformcenter.shared.presentation.ui.StripeUiState
import com.humanperformcenter.shared.presentation.viewmodel.PaymentViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheet.Configuration
import com.stripe.android.paymentsheet.PaymentSheet.CustomerConfiguration
import com.stripe.android.paymentsheet.PaymentSheet.GooglePayConfiguration

@Composable
fun StripeCheckoutScreen(
    navController: NavHostController,
    paymentViewModel: PaymentViewModel,
    serviceProductViewModel: ServiceProductViewModel,
    paymentSheet: PaymentSheet,
    userId: Int
) {
    val context = LocalContext.current

    val stripeState by paymentViewModel.stripeUi.collectAsStateWithLifecycle()

    // 🔹 Presentar PaymentSheet cuando el estado esté Ready
    LaunchedEffect(stripeState) {
        val state = stripeState
        if (state is StripeUiState.Ready) {
            val config = buildStripeConfig(state.config)

            paymentSheet.presentWithPaymentIntent(
                state.clientSecret,
                config
            )

            // Evitar doble presentación
            paymentViewModel.reset()
        }
    }

    // 🔹 Reaccionar al resultado del pago
    LaunchedEffect(stripeState) {
        when (val state = stripeState) {
            StripeUiState.Completed -> {
                Toast.makeText(context, "Pago exitoso", Toast.LENGTH_SHORT).show()

                val productId =
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.get<Int>("selected_product_id")
                        ?: navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<Int>("selected_product_id")

                val coupon =
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("selected_coupon")
                        ?: navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<String>("selected_coupon")

                if (productId == null) {
                    navController.popBackStack()
                    navController.navigate(PaymentSuccess)
                    return@LaunchedEffect
                }

                // 🔹 Asignar producto en backend
                serviceProductViewModel.assignProductToUser(
                    userId = userId,
                    productId = productId,
                    paymentMethod = "card",
                    couponCode = coupon
                )
            }

            StripeUiState.Canceled -> {
                navController.popBackStack()
            }

            is StripeUiState.Failed -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }

            else -> Unit
        }
    }

    // 🔹 Escuchar eventos de asignación del producto
    LaunchedEffect(Unit) {
        serviceProductViewModel.assignEvent.collect { event ->
            when (event) {
                is AssignEvent.Success -> {
                    Toast.makeText(context, "Producto asignado", Toast.LENGTH_SHORT).show()

                    // Limpiar savedState
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_coupon", null)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_product_id", null)

                    navController.popBackStack()
                    navController.navigate(ProductDetail(productId = event.productId))
                }

                is AssignEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                    navController.navigate(PaymentSuccess)
                }
            }
        }
    }

    // 🔹 Loading UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private fun buildStripeConfig(config: StripeCheckoutConfig): Configuration {
    val customerConfig = if (!config.customerId.isNullOrBlank() && !config.customerEphemeralKeySecret.isNullOrBlank()) {
        CustomerConfiguration(
            id = config.customerId!!,
            ephemeralKeySecret = config.customerEphemeralKeySecret!!
        )
    } else {
        null
    }

    val googlePay = if (config.googlePayEnabled && !config.googlePayCountryCode.isNullOrBlank() && !config.googlePayCurrencyCode.isNullOrBlank()) {
        GooglePayConfiguration(
            environment = GooglePayConfiguration.Environment.Test,
            countryCode = config.googlePayCountryCode!!,
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
