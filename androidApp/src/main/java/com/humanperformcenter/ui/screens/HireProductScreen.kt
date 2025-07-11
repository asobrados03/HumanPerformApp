package com.humanperformcenter.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import kotlin.collections.emptyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HireProductScreen(
    serviceId: Int,
    navController: NavHostController,
    viewModel: ServiceProductViewModel,
    userId: Int
) {
    val productosMap by viewModel.serviceProducts.collectAsState()
    val productos = productosMap[serviceId] ?: emptyList()

    val productosContratados by viewModel.userProducts.collectAsState()
    val idsContratados = productosContratados.map { it.id }.toSet()

    var mostrarCuponSheet by remember { mutableStateOf(false) }
    var productoIdSeleccionado by remember { mutableStateOf<Int?>(null) }
    var cuponTexto by remember { mutableStateOf("") }

    var mostrarSeleccionPago by remember { mutableStateOf(false) }


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
                                productoIdSeleccionado = producto.id
                                mostrarCuponSheet = true
                            }
                        }
                    ) {
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
                                        style = MaterialTheme.typography.bodyMedium
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
    if (mostrarCuponSheet && productoIdSeleccionado != null) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarCuponSheet = false
                mostrarSeleccionPago = false
                productoIdSeleccionado = null
                cuponTexto = ""
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            val context = LocalContext.current

            Column(modifier = Modifier.padding(16.dp)) {

                if (!mostrarSeleccionPago) {
                    // Pantalla de cupón
                    Text("¿Tienes un cupón?", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = cuponTexto,
                        onValueChange = { cuponTexto = it },
                        label = { Text("Código de cupón") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.aplicarCupon(
                                codigo = cuponTexto.trim(),
                                userId = userId,
                                productId = productoIdSeleccionado!!
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "Cupón aplicado", Toast.LENGTH_SHORT).show()
                                    mostrarSeleccionPago = true
                                } else {
                                    Toast.makeText(context, "Cupón no válido para este producto", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aplicar cupón")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            Toast.makeText(context, "Sin cupón", Toast.LENGTH_SHORT).show()
                            mostrarSeleccionPago = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Omitir cupón y continuar")
                    }

                } else {
                    // Pantalla de selección de método de pago
                    Text("Selecciona método de pago", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.assignProductToUser(userId, productoIdSeleccionado!!)
                            Toast.makeText(context, "Contratado con tarjeta", Toast.LENGTH_SHORT).show()
                            mostrarCuponSheet = false
                            mostrarSeleccionPago = false
                            productoIdSeleccionado = null
                            cuponTexto = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pagar con tarjeta")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.assignProductToUser(userId, productoIdSeleccionado!!)
                            Toast.makeText(context, "Contratado en efectivo", Toast.LENGTH_SHORT).show()
                            mostrarCuponSheet = false
                            mostrarSeleccionPago = false
                            productoIdSeleccionado = null
                            cuponTexto = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pagar en efectivo")
                    }
                }
            }
        }
    }
}
