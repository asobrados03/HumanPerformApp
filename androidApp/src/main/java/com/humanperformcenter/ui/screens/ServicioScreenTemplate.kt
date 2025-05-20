package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.LogoAppBar
import com.humanperformcenter.NavigationBar
import com.humanperformcenter.shared.data.model.ServicioItem
import com.humanperformcenter.ui.components.AppCard

@Composable
fun ServicioScreenTemplate(
    productos: List<ServicioItem>,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            LogoAppBar(showBackArrow = true) {
                navController.popBackStack()
            }
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
            items(productos) { producto ->
                AppCard(onClick = {
                    // Aquí puedes guardar la compra, si aplica
                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = producto.nombre,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text("${producto.precio}€", fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                }
            }
        }
    }
}

