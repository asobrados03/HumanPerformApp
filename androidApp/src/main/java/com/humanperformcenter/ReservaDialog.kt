@file:JvmName("ReservaDialog")

package com.humanperformcenter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate

@Composable
fun MostrarDialogoReserva(
    fecha: LocalDate,
    disponibilidad: Map<Int, List<String>>,
    onClose: () -> Unit,
    onReservar: (hora: String, centroId: Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {},
        title = {
            Text(
                text = "Selecciona una hora para el ${fecha.dayOfMonth}/${fecha.monthNumber}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                disponibilidad.forEach { (centroId, horas) ->
                    Text(
                        text = obtenerNombreCentro(centroId),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Column {
                        horas.forEach { hora ->
                            Text(
                                text = hora,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onReservar(hora, centroId)
                                        onClose()
                                    }
                                    .padding(vertical = 6.dp),
                                color = obtenerColorPorCentro(centroId),
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    )
}

private fun obtenerNombreCentro(centroId: Int): String =
    when (centroId) {
        1 -> "El Cerro"
        2 -> "El Sotillo"
        else -> "Centro $centroId"
    }

private fun obtenerColorPorCentro(centroId: Int): Color =
    when (centroId) {
        1 -> Color(0xFF81D4FA)
        else -> Color(0xFFA5D6A7)
    }