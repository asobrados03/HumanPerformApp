package com.humanperformcenter.ui.components.hire_product

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.Service
import com.humanperformcenter.app.navigation.StripeSinglePayment
import com.humanperformcenter.app.navigation.StripeSubscription
import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.presentation.ui.AssignEvent
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel

@Composable
fun PaymentSelectionContent(
    product: Product,
    userCoupons: List<Coupon>,
    couponCode: String,
    navController: NavHostController,
    onElectronicWalletPayment: () -> Unit,
    serviceProductViewModel: ServiceProductViewModel,
) {
    var shouldShowWalletDialog by remember { mutableStateOf(false) }
    // Aquí puedes meter más lógica UI para mostrar/ocultar tarjetas guardadas
    val context = LocalContext.current

    LaunchedEffect(serviceProductViewModel.assignEvent) {
        serviceProductViewModel.assignEvent.collect { event ->
            when (event) {
                is AssignEvent.Success -> {
                    // El pago fue bien y el producto está asignado
                    navController.navigate(Service) {
                        // Opcional: Limpia el historial para que no pueda volver atrás al pago
                        popUpTo(Service) { inclusive = true }
                    }
                    Toast.makeText(
                        context, "El pago del producto fue exitoso y ya está asignado a tu cuenta.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is AssignEvent.Error -> {
                    // Aquí podrías mostrar un Toast o un error si no había saldo
                    Log.e("ERROR PAYMENT","Error en el pago: ${event.message}")
                    Toast.makeText(
                        context, "Error en el pago: ${event.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    )  {
        Text("Selecciona método de pago", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        val totalAmount = serviceProductViewModel.calculateDiscountedPrice(
            product.id,
            product.price ?: 0.0,
            userCoupons
        )

        // 🔵 Botón principal — Stripe
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔵 Botón principal — Stripe
            Button(
                onClick = {
                    val normalizedCouponCode = couponCode.takeIf { it.isNotBlank() }

                    Log.d(
                        "NAVIGATION_DEBUG",
                        "Navegando a pago: price=$totalAmount, id=${product.id}, coupon=$normalizedCouponCode"
                    )

                    if (product.typeOfProduct == "recurrent") {
                        val priceId = product.priceId
                        if (priceId.isNullOrBlank()) {
                            Toast.makeText(
                                context,
                                "No se ha podido iniciar la suscripción.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

                        navController.navigate(
                            StripeSubscription(
                                productPrice = totalAmount,
                                productId = product.id,
                                priceId = priceId,
                                couponCode = normalizedCouponCode
                            )
                        )
                    } else {
                        navController.navigate(
                            StripeSinglePayment(
                                productPrice = totalAmount,
                                productId = product.id,
                                couponCode = normalizedCouponCode
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF635BFF), // Stripe brand
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Pagar $totalAmount €",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // 🛡️ Microcopy de seguridad
            Text(
                text = "Pago seguro gestionado por Stripe",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ⚪ Botón secundario — Cartera virtual
            OutlinedButton(
                onClick = { shouldShowWalletDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Usar saldo de la cartera virtual",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }

    if (shouldShowWalletDialog) {
        AlertDialog(
            onDismissRequest = { shouldShowWalletDialog = false },
            title = { Text("Confirmar pago") },
            text = { Text("¿Pagar con saldo virtual?") },
            confirmButton = {
                TextButton(onClick = {
                    shouldShowWalletDialog = false
                    onElectronicWalletPayment()
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { shouldShowWalletDialog = false }) { Text("Cancelar") }
            }
        )
    }
}