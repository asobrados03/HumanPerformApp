package com.humanperformcenter.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel

private const val TAG = "STRIPE_DEBUG"

@Composable
fun StripeSinglePaymentScreen(
    navController: NavHostController,
    stripeViewModel: StripeViewModel,
    userId: Int,
    productPrice: Double,
    productId: Int,
    couponCode: String?,
    onClose: () -> Unit
) {
    val checkoutState by stripeViewModel.startStripeCheckout.collectAsStateWithLifecycle()

    // LÓGICA DE INICIALIZACIÓN
    LaunchedEffect(productPrice, productId, couponCode) {
        Log.d(TAG, "Lanzando Checkout -> Precio: $productPrice, Cupón: $couponCode, ID: $productId")

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
                couponCode = couponCode,
                productId = productId,
                userId = userId
            )
        } else {
            // Si aquí llega null, revisa el mapeo del JSON en tu Data Class (StripeCustomerResponse)
            Log.e(TAG, "Error: El CustomerId llegó como null al código Kotlin")
            stripeViewModel.onStripeFailed("Error al identificar el usuario en la pasarela.")
        }
    }

    StripeCheckoutGate(
        checkoutState,
        stripeViewModel,
        navController,
        onClose,
        isSubscription = false
    )
}
