package com.humanperformcenter.ui.components.hire_product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.components.app.AppCard
import java.util.Locale

@Composable
fun ProductList(
    modifier: Modifier = Modifier,
    availableProducts: List<Product>,
    idsContratados: Set<Int>,
    userCoupons: List<Coupon>,
    onProductClick: (Product) -> Unit,
    serviceProductViewModel: ServiceProductViewModel,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (availableProducts.isEmpty()) {
            item {
                Text("No hay productos disponibles para este servicio.")
            }
        } else {
            items(
                items = availableProducts,
                key = { it.id }
            ) { availableProduct ->
                val isHired = idsContratados.contains(availableProduct.id)

                // Calculamos el precio aquí para usarlo en la tarjeta
                val finalPrice = remember(availableProduct, userCoupons) {
                    serviceProductViewModel.calculateDiscountedPrice(
                        availableProduct.id,
                        availableProduct.price ?: 0.0,
                        userCoupons
                    )
                }

                AppCard(
                    onClick = { onProductClick(availableProduct) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Imagen
                        availableProduct.image?.let { imagePath ->
                            AsyncImage(
                                model = "${ApiClient.baseUrl}/product_images/$imagePath",
                                contentDescription = availableProduct.name,
                                modifier = Modifier
                                    .size(69.dp)
                                    .padding(end = 12.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(availableProduct.name, style = MaterialTheme.typography.titleMedium)
                            if (isHired) {
                                Text(
                                    "Ya contratado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = String.format(Locale("es", "ES"), "%.2f€", finalPrice),
                            fontWeight = if (isHired) FontWeight.Normal else FontWeight.Bold,
                            color = if (isHired) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}