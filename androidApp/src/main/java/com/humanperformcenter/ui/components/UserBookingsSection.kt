package com.humanperformcenter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.presentation.ui.UserProductsUiState
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import com.humanperformcenter.ui.util.createICSFile
import com.humanperformcenter.ui.util.shareICS
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

@Composable
fun UserBookingsSection(
    userViewModel: UserViewModel,
    serviceProductViewModel: ServiceProductViewModel,
    userBookings: List<UserBooking>,
    userId: Int?
) {
    val productosState by serviceProductViewModel.userProductsState.collectAsStateWithLifecycle()
    var servicioFiltro by remember { mutableStateOf<Product?>(null) }

    val today = remember { LocalDate.now() }

    val listaParaDropdown = remember(productosState) {
        if (productosState is UserProductsUiState.Success) {
            (productosState as UserProductsUiState.Success).products
        } else {
            emptyList()
        }
    }

    val reservasFiltradas = remember(userBookings, servicioFiltro, today) {
        userBookings.filter { booking ->
            // 1. Filtro de fecha (se queda igual)
            val fecha = try {
                LocalDate.parse(booking.date.take(10))
            } catch (_: DateTimeParseException) { null }
            val pasaFecha = fecha != null && (fecha.isEqual(today) || fecha.isAfter(today))

            // 2. FILTRO DIRECTO POR PRODUCT_ID
            // Comparamos el ID del dropdown (11) con el product_id de la reserva (11)
            val pasaServicio = servicioFiltro == null || booking.productId == servicioFiltro!!.id

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
            serviciosPermitidos = listaParaDropdown,
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
            LazyColumn(modifier = Modifier.heightIn(max = 1000.dp)) {
                items(
                    items = reservasFiltradas,
                    key = { booking -> booking.id }
                ) { booking ->
                    val dateFormateada = booking.date.take(10)
                    val horaFormateada = booking.hour.take(5)
                    val isExpanded = menuExpandedMap[booking.id] ?: false
                    val colorFondo = coloresPorServicio[booking.serviceId] ?: Color(0xFF6B426C)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(colorFondo, RoundedCornerShape(12.dp))
                            .border(3.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        booking.coachProfilePic?.let { url ->
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
                            Text("🏢 Servicio: ${booking.service}")
                            Text("✨ Producto: ${booking.product}")
                            Text("👟 Profesional: ${booking.coachName}")
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
                                        try {
                                            // CAMBIO 3: Creación del Instant con java.time
                                            val startDateTime = LocalDateTime.parse("${dateFormateada}T${horaFormateada}:00")

                                            // Combinamos fecha/hora con la zona horaria del sistema para obtener el instante
                                            val instant = startDateTime
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant()

                                            // Asegúrate que createICSFile acepte java.time.Instant o long (toEpochMilli())
                                            val icsContent = createICSFile(
                                                eventTitle = booking.service,
                                                startDateTime = instant
                                            )
                                            shareICS(context, icsContent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            // Opcional: Mostrar Toast de error
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cancelar reserva") },
                                    onClick = {
                                        menuExpandedMap[booking.id] = false
                                        userId?.let {
                                            userViewModel.cancelUserBooking(booking.id)
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