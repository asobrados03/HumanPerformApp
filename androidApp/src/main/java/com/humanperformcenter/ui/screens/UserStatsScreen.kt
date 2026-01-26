package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.shared.presentation.viewmodel.UserStatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserStatsScreen(
    navController: NavHostController,
    statsViewModel: UserStatsViewModel,
    onRetry: () -> Unit
) {
    val uiState by statsViewModel.uiState.collectAsStateWithLifecycle()

    val entrenamientosMesPasado = uiState.entrenamientosMesPasado
    val entrenadorMasUsado= uiState.entrenadorMasUsado
    val reservasPendientes = uiState.reservasPendientes
    val isLoading = uiState.isLoading
    val error = uiState.error


    println("UserStatsScreen: Entrenamientos del mes pasado: $entrenamientosMesPasado")
    println("UserStatsScreen: Entrenador más usado: $entrenadorMasUsado")

    Scaffold(
        topBar = {
            LogoAppBar(showBackArrow = false, onBackNavClicked = { navController.popBackStack() })
        },
        bottomBar = { NavigationBar(navController = navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Sección de estadísticas
                when {
                    isLoading -> {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    error != null -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Error, null, tint = Color.Red)
                            Text("Error: $error", color = Color.Red)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = onRetry) {
                                Icon(Icons.Default.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Reintentar")
                            }
                        }
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📊 Tus estadísticas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                textAlign = TextAlign.Start
                            )

                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "📅 Entrenamientos del mes pasado",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("$entrenamientosMesPasado sesiones")
                                }
                            }

                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("🏋️ Entrenador más usado", fontWeight = FontWeight.Bold)
                                    Text(entrenadorMasUsado ?: "No hay datos disponibles")
                                }
                            }

                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("⏳ Reservas pendientes", fontWeight = FontWeight.Bold)
                                    Text("$reservasPendientes sesiones pendientes")
                                }
                            }

                        }
                    }
                }
            }

        }
    }
}
