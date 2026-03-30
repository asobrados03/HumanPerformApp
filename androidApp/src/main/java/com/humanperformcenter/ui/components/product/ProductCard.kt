package com.humanperformcenter.ui.components.product

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.network.API_BASE_URL
import com.humanperformcenter.ui.components.app.AppCard
import java.util.Locale

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    val imageUrl = product.image?.let {
        "${API_BASE_URL}/product_images/$it"
    }

    AppCard(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = product.name,
                modifier = Modifier.size(64.dp).padding(end = 12.dp)
            )
            Text(product.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                text = String.format(Locale("es", "ES"), "%.2f€", product.price ?: 0.0),
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
    }
}