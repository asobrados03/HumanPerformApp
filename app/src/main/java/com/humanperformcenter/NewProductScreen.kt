@file:OptIn(ExperimentalMaterial3Api::class)
package com.humanperformcenter

import androidx.compose.material3.ExperimentalMaterial3Api
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.viewModels.SessionViewModel
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.ui.text.font.FontWeight
import com.humanperformcenter.ui.components.AppCard

@RequiresApi(Build.VERSION_CODES.O)
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
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 8.dp)
                        )
                    }
                },
                colors = topAppBarColors(
                    containerColor = Color(0xFFB71C1C), // Rojo fuerte, ajustable
                    titleContentColor = Color.White
                ),
                navigationIcon = {},
                actions = {}
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
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Ir",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}