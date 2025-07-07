package com.humanperformcenter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import kotlin.collections.emptyList

@Composable
fun ContratarProductoScreen(
    serviceId: Int,
    navController: NavHostController,
    viewModel: ServiceProductViewModel,
    userId: Int
) {
    val productosMap by viewModel.serviceProducts.collectAsState()
    val productos = productosMap[serviceId] ?: emptyList()

    val productosContratados by viewModel.userProducts.collectAsState()
    val idsContratados = productosContratados.map { it.id }.toSet()

    LaunchedEffect(serviceId) {
        viewModel.loadServiceProducts(serviceId)
        viewModel.loadUserProducts(userId)
    }

    Scaffold(
        topBar = {
            LogoAppBar(showBackArrow = true) {
                navController.popBackStack()
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (productos.isEmpty()) {
                item {
                    Text("No hay productos disponibles para este servicio.")
                }
            } else {
                items(productos) { producto ->

                    val contratado = idsContratados.contains(producto.id)

                    AppCard(
                        onClick = {
                            if (!contratado) {
                                viewModel.assignProductToUser(userId, producto.id)
                            }
                        }
                    ){
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            producto.image?.let {
                                AsyncImage(
                                    model = "http://163.172.71.195:8085/product_images/$it",
                                    contentDescription = producto.name,
                                    modifier = Modifier
                                        .size(69.dp)
                                        .padding(end = 12.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(producto.name, fontWeight = FontWeight.Bold)

                                if (contratado) {
                                    Text(
                                        text = "Ya contratado",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.subtitle1
                                    )
                                }
                            }

                            Text(
                                text = "${producto.price?.toInt() ?: 0}€",
                                fontWeight = FontWeight.Bold,
                                color = if (contratado) Color.Gray else Color.Red
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
