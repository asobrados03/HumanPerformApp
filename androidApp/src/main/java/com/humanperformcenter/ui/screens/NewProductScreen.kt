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
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.R
import com.humanperformcenter.app.navigation.AlterG
import com.humanperformcenter.app.navigation.Entrenamiento
import com.humanperformcenter.app.navigation.Fisioterapia
import com.humanperformcenter.app.navigation.Nutricion
import com.humanperformcenter.app.navigation.Opositores
import com.humanperformcenter.app.navigation.Pilates
import com.humanperformcenter.app.navigation.Presoterapia
import com.humanperformcenter.app.navigation.Taquilla
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProductScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
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
                            "NUTRICIÓN" -> navController.navigate(Nutricion)
                            "ENTRENAMIENTO" -> navController.navigate(Entrenamiento)
                            "FISIOTERAPIA" -> navController.navigate(Fisioterapia)
                            "PILATES" -> navController.navigate(Pilates)
                            "PRESOTERAPIA" -> navController.navigate(Presoterapia)
                            "ENTRENAMIENTO OPOSITORES" -> navController.navigate(Opositores)
                            "SERVICIO DE TAQUILLA PERSONAL" -> navController.navigate(Taquilla)
                            "ALTER G Cinta antigravedad" -> navController.navigate(AlterG)
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