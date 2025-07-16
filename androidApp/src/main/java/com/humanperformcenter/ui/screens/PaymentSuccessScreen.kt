package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.ui.components.LogoAppBar

@Composable
fun PaymentSuccessScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            LogoAppBar(showBackArrow = false)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✅ ¡Pago realizado con éxito!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                navController.popBackStack() // o ir a pantalla principal
            }) {
                Text("Volver")
            }
        }
    }
}
