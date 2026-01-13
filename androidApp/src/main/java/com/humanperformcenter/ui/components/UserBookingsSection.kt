package com.humanperformcenter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.UserBooking
import com.humanperformcenter.ui.util.createICSFile
import com.humanperformcenter.ui.util.shareICS
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun UserBookingsSection(
    userViewModel: UserViewModel,
    sessionViewModel: SessionViewModel,
    userBookings: List<UserBooking>, // Asegúrate de que tengas el modelo Booking importado
    userId: Int?
) {
    val serviciosPermitidos by sessionViewModel.allowedServices.collectAsState()
    var servicioFiltro by remember { mutableStateOf<ServiceAvailable?>(null) }

    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val reservasFiltradas = remember(userBookings, servicioFiltro, today) {
        userBookings.filter { booking ->
            val fecha = try {
                LocalDate.parse(booking.date.take(10))
            } catch (_: Exception) { null }

            val pasaFecha = fecha != null && fecha >= today
            val pasaServicio = servicioFiltro == null || booking.service_id == servicioFiltro!!.id
            pasaFecha && pasaServicio
        }
    }

    val coloresPorServicio = mapOf(
        1 to Color(0xFF97DE98),
        2 to Color(0xFF84B8E3),
        3 to Color(0xFFECDB6C),
        4 to Color(0xFFDE8B75)
    )

    val context = LocalContext.current
    val menuExpandedMap = remember { mutableStateMapOf<Int, Boolean>() }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Tus sesiones reservadas",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        ServiceFilterDropdown(
            serviciosPermitidos = serviciosPermitidos,
            servicioFiltro = servicioFiltro,
            onServicioSeleccionado = { servicioFiltro = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (reservasFiltradas.isEmpty()) {
            Text(
                text = "No tienes sesiones reservadas.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyColumn {
                items(
                    items = reservasFiltradas,
                    key = {booking -> booking.id}
                ) { booking ->
                    val dateFormateada = booking.date.take(10)
                    val horaFormateada = booking.hour.take(5)
                    val isExpanded = menuExpandedMap[booking.id] ?: false
                    val colorFondo = coloresPorServicio[booking.service_id] ?: Color(0xFF6B426C)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(colorFondo, RoundedCornerShape(12.dp))
                            .border(3.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        booking.coach_profile_pic?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Foto del entrenador",
                                modifier = Modifier
                                    .size(86.dp)
                                    .padding(end = 12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("📅 $dateFormateada - 🕒 $horaFormateada")
                            Text("🧘 Servicio: ${booking.service}")
                            Text("🏋️ Entrenador: ${booking.coach_name}")
                        }

                        Box {
                            IconButton(onClick = { menuExpandedMap[booking.id] = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                            }

                            DropdownMenu(
                                expanded = isExpanded,
                                onDismissRequest = { menuExpandedMap[booking.id] = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Descargar evento") },
                                    onClick = {
                                        menuExpandedMap[booking.id] = false
                                        val startDateTimeStr = LocalDateTime.parse("${dateFormateada}T${horaFormateada}:00")
                                        val instant = startDateTimeStr.toInstant(TimeZone.currentSystemDefault())
                                        val icsContent = createICSFile(
                                            eventTitle = booking.service,
                                            startDateTime = instant
                                        )
                                        shareICS(context, icsContent)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cancelar reserva") },
                                    onClick = {
                                        menuExpandedMap[booking.id] = false
                                        userId?.let {
                                            userViewModel.cancelUserBooking(booking.id, context)
                                            userViewModel.fetchUserBookings(it)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}