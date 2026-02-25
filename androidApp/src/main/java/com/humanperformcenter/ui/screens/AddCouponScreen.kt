package com.humanperformcenter.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import com.humanperformcenter.ui.components.app.LogoAppBar

@Composable
fun AddCouponScreen(
    userId: Int,
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    val uiState by userViewModel.couponUiState.collectAsStateWithLifecycle()

    // Carga el cupón activo al entrar
    LaunchedEffect(Unit) {
        userViewModel.loadUserCoupon(userId)
    }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Añadir cupón de descuento",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = uiState.code ?: "",
                onValueChange = userViewModel::onCouponCodeChanged,
                label = { Text("Código de cupón") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                isError = uiState.error != null,
                supportingText = {
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "Se ha producido un error desconocido",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { userViewModel.addCouponToUser(userId, uiState.code?.trim() ?: "") },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) CircularProgressIndicator(Modifier.size(20.dp))
                else Text("Validar y guardar")
            }

            uiState.error?.let {
                Log.d("DEBUG COUPON","Error al añadir cupón: $it")
            }

            // 🔽 Scroll solo para los cupones
            if (uiState.currentCoupons.isEmpty()) {
                Text("No hay cupones", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("Cupones activos:", style = MaterialTheme.typography.titleSmall)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.currentCoupons) { coupon ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Código: ${coupon.code}", style = MaterialTheme.typography.bodyMedium)
                                Text("Descuento: ${coupon.discount}${if (coupon.isPercentage) "%" else "€"}")
                            }
                        }
                    }
                }
            }
        }
    }
}
