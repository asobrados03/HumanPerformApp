package com.humanperformcenter.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.PaymentSuccess
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel

private const val TAG = "STRIPE_DEBUG"

@Composable
fun StripeSinglePaymentScreen(
    navController: NavHostController,
    stripeViewModel: StripeViewModel,
    userId: Int,
    onClose: () -> Unit
) {
    val checkoutState by stripeViewModel.startStripeCheckout.collectAsStateWithLifecycle()

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

    StripeCheckoutGate(
        checkoutState,
        stripeViewModel,
        navController,
        onClose,
        PaymentSuccess,
        isSubscription = false
    )
}
