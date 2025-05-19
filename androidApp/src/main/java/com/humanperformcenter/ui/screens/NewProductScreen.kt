package com.humanperformcenter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.LogoAppBar
import com.humanperformcenter.NavigationBar
import com.humanperformcenter.R
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.viewModels.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProductScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    onPlaySound: (Int) -> Unit
) {
    val services = listOf(
        "NUTRICIÓN" to R.drawable.nutricion,
        "ENTRENAMIENTO" to R.drawable.entrenamiento,
        "FISIOTERAPIA" to R.drawable.fisioterapia,
        "PILATES" to R.drawable.pilates,
        "PRESOTERAPIA" to R.drawable.presoterapia,
        "ENTRENAMIENTO OPOSITORES" to R.drawable.opositores,
        "SERVICIO DE TAQUILLA PERSONAL" to R.drawable.taquilla,
        "ALTER G Cinta antigravedad" to R.drawable.alterg
    )

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = false,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        bottomBar = { NavigationBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            items(services) { service ->
                val (name, iconRes) = service
                AppCard(
                    onClick = {
                        when (name) {
                            "NUTRICIÓN" -> navController.navigate("nutricion_screen")
                            "ENTRENAMIENTO" -> navController.navigate("entrenamiento_screen")
                            "FISIOTERAPIA" -> navController.navigate("fisioterapia_screen")
                            "PILATES" -> navController.navigate("pilates_screen")
                            "PRESOTERAPIA" -> navController.navigate("presoterapia_screen")
                            "ENTRENAMIENTO OPOSITORES" -> navController.navigate("opositores_screen")
                            "SERVICIO DE TAQUILLA PERSONAL" -> navController.navigate("taquilla_screen")
                            "ALTER G Cinta antigravedad" -> navController.navigate("alterg_screen")
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = name,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardDoubleArrowRight,
                            contentDescription = "Ir",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}