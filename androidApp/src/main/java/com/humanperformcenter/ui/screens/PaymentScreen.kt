package com.humanperformcenter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.PaymentSuccess
import com.humanperformcenter.shared.presentation.viewmodel.PaymentViewModel
import com.humanperformcenter.ui.components.PaymentWebView

@Composable
fun PaymentScreen(viewModel: PaymentViewModel, navController: NavHostController) {
    val paymentUrl by viewModel.paymentUrl.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(paymentUrl) {
        println("🔗 URL de pago recibida en pantalla: $paymentUrl")
    }


    when {
        paymentUrl == null && error == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        /*paymentUrl != null -> {
            PaymentWebView(
                url = paymentUrl!!,
                onPaymentSuccess = {
                    Toast.makeText(context, "Pago exitoso", Toast.LENGTH_LONG).show()
                    viewModel.limpiarEstado()
                    navController.navigate(PaymentSuccess)
                },
                onPaymentCancelled = {
                    Toast.makeText(context, "Pago cancelado", Toast.LENGTH_LONG).show()
                    viewModel.limpiarEstado()
                }
            )
        }*/
        paymentUrl != null -> {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "🔗 URL de pago:\n$paymentUrl",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier.weight(1f) // El WebView ocupa el resto de la pantalla
                ) {
                    PaymentWebView(
                        url = paymentUrl!!,
                        onPaymentSuccess = {
                            Toast.makeText(context, "Pago exitoso", Toast.LENGTH_LONG).show()
                            viewModel.limpiarEstado()
                            navController.navigate(PaymentSuccess)
                        },
                        onPaymentCancelled = {
                            Toast.makeText(context, "Pago cancelado", Toast.LENGTH_LONG).show()
                            viewModel.limpiarEstado()
                            navController.popBackStack()
                        }
                    )
                }
            }
        }


        error != null -> {
            Column {
                Text("Error: $error", color = Color.Red)
                Button(onClick = { viewModel.limpiarEstado() }) {
                    Text("Reintentar")
                }
            }
        }

    }
}


