package com.humanperformcenter.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel

@Composable
fun StripeSubscriptionScreen(
    navController: NavHostController,
    stripeViewModel: StripeViewModel,
    userId: Int,
    productPrice: Double,
    productId: Int,
    priceId: String,
    couponCode: String?,
    onClose: () -> Unit
) {
    val checkoutState by stripeViewModel.startStripeCheckout.collectAsStateWithLifecycle()

    // Inicialización específica para suscripción
    LaunchedEffect(priceId, productId, couponCode) {
        val customerId = stripeViewModel.createOrGetCustomer()

        if (customerId != null) {
            stripeViewModel.startStripeSubscription(
                priceId = priceId,
                customerId = customerId,
                userId = userId,
                productId = productId,
                couponCode = couponCode
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
        isSubscription = true,
        price = productPrice
    )
}