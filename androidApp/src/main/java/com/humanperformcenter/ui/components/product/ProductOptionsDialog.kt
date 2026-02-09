package com.humanperformcenter.ui.components.product

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ProductOptionsDialog(
    productName: String,
    onViewDetails: () -> Unit,
    onCancelRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Producto: $productName") },
        text = { Text("¿Qué deseas hacer con este producto?") },
        confirmButton = {
            Column {
                TextButton(onClick = onViewDetails) { Text("Ver detalles") }
                TextButton(onClick = onCancelRequest) { Text("Darse de baja", color = Color.Red) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
