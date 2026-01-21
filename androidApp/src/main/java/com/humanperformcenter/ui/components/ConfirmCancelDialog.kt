package com.humanperformcenter.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ConfirmCancelDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar baja") },
        text = { Text("¿Estás seguro de que quieres darte de baja?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Sí, dar de baja", color = Color.Red) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}