package com.humanperformcenter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.SharedPool
import com.humanperformcenter.shared.data.model.product_service.ServiceItem
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.presentation.ui.FetchUserBookingsState
import com.humanperformcenter.shared.presentation.ui.UserProductsUiState
import com.humanperformcenter.shared.presentation.viewmodel.DaySessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
// Importa ambas clases con alias para evitar confusión
import java.time.LocalDate as JavaLocalDate
import kotlinx.datetime.LocalDate as KotlinLocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private val SuccessColor = Color(0xFF4CAF50)
private val WarningColor = Color(0xFFFFA000)
private val ErrorColor = Color(0xFFD32F2F)
private val DisabledColor = Color(0xFFEEEEEE)
private val DisabledTextColor = Color.Gray

@Stable
private class ReservationFlowState(
    private val daySessionViewModel: DaySessionViewModel,
    private val userViewModel: UserViewModel,
    private val scope: CoroutineScope,
    private val userId: Int,
    val userBookings: List<UserBooking>,
    val sessions: List<DaySession>,
    val weeklyLimits: Map<Int, Int>,
    val unlimitedSessions: Map<Int, Int>,
    val sharedSessions: List<SharedPool>,
    val serviceToPrimary: Map<Int, Int>,
    private val zoneId: ZoneId = ZoneId.of("Europe/Madrid")
) {
    var dialog by mutableStateOf<Dialog>(Dialog.Hidden)
        private set

    var selectedService by mutableStateOf<ServiceItem?>(null)

    // Tiempo actual en java.time
    val now: LocalDateTime get() = LocalDateTime.now(zoneId)
    val today: JavaLocalDate = JavaLocalDate.now(zoneId)

    // Parseo seguro usando java.time
    private val reservedDates = userBookings.mapNotNull { it.date.toLocalDate() }.toSet()

    fun onDayClicked(date: JavaLocalDate) {
        if (reservedDates.contains(date)) {
            dialog = Dialog.ConfirmContinue(date)
        } else {
            startReservationFlow(date)
        }
    }

    fun startReservationFlow(date: JavaLocalDate) {
        resetStateForNewDate()
        daySessionViewModel.clearSessions()
        dialog = Dialog.Reservation(date)
    }

    fun onServiceSelected(service: ServiceItem, date: JavaLocalDate) { // 'date' es java.time
        selectedService = service
        daySessionViewModel.clearCoachesForHour()
        daySessionViewModel.fetchAvailableSessions(service.id, date.toKotlinLocalDate())
    }

    fun onHourClicked(hour: String, date: JavaLocalDate) {
        val serviceId = selectedService?.id ?: return

        // Validación de hora pasada
        val currentNow = now
        val slotDateTime = hour.toLocalDateTimeOnDate(date)

        if (slotDateTime.isBefore(currentNow)) return // isBefore es de java.time

        if (userBookings.any { it.date.toLocalDate() == date && it.hour.take(5) == hour.take(5) }) {
            dialog = Dialog.HourOccupied
            return
        }

        scope.launch(Dispatchers.IO) {
            daySessionViewModel.filtrarEntrenadoresPorHora(hour)
            val availableCoaches = daySessionViewModel.coachesForHour.value.filter { it.booked < it.capacity }
            if (availableCoaches.isEmpty()) {
                dialog = Dialog.NoCoachesAvailable
                return@launch
            }

            val canOnlyChange = daySessionViewModel.seSuperoLimiteReserva(
                serviceId,
                date.toKotlinLocalDate(),
                weeklyLimits,
                unlimitedSessions,
                sharedSessions,
                userBookings,
                serviceToPrimary
            )

            val preferredId = daySessionViewModel.getPreferredCoachId(userId, serviceId)
            val coach = availableCoaches.firstOrNull { it.coachId == preferredId } ?: availableCoaches.first()

            dialog = Dialog.Confirm(date, hour, coach, canOnlyChange)
        }
    }

    fun bookSession(date: JavaLocalDate, hour: String, coach: DaySession) {
        val serviceId = selectedService?.id ?: return
        scope.launch(Dispatchers.IO) {
            daySessionViewModel.realizarReserva(
                customerId = userId,
                coachId = coach.coachId,
                serviceId = serviceId,
                centerId = 1,
                selectedDate = date.toString(), // java.time usa ISO-8601 por defecto
                hour = hour
            )
            userViewModel.fetchUserBookings(userId)
            dismiss()
        }
    }

    fun changeSession(bookingToChange: UserBooking, newDate: JavaLocalDate, newHour: String, newCoach: DaySession) {
        val serviceId = selectedService?.id ?: return
        scope.launch(Dispatchers.IO) {
            daySessionViewModel.cambiarReservaSesion(
                customerId = userId,
                bookingId = bookingToChange.id,
                newCoachId = newCoach.coachId,
                newServiceId = serviceId,
                newStartDate = newDate.toString(),
                hour = newHour
            )
            userViewModel.fetchUserBookings(userId)
            dismiss()
        }
    }

    fun showChangeSelector(date: JavaLocalDate, hour: String, coach: DaySession) {
        dialog = Dialog.ChangeExisting(date, hour, coach)
    }

    fun dismiss() {
        dialog = Dialog.Hidden
        if (dialog !is Dialog.Reservation) {
            resetStateForNewDate()
        }
    }

    private fun resetStateForNewDate() {
        selectedService = null
        daySessionViewModel.clearCoachesForHour()
    }

    sealed class Dialog {
        object Hidden : Dialog()
        data class ConfirmContinue(val date: JavaLocalDate) : Dialog()
        data class Reservation(val date: JavaLocalDate) : Dialog()
        object HourOccupied : Dialog()
        object NoCoachesAvailable : Dialog()
        data class Confirm(val date: JavaLocalDate, val hour: String, val coach: DaySession, val canOnlyChange: Boolean) : Dialog()
        data class ChangeExisting(val date: JavaLocalDate, val hour: String, val coach: DaySession) : Dialog()
    }
}

// COMPONENTES UI

@Composable
fun reservationFlowDialogs(
    daySessionViewModel: DaySessionViewModel,
    serviceProductViewModel: ServiceProductViewModel,
    userViewModel: UserViewModel
): (JavaLocalDate) -> Unit {
    val user by userViewModel.userData.collectAsStateWithLifecycle()
    val userId = user?.id ?: return {}
    val scope = rememberCoroutineScope()

    val userBookings by userViewModel.userBookings.collectAsStateWithLifecycle()
    val bookingsList = (userBookings as? FetchUserBookingsState.Success)?.bookings ?: emptyList()

    val sessions by daySessionViewModel.sessions.collectAsStateWithLifecycle()
    val weeklyLimits by daySessionViewModel.weeklyLimits.collectAsStateWithLifecycle()
    val unlimitedSessions by daySessionViewModel.unlimitedSessions.collectAsStateWithLifecycle()
    val sharedSessions by daySessionViewModel.sharedSessions.collectAsStateWithLifecycle()
    val serviceToPrimary by daySessionViewModel.serviceToPrimary.collectAsStateWithLifecycle()

    val userProductsState by serviceProductViewModel.userProductsState.collectAsStateWithLifecycle()

    val listaProductosValidos = remember(userProductsState) {
        (userProductsState as? UserProductsUiState.Success)?.products ?: emptyList()
    }

    // Se mantiene igual, el cambio es interno en la clase
    val state = remember(userId) {
        ReservationFlowState(
            daySessionViewModel, userViewModel, scope, userId, bookingsList,
            sessions, weeklyLimits, unlimitedSessions, sharedSessions, serviceToPrimary
        )
    }

    when (val dialog = state.dialog) {
        is ReservationFlowState.Dialog.Reservation -> {
            ReservationDialog(
                state = state,
                date = dialog.date,
                allowedServices = listaProductosValidos
            )
        }
        is ReservationFlowState.Dialog.Confirm -> ConfirmDialog(state, dialog)
        is ReservationFlowState.Dialog.ConfirmContinue -> ConfirmContinueDialog(
            onConfirm = { state.startReservationFlow(dialog.date) },
            onDismiss = state::dismiss
        )
        is ReservationFlowState.Dialog.ChangeExisting -> ChangeExistingDialog(state, dialog)
        is ReservationFlowState.Dialog.HourOccupied -> InfoDialog("Reserva existente", "Ya tienes una reserva a esa hora.", state::dismiss)
        is ReservationFlowState.Dialog.NoCoachesAvailable -> InfoDialog("Sin disponibilidad", "No hay entrenadores disponibles.", state::dismiss)
        else -> Unit
    }

    return state::onDayClicked
}

@Composable
private fun ReservationDialog(state: ReservationFlowState, date: JavaLocalDate, allowedServices: List<ServiceItem>) {
    AlertDialog(
        onDismissRequest = state::dismiss,
        title = {
            ServiceSelector(allowedServices, state.selectedService) { state.onServiceSelected(it, date) }
        },
        text = {
            val hours = remember(state.sessions, state.selectedService) {
                state.sessions.filter { it.serviceId == state.selectedService?.id }.map { it.hour }.distinct().sorted()
            }
            HourSelector(state, hours, date)
        },
        confirmButton = { TextButton(onClick = state::dismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun HourSelector(state: ReservationFlowState, hours: List<String>, selectedDate: JavaLocalDate) {
    val currentNow = state.now
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        hours.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { hour ->
                    HourChip(hour, state.sessions, selectedDate, currentNow) { state.onHourClicked(hour, selectedDate) }
                }
            }
        }
    }
}

@Composable
private fun HourChip(hour: String, sessions: List<DaySession>, selectedDate: JavaLocalDate, now: LocalDateTime, onClick: () -> Unit) {
    val isPast = remember(selectedDate, hour, now) {
        hour.toLocalDateTimeOnDate(selectedDate).isBefore(now)
    }

    val hourSessions = remember(sessions, hour) { sessions.filter { it.hour == hour } }
    val booked = hourSessions.sumOf { it.booked }
    val capacity = hourSessions.sumOf { it.capacity }
    val ratio = if (capacity > 0) booked.toFloat() / capacity else 1f

    val color = when {
        isPast -> DisabledTextColor
        ratio < 0.5f -> SuccessColor
        ratio < 1f -> WarningColor
        else -> ErrorColor
    }

    Box(
        modifier = Modifier
            .padding(vertical = 4.dp).size(80.dp, 40.dp)
            .background(if (isPast) DisabledColor else Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .clickable(enabled = !isPast) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(hour.take(5), color = color)
    }
}

@Composable
private fun ChangeExistingDialog(state: ReservationFlowState, dialog: ReservationFlowState.Dialog.ChangeExisting) {
    val serviceId = state.selectedService?.id ?: return

    // Cálculo de semana usando java.time
    // Asumimos que la semana empieza el Lunes (ordinal 0 en array, pero 1 en java.time DayOfWeek)
    // dayOfWeek.ordinal en java.time devuelve 0 para lunes, 6 para domingo.
    val startOfWeek = dialog.date.minusDays(dialog.date.dayOfWeek.ordinal.toLong())
    val endOfWeek = startOfWeek.plusDays(6)

    val weeklyBookings = remember(state.userBookings, serviceId) {
        state.userBookings.filter {
            val d = it.date.toLocalDate()
            // Comparación de fechas en java.time (>= y <= funcionan por Comparable, pero isAfter/isBefore son más explícitos)
            it.service_id == serviceId && d != null && !d.isBefore(state.today) &&
                    (d.isEqual(startOfWeek) || d.isAfter(startOfWeek)) &&
                    (d.isEqual(endOfWeek) || d.isBefore(endOfWeek))
        }
    }

    AlertDialog(
        onDismissRequest = state::dismiss,
        title = { Text("Cambiar reserva") },
        text = {
            if (weeklyBookings.isEmpty()) Text("No hay reservas para cambiar.")
            else LazyColumn(Modifier.heightIn(max = 300.dp)) {
                items(weeklyBookings) { booking ->
                    Card(Modifier.fillMaxWidth().padding(4.dp).clickable {
                        state.changeSession(booking, dialog.date, dialog.hour, dialog.coach)
                    }) {
                        Text(booking.date.take(10) + " " + booking.hour.take(5), Modifier.padding(8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = state::dismiss) { Text("Cancelar") } }
    )
}

// ... Resto de diálogos (ServiceSelector, ConfirmDialog, etc) iguales ...
// Solo asegúrate de que ConfirmDialog use la nueva firma si usas LocalDate en otros sitios.

@Composable
private fun ServiceSelector(services: List<ServiceItem>, selected: ServiceItem?, onSelect: (ServiceItem) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(selected?.name ?: "Seleccionar servicio", Modifier.clickable { expanded = true }.background(ErrorColor, RoundedCornerShape(8.dp)).padding(16.dp), color = Color.White)
        DropdownMenu(expanded, { expanded = false }) {
            services.forEach { s -> DropdownMenuItem(text = { Text(s.name) }, onClick = { onSelect(s); expanded = false }) }
        }
    }
}

@Composable
private fun ConfirmDialog(state: ReservationFlowState, dialog: ReservationFlowState.Dialog.Confirm) {
    AlertDialog(
        onDismissRequest = state::dismiss,
        title = { Text("Confirmar") },
        text = { Text("Reserva con ${dialog.coach.coachName} a las ${dialog.hour.take(5)}") },
        confirmButton = {
            Row {
                if (!dialog.canOnlyChange) Button(onClick = { state.bookSession(dialog.date, dialog.hour, dialog.coach) }) { Text("Reservar") }
                TextButton(onClick = { state.showChangeSelector(dialog.date, dialog.hour, dialog.coach) }) { Text("Cambiar") }
            }
        },
        dismissButton = { TextButton(onClick = state::dismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ConfirmContinueDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Aviso") }, text = { Text("Ya tienes una reserva hoy. ¿Continuar?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Sí") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("No") } })
}

@Composable
private fun InfoDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(text) }, confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } })
}

// ------------------------------------------
// EXTENSIONES JAVA.TIME
// ------------------------------------------

// Parsea "YYYY-MM-DD" a java.time.LocalDate
private fun String.toLocalDate(): JavaLocalDate? = try {
    JavaLocalDate.parse(this.take(10))
} catch (_: Exception) {
    null
}

// Extensión para convertir de Java (Android) a Kotlin (KMM)
fun JavaLocalDate.toKotlinLocalDate(): KotlinLocalDate {
    return KotlinLocalDate(this.year, this.monthValue, this.dayOfMonth)
}

// Construye LocalDateTime desde "HH:mm" y una fecha dada
private fun String.toLocalDateTimeOnDate(date: JavaLocalDate): LocalDateTime {
    val h = this.take(2).toInt()
    val m = this.substring(3, 5).toInt()
    // java.time tiene el método atTime para combinar fecha y hora limpiamente
    return date.atTime(h, m)
}
