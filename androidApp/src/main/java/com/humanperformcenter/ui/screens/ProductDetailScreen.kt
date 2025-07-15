package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.ui.components.LogoAppBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel

@Composable
fun ProductDetailScreen(
    productId: Int,
    userId: Int,
    viewModel: ServiceProductViewModel,
    navController: NavHostController
) {
    val detailState = viewModel.productDetails.collectAsState()
    val detail = detailState.value

    LaunchedEffect(productId, userId) {
        viewModel.fetchProductDetails(userId, productId)
    }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        }
    ) { padding ->
        if (detail == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val parsedDate = try {
                LocalDate.parse(detail.created_at.substring(0, 10))
            } catch (e: Exception) {
                null
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                detail?.let { producto ->
                    // Imagen
                    AsyncImage(
                        model = producto.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre del producto
                    Text(
                        text = producto.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Descripción
                    Text(
                        text = producto.description ?: "No hay descripción disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fecha de obtención
                    producto.created_at?.let {
                        val fechaFormateada = formatDate(it)
                        Text("Fecha de obtención: $fechaFormateada")
                    }

                    // Fecha de caducidad
                    producto.expiry_date?.let {
                        val fechaCaducidad = formatDate(it)
                        Text("Fecha de caducidad: $fechaCaducidad")
                    }

                    // Precio original
                    producto.amount?.let {
                        Text("Precio: %.2f€".format(it))
                    }

                    // Descuento
                    producto.discount?.let {
                        Text("Descuento: %.2f€".format(it))
                    }

                    // Total pagado
                    producto.total_amount?.let {
                        Text("Total pagado: %.2f€".format(it))
                    }

                    // Método de pago
                    producto.payment_method?.let {
                        Text("Pago con: ${it.replaceFirstChar { c -> c.uppercase() }}")
                    }

                    // Estado del pago
                    producto.payment_status?.let {
                        Text("Estado de pago: ${it.replaceFirstChar { c -> c.uppercase() }}")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Servicios asociados
                    Text(
                        text = "Servicios incluidos:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (producto.services.isNotEmpty()) {
                        producto.services.forEach { servicio ->
                            Text("• ${servicio.name}")
                        }
                    } else {
                        Text("No hay servicios asociados.")
                    }
                }
            }

        }
    }
}
