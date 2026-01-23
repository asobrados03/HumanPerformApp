package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.viewmodel.state.ProductDetailState

@Composable
fun ProductDetailScreen(
    productId: Int,
    userId: Int,
    viewModel: ServiceProductViewModel,
    navController: NavHostController
) {
    val detailState = viewModel.productDetailsState.collectAsStateWithLifecycle()

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
        when (val state = detailState.value) {
            is ProductDetailState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is ProductDetailState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.fetchProductDetails(userId, productId)
                        }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is ProductDetailState.Success -> {
                val producto = state.product
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val imageUrl = producto.image?.let { "${ApiClient.baseUrl}/product_images/$it" }

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

                    Text(
                        text = producto.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = producto.description ?: "No hay descripción disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Fecha de obtención: ${producto.created_at.take(10)}", modifier = Modifier.fillMaxWidth())
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