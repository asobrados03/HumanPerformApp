package com.humanperformcenter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.humanperformcenter.data.Session
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SessionItem(
    session: Session,
    onClick: () -> Unit,
    // The following are needed for delete dialog logic:
    showDialog: Boolean = false,
    setShowDialog: ((Boolean) -> Unit)? = null,
    sessionToDelete: Session? = null,
    setSessionToDelete: ((Session?) -> Unit)? = null
) {
    // Color según el tipo de sesión
    val colorTipo = when (session.service.lowercase()) {
        "entrenamiento" -> Color(0xFF4CAF50) // Verde
        "fisioterapia" -> Color(0xFF2196F3)  // Azul
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        border = BorderStroke(1.dp, Color(0xFFBDBDBD)),
        shape = RoundedCornerShape(16.dp)
    ) {
        // --- BEGIN NEW LAYOUT ---
        // Profesional and imageName logic must be outside composable lambdas
        val profesional = when (session.service.lowercase()) {
            "entrenamiento" -> if (session.product == "Centro 1") listOf("Pablo Sanz", "Sergio Sanz").random() else listOf("Juan Sanz", "Jorge Mínguez").random()
            "fisioterapia" -> if (session.product == "Centro 1") "Idaira Prieto" else "Isabel Prieto"
            "nutrición" -> "Susana Muñoz"
            else -> "Desconocido"
        }
        val imageName = when (profesional) {
            "Pablo Sanz" -> "ent_pablo"
            "Sergio Sanz" -> "ent_sergio"
            "Juan Sanz" -> "ent_juan"
            "Jorge Mínguez" -> "ent_jorge"
            "Idaira Prieto" -> "ent_idaira"
            "Isabel Prieto" -> "ent_isabel"
            "Susana Muñoz" -> "ent_susana"
            else -> null
        }
        var expanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First column: text details
            Column(
                modifier = Modifier
                    .weight(0.6f)
            ) {
                Text(
                    text = session.service.uppercase(),
                    color = colorTipo,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (session.product.isNotBlank()) {
                    Text(
                        text = if (session.product == "Centro 1") "Centro 1: El cerro" else "Centro 2: El sotillo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val dateTime = Instant.fromEpochMilliseconds(session.date).toLocalDateTime(TimeZone.currentSystemDefault())
                val day = dateTime.date.toString()
                val time = dateTime.time.toString()
                Text("Día: $day", style = MaterialTheme.typography.bodySmall)
                Text("Hora: $time", style = MaterialTheme.typography.bodySmall)
                Text("Entrenador: $profesional", style = MaterialTheme.typography.bodySmall)
            }

            // Second column: image (if available)
            imageName?.let { name ->
                val context = LocalContext.current
                val resId = remember(name) {
                    context.resources.getIdentifier(name.lowercase(), "drawable", context.packageName)
                }
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Foto de $profesional",
                        modifier = Modifier
                            .weight(0.3f)
                            .size(96.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(0.3f))
                }
            } ?: Spacer(modifier = Modifier.weight(0.3f))

            // Third column: three-dots menu
            Box(
                modifier = Modifier.weight(0.1f),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones"
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cambiar hora") },
                        onClick = {
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            expanded = false
                            setSessionToDelete?.invoke(session)
                            setShowDialog?.invoke(true)
                        }
                    )
                }
            }
        }
        // --- END NEW LAYOUT ---
    }
}