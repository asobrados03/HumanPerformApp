package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.model.ServiceItem
import com.humanperformcenter.ui.components.LogoAppBar

@Composable
fun ProductoDetalleScreen(
    producto: ServiceItem,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            LogoAppBar(showBackArrow = true) {
                navController.popBackStack()
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            producto.image?.let {
                AsyncImage(
                    model = "http://163.172.71.195:8085/product_images/$it",
                    contentDescription = producto.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(producto.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                producto.description ?: "Sin descripción.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${producto.price?.toInt() ?: 0} €",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Red
            )
        }
    }
}
