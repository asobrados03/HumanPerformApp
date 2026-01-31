package com.humanperformcenter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.humanperformcenter.shared.data.model.product_service.Product

@Composable
fun ServiceFilterDropdown(
    serviciosPermitidos: List<Product>,
    servicioFiltro: Product?,
    onServicioSeleccionado: (Product?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = servicioFiltro?.name ?: "Todas",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
                .clickable { expanded = true }
                .onGloballyPositioned { buttonWidth = it.size.width }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { buttonWidth.toDp() })
                .background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = {
                    onServicioSeleccionado(null)
                    expanded = false
                }
            )
            serviciosPermitidos.forEach { servicio ->
                DropdownMenuItem(
                    text = { Text(servicio.name) },
                    onClick = {
                        onServicioSeleccionado(servicio)
                        expanded = false
                    }
                )
            }
        }
    }
}