package com.humanperformcenter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.humanperformcenter.shared.presentation.viewmodel.PaymentViewModel
import com.humanperformcenter.ui.components.PaymentWebView

@Composable
fun PaymentScreen(viewModel: PaymentViewModel) {
    val paymentUrl by viewModel.paymentUrl.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    when {
        paymentUrl == null && error == null -> {
            CircularProgressIndicator()
        }

        paymentUrl != null -> {
            PaymentWebView(
                url = paymentUrl!!,
                onPaymentSuccess = {
                    Toast.makeText(context, "Pago exitoso", Toast.LENGTH_LONG).show()
                    viewModel.limpiarEstado()
                },
                onPaymentCancelled = {
                    Toast.makeText(context, "Pago cancelado", Toast.LENGTH_LONG).show()
                    viewModel.limpiarEstado()
                }
            )
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


