package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.humanperformcenter.shared.presentation.ui.UserStatsState
import com.humanperformcenter.shared.presentation.viewmodel.UserStatsViewModel
import com.humanperformcenter.ui.components.app.AppCard
import com.humanperformcenter.ui.components.app.LogoAppBar
import com.humanperformcenter.ui.components.app.NavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserStatsScreen(
    navController: NavHostController,
    statsViewModel: UserStatsViewModel,
    onRetry: () -> Unit
) {
    // Obtenemos el estado reactivo
    val uiState by statsViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = false,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        bottomBar = { NavigationBar(navController = navController) }
    ) { paddingValues ->

        // Usamos Box para centrar el loading o el error fácilmente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when (val state = uiState) {
                is UserStatsState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is UserStatsState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UserStatsState.Success -> {
                    // Si es Success, mostramos la lista con los datos
                    StatsContent(state = state)
                }
            }
        }
    }
}

@Composable
private fun StatsContent(state: UserStatsState.Success) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "📊 Tus estadísticas",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            val workouts = state.stats.lastMonthWorkouts
            StatCard(
                title = "📅 Entrenamientos del mes pasado",
                value = if (workouts == 1) "1 sesión" else "$workouts sesiones"
            )
        }

        item {
            StatCard(
                title = "🏋️ Entrenador más usado",
                value = state.stats.mostFrequentTrainer ?: "No hay datos disponibles"
            )
        }

        item {
            val bookings = state.stats.pendingBookings
            StatCard(
                title = "⏳ Reservas pendientes",
                value = if (bookings == 1) "1 sesión pendiente" else "$bookings sesiones pendientes"
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(value)
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text("Error: $message", color = Color.Red, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}