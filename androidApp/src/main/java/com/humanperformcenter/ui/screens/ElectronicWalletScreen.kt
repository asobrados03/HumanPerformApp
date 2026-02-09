package com.humanperformcenter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.presentation.ui.EwalletUiState
import com.humanperformcenter.ui.components.app.AppCard
import com.humanperformcenter.ui.components.app.LogoAppBar
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel

@Composable
fun ElectronicWalletScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    userId: Int
) {
    val balance by userViewModel.balance.collectAsStateWithLifecycle()
    val uiState by userViewModel.ewalletTransactions.collectAsStateWithLifecycle()

    var mostrarDetalles by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        userViewModel.loadBalance(userId)
        userViewModel.loadEwalletTransactions(userId)
    }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    // Sección de Saldo
                    Text(
                        text = "💳 Saldo actual: $balance €",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Botón para expandir detalles
                    Text(
                        text = if (mostrarDetalles) "🔼 Ocultar detalles" else "▶️ Ver detalles",
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clickable { mostrarDetalles = !mostrarDetalles },
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Sección de Transacciones (Solo si mostrarDetalles es true)
                    if (mostrarDetalles) {
                        Spacer(modifier = Modifier.height(16.dp))

                        when (val state = uiState) {
                            is EwalletUiState.Loading -> {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                                }
                            }
                            is EwalletUiState.Error -> {
                                Text(
                                    text = "❌ ${state.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            is EwalletUiState.Success -> {
                                if (state.transactions.isEmpty()) {
                                    Text("No hay transacciones disponibles.")
                                } else {
                                    // Usamos LazyColumn para eficiencia con altura limitada
                                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                                        items(state.transactions) { tx ->
                                            TransactionRow(tx)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: EwalletTransaction) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        val fechaFormateada = tx.date.take(10)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("📅 $fechaFormateada", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "${if (tx.amount > 0) "+" else ""}${tx.amount} €",
                fontWeight = FontWeight.Bold,
                color = if (tx.amount > 0) Color(0xFF4CAF50) else Color(0xFFE57373)
            )
        }
        Text("📝 ${tx.description}", style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}