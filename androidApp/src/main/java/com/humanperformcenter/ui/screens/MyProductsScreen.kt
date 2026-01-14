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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.app.navigation.ProductDetail
import com.humanperformcenter.shared.data.model.ServiceItem
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.viewmodel.DaySessionViewModel
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModel

@Composable
fun MyProductsScreen(
    viewModel: ServiceProductViewModel,
    navController: NavHostController,
    userViewModel: UserViewModel,
    daySessionViewModel: DaySessionViewModel,
    userId: Int
) {
    val productos by viewModel.userProducts.collectAsStateWithLifecycle()
    val productosUnicos = productos.distinctBy { it.id }
    var productoSeleccionado by remember { mutableStateOf<ServiceItem?>(null) }
    var mostrarDialogoProducto by remember { mutableStateOf(false) }
    val userBookings by userViewModel.userBookings.collectAsStateWithLifecycle()
    var mostrarConfirmacionBaja by remember { mutableStateOf(false) }

    LaunchedEffect(userBookings) {
        if (userBookings.isNotEmpty()) {
            daySessionViewModel.cargarFormularioSiProcede(userBookings)
        }
    }

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
            items(
                items = productosUnicos,
                key = { it.id }
            ) { product ->
                val imageUrl = product.image?.let { "${ApiClient.baseUrl}/product_images/$it" }

                AppCard(onClick = {
                    productoSeleccionado = product
                    mostrarDialogoProducto = true
                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        imageUrl?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = product.name,
                                modifier = Modifier
                                    .size(69.dp)
                                    .padding(end = 12.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "${product.price?.toInt() ?: 0}€",
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
                        viewModel.productoSeleccionado = productoSeleccionado
                        navController.navigate(ProductDetail(productoSeleccionado!!.id))
                    }) {
                        Text("Ver detalles")
                    }

                    TextButton(
                        onClick = { mostrarConfirmacionBaja = true }
                    ) {
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
    if (mostrarConfirmacionBaja) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionBaja = false },
            title = { Text("Confirmar baja") },
            text = { Text("¿Estás seguro de que quieres darte de baja de este producto?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.unassignProductFromUser(productoSeleccionado!!.id, userId)
                    mostrarDialogoProducto = false
                    productoSeleccionado = null
                    mostrarConfirmacionBaja = false
                }) {
                    Text("Sí, darse de baja", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarConfirmacionBaja = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}