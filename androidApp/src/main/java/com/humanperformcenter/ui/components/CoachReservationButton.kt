package com.humanperformcenter.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.humanperformcenter.shared.data.model.ReservaRequest
import com.humanperformcenter.ui.viewmodel.SesionesDiaViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/*@Composable
fun CoachReservationButton(
    sesionesDiaViewModel: SesionesDiaViewModel,
    coachName: String = "",
    serviceId: Int,
    centerId: Int,
    selectedDate: LocalDate,
    hour: String, // formato "HH:mm"
    customerId: Int,
    onSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val coachesForHour by sesionesDiaViewModel.coachesForHour.collectAsState()
    val context = LocalContext.current

    Button(
        onClick = {
            scope.launch {
                try {
                    val preferredCoachId = sesionesDiaViewModel.getPreferredCoachId(customerId, serviceId)

                    // Filtra los disponibles (booked < capacity)
                    val disponibles = coachesForHour.filter { it.booked < it.capacity }

                    // Busca el preferred si está disponible
                    val coachSeleccionado = disponibles.find { it.coach_id == preferredCoachId }
                        ?: disponibles.firstOrNull()

                    if (coachSeleccionado == null) {
                        println("❌ No hay entrenadores disponibles a esta hora")
                        return@launch
                    }

                    // Arma la fecha completa en ISO
                    val startDateTime = LocalDateTime.of(
                        selectedDate.toJavaLocalDate(),
                        LocalTime.parse(hour, DateTimeFormatter.ofPattern("HH:mm"))
                    ).toString()

                    // Reserva
                    sesionesDiaViewModel.realizarReserva(customerId,coachSeleccionado.coach_id, serviceId, 1, startDateTime, hour)
                    println("✅ Reserva realizada con éxito")
                    onSuccess()
                } catch (e: Exception) {
                    println("❌ Error al realizar la reserva: ${e.message}")
                }
            }
        }
    ) {
        Text("Reservar automáticamente")
    }
}
*/