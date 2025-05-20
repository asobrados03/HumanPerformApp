package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.shared.data.model.PackEntrenamiento
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.viewModels.SessionViewModel

@Composable
fun EntrenamientoScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val packs = listOf(
        PackEntrenamiento("1 ENTRENAMIENTO/SEMANA", 1, 70),
        PackEntrenamiento("2 ENTRENAMIENTOS/SEMANA", 2, 120),
        PackEntrenamiento("3 ENTRENAMIENTOS/SEMANA", 3, 150),
        PackEntrenamiento("4 ENTRENAMIENTOS/SEMANA", 4, 180),
        PackEntrenamiento("5 ENTRENAMIENTOS/SEMANA", 5, 210)
    )

    Scaffold(
        topBar = {
            LogoAppBar(showBackArrow = true) {
                navController.popBackStack()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            items(packs) { pack ->
                AppCard(onClick = {
                    // Guardamos en ViewModel la cantidad de entrenamientos comprados
                    sessionViewModel.comprarEntrenamiento(pack.sesionesPorSemana)

                    // (Opcional) Mostrar mensaje, navegar o feedback
                    navController.popBackStack()
                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = pack.nombre,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${pack.precio}€",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}