package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.app.navigation.ProductDetail
import com.humanperformcenter.shared.data.model.ServiceItem
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel

@Composable
fun MyProductsScreen(
    viewModel: ServiceProductViewModel,
    navController: NavHostController,
    userId: Int
) {
    val productos by viewModel.userProducts.collectAsState()
    println("MisProductosView: ${productos.size} productos contratados")
    val productosUnicos = productos.distinctBy { it.id }
    println("MisProductosView: ${productosUnicos.size} productos únicos")
    var productoSeleccionado by remember { mutableStateOf<ServiceItem?>(null) }
    var mostrarDialogoProducto by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (productos.isEmpty()) {
            item {
                Text("No tienes productos contratados.")
            }
        } else {
            items(productosUnicos) { producto ->
                val imageUrl = producto.image?.let { "http://163.172.71.195:8085/product_images/$it" }

                AppCard(onClick = {
                    productoSeleccionado = producto
                    mostrarDialogoProducto = true
                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        imageUrl?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = producto.name,
                                modifier = Modifier
                                    .size(69.dp)
                                    .padding(end = 12.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(producto.name, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "${producto.price?.toInt() ?: 0}€",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
    if (mostrarDialogoProducto && productoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoProducto = false
                productoSeleccionado = null
            },
            title = { Text("Producto: ${productoSeleccionado!!.name}") },
            text = { Text("¿Qué deseas hacer con este producto?") },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        // Acción de VER DETALLES
                        viewModel.productoSeleccionado = productoSeleccionado
                        navController.navigate(ProductDetail(productoSeleccionado!!.id))
                    }) {
                        Text("Ver detalles")
                    }

                    TextButton(onClick = {
                        // Acción de DESCONTRATAR
                        viewModel.unassignProductFromUser(productoSeleccionado!!.id, userId) // ← debes implementarlo
                        mostrarDialogoProducto = false
                        productoSeleccionado = null
                    }) {
                        Text("Darse de baja", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoProducto = false
                    productoSeleccionado = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}