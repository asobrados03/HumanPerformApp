package com.humanperformcenter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import com.humanperformcenter.ui.viewmodel.PaymentViewModel
import com.humanperformcenter.ui.components.PaymentWebView

@Composable
fun PaymentScreen(viewModel: PaymentViewModel, navController: NavHostController) {
    val paymentUrl by viewModel.paymentUrl.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    when {
        paymentUrl == null && error == null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        paymentUrl != null -> {
            PaymentWebView(
                url = paymentUrl!!,
                onPaymentSuccess = {
                    Toast.makeText(context, "Pago exitoso", Toast.LENGTH_SHORT).show()
                    viewModel.clearState()
                    navController.navigate(PaymentSuccess)
                },
                onPaymentCancelled = {
                    Toast.makeText(context, "Pago cancelado", Toast.LENGTH_SHORT).show()
                    viewModel.clearState()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxSize().padding(top = 16.dp)
            )
        }

        error != null -> {
            Column(Modifier.padding(16.dp)) {
                Text("Error: $error", color = Color.Red)
                Button(onClick = { viewModel.clearState() }) {
                    Text("Reintentar")
                }
            }
        }
    }
}



