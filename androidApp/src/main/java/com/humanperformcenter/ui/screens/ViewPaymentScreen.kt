package com.humanperformcenter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.viewmodel.UserViewModel

@Composable
fun ViewPaymentScreen(
    navController: NavHostController,
    viewModel: UserViewModel,
    userId: Int
) {
    val balance by viewModel.balance.collectAsState()
    val ewalletTransactions by viewModel.ewalletTransactions.collectAsState()

    var mostrarDetalles by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadBalance(userId)
        viewModel.loadEwalletTransactions(userId)
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
                    Text("💳 Saldo actual: $balance €", fontWeight = FontWeight.Bold)

                    Text(
                        text = if (mostrarDetalles) "🔼 Ocultar detalles" else "▶️ Ver detalles",
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .clickable { mostrarDetalles = !mostrarDetalles },
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (mostrarDetalles) {
                        LazyColumn {
                            items(ewalletTransactions) { tx ->
                                Column(Modifier.padding(vertical = 8.dp)) {
                                    val fechaFormateada = tx.date.substring(0, 10)
                                    Text("📅 $fechaFormateada")
                                    Text("💰 ${if (tx.amount > 0) "+" else ""}${tx.amount} €")
                                    Text("📝 ${tx.description}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


