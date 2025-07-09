package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

            val fechaFormateada = parsedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "¿?"
            val fechaCaducidad = parsedDate?.plusDays(detail.valid_due?.toLong() ?: 0)
                ?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "¿?"

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                detail.image?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "${ApiClient.baseUrl}/product_images/${detail.image.orEmpty().trim()}",
                            contentDescription = "Imagen del producto",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp)
                                .padding(bottom = 16.dp)
                        )

                    }
                }

                Text(
                    detail.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                detail.description?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text("Caducidad de ${detail.valid_due ?: "?"} días", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(16.dp))

                Text("📅 Fecha de obtención: $fechaFormateada")
                Text("⏳ Fecha de caducidad: $fechaCaducidad")

                Spacer(modifier = Modifier.height(20.dp))

                Text("🧾 Servicios incluidos:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                detail.services.forEach {
                    Text("• ${it.name}", style = MaterialTheme.typography.bodySmall)
                }
            }

        }
    }
}

