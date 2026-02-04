package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.components.LogoAppBar

@Composable
fun ProductDetailFullScreen(
    product: Product,
    userCoupons: List<Coupon>,
    isAlreadyHired: Boolean,
    serviceProductViewModel: ServiceProductViewModel,
    onClose: () -> Unit,
    onBuyClick: () -> Unit,
) {
    val precioFinal = remember(product, userCoupons) {
        serviceProductViewModel.calcularPrecioConDescuento(
            product.id,
            product.price ?: 0.0,
            userCoupons
        )
    }

    Scaffold(
        topBar = {
            LogoAppBar(showBackArrow = true, onBackNavClicked = onClose)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            product.image?.let { imagePath ->
                AsyncImage(
                    model = "${ApiClient.baseUrl}/product_images/$imagePath",
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Text(
                text = product.description ?: "No hay descripción disponible.",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                product.session?.let { sesiones ->
                    Text("Sesiones: $sesiones", style = MaterialTheme.typography.bodyMedium)
                }
                product.typeOfProduct?.let { tipo ->
                    Text("Tipo: ${tipo.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(
                text = "Precio: ${precioFinal.toInt()}€",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )

            if (isAlreadyHired) {
                Text(
                    text = "Ya has contratado este producto.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onBuyClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Comprar")
            }
        }
    }
}
