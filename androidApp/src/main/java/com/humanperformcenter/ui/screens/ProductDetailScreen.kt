package com.humanperformcenter.ui.screens

import androidx.compose.foundation.border
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
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                detail.let { producto ->
                    val imageUrl = producto.image?.let { "https://apihuman.fransdata.com/api/product_images/$it" }

                    imageUrl?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = producto.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre del producto
                    Text(
                        text = producto.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Descripción
                    Text(
                        text = producto.description ?: "No hay descripción disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fechas y precios
                    producto.created_at?.let {
                        Text("Fecha de obtención: ${it.substring(0, 10)}", modifier = Modifier.fillMaxWidth())
                    }
                    producto.expiry_date?.let {
                        Text("Fecha de caducidad: ${it.substring(0, 10)}", modifier = Modifier.fillMaxWidth())
                    }
                    producto.amount?.let {
                        Text("Precio: %.2f€".format(it), modifier = Modifier.fillMaxWidth())
                    }
                    producto.discount?.let {
                        Text("Descuento: %.2f€".format(it), modifier = Modifier.fillMaxWidth())
                    }
                    producto.total_amount?.let {
                        Text("Total pagado: %.2f€".format(it), modifier = Modifier.fillMaxWidth())
                    }
                    producto.payment_method?.let {
                        Text("Pago con: ${it.replaceFirstChar { c -> c.uppercase() }}", modifier = Modifier.fillMaxWidth())
                    }
                    producto.payment_status?.let {
                        Text("Estado de pago: ${it.replaceFirstChar { c -> c.uppercase() }}", modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Servicios incluidos
                    Text(
                        text = "Servicios incluidos:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (producto.services.isNotEmpty()) {
                        producto.services.forEach { servicio ->
                            Text("• ${servicio.name}", modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        Text("No hay servicios asociados.", modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
