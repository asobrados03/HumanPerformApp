package com.humanperformcenter.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.PaymentSuccess
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel

@Composable
fun StripeSubscriptionScreen(
    navController: NavHostController,
    stripeViewModel: StripeViewModel,
    userId: Int,
    onClose: () -> Unit
) {
    val checkoutState by stripeViewModel.startStripeCheckout.collectAsStateWithLifecycle()

    val handle = navController.previousBackStackEntry?.savedStateHandle
    val productPrice = handle?.get<Double>("selected_product_price")

    // Inicialización específica para suscripción
    LaunchedEffect(Unit) {
        val priceId = handle?.get<String>("selected_price_id") // ID de Stripe: price_...

        if (priceId == null) {
            stripeViewModel.onStripeFailed("ID de suscripción no encontrado.")
            return@LaunchedEffect
        }

        val customerId = stripeViewModel.createOrGetCustomer()

        val productId = handle.get<Int>("selected_product_id") ?: 0 // ID del producto en tu sistema
        val coupon = handle.get<String>("selected_coupon")

        if (customerId != null) {
            // Llamamos a la nueva función de suscripción
            stripeViewModel.startStripeSubscription(
                priceId = priceId,
                customerId = customerId,
                userId = userId,
                productId = productId,
                couponCode = coupon
            )
        } else {
            stripeViewModel.onStripeFailed("Error al identificar el cliente.")
        }
    }

    StripeCheckoutGate(
        checkoutState,
        stripeViewModel,
        navController,
        onClose,
        PaymentSuccess,
        isSubscription = true,
        price = productPrice
    )
}