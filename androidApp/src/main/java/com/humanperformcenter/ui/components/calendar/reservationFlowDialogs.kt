package com.humanperformcenter.ui.components.calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.presentation.ui.DailySessionsUiState
import com.humanperformcenter.shared.presentation.ui.FetchUserBookingsState
import com.humanperformcenter.shared.presentation.ui.UserProductsUiState
import com.humanperformcenter.shared.presentation.viewmodel.DaySessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate as JavaLocalDate
import kotlinx.datetime.LocalDate as KotlinLocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

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
    private val zoneId: ZoneId = ZoneId.of("Europe/Madrid")
) {
    var dialog by mutableStateOf<Dialog>(Dialog.Hidden)
        private set

    var selectedProduct by mutableStateOf<Product?>(null)

    val now: LocalDateTime get() = LocalDateTime.now(zoneId)
    val today: JavaLocalDate = JavaLocalDate.now(zoneId)

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

    fun onServiceSelected(product: Product, date: JavaLocalDate) {
        selectedProduct = product
        daySessionViewModel.clearCoachesForHour()
        daySessionViewModel.fetchAvailableSessions(product.id, date.toKotlinLocalDate())
    }

    fun onHourClicked(hour: String, date: JavaLocalDate) {
        // Validación de hora pasada
        val currentNow = now
        val slotDateTime = hour.toLocalDateTimeOnDate(date)

        if (slotDateTime.isBefore(currentNow)) return // isBefore es de java.time

        if (userBookings.any { it.date.toLocalDate() == date && it.hour.take(5) == hour.take(5) }) {
            dialog = Dialog.HourOccupied
            return
        }

        scope.launch(Dispatchers.IO) {
            daySessionViewModel.filterCoachesByHour(hour)
            val availableCoaches = daySessionViewModel.coachesForHour.value.filter { it.booked < it.capacity }
            if (availableCoaches.isEmpty()) {
                dialog = Dialog.NoCoachesAvailable
                return@launch
            }

            // CAMBIO: Si hay más de 1 entrenador, mostramos selector.
            // Si solo hay 1, podríamos autoseleccionar o mostrar también el selector (a tu gusto).
            // Aquí forzamos el selector siempre para que veas quién te toca.
            dialog = Dialog.SelectCoach(date, hour, availableCoaches)
        }
    }
    // NUEVA FUNCIÓN: Se llama cuando el usuario hace clic en un entrenador
    fun onCoachSelected(coach: DaySession, date: JavaLocalDate, hour: String, canBook: Boolean) {
        dialog = Dialog.Confirm(date, hour, coach, canBooking = canBook)
    }

    fun bookSession(date: JavaLocalDate, hour: String, coach: DaySession) {
        // 1. Obtenemos el ID del producto que el usuario seleccionó en el dropdown
        val selectedProductId = selectedProduct?.id ?: return

        scope.launch(Dispatchers.IO) {
            // Traducimos ese Producto al Servicio real (Base de datos)
            val realServiceId = daySessionViewModel.fetchServiceIdForProduct(selectedProductId)

            daySessionViewModel.makeBooking(
                customerId = userId,
                coachId = coach.coachId,
                serviceId = realServiceId ?: -1,
                productId = selectedProductId,
                centerId = 1,
                selectedDate = date.toString(),
                hour = hour,
                dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            )
            userViewModel.fetchUserBookings(userId)

            // Importante: Refrescar los límites después de reservar para actualizar el saldo
            daySessionViewModel.fetchUserWeeklyLimit(userId)
            dismiss()
        }
    }

    fun changeSession(
        bookingToChange: UserBooking,
        newDate: JavaLocalDate,
        newHour: String,
        newCoach: DaySession
    ) {
        // 1. Extraemos el ID del producto seleccionado actualmente en la UI
        val selectedProductId = selectedProduct?.id ?: return

        scope.launch(Dispatchers.IO) {
            // 2. Opcional: Si necesitas el serviceId real para la lógica de horarios
            val realServiceId = daySessionViewModel.fetchServiceIdForProduct(selectedProductId) ?: -1

            // 3. Llamamos al ViewModel pasando explícitamente el productId seleccionado
            daySessionViewModel.modifyBookingSession(
                bookingId = bookingToChange.id,
                newCoachId = newCoach.coachId,
                newServiceId = realServiceId,
                newProductId = selectedProductId, // <--- Aquí está el cambio clave
                newDayOfWeek = newDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                newStartDate = newDate.toString(),
                hour = newHour
            )

            // 4. Refrescamos datos globales para que la UI se actualice
            userViewModel.fetchUserBookings(userId)
            daySessionViewModel.fetchUserWeeklyLimit(userId)
            dismiss()
        }
    }

    fun showChangeSelector(date: JavaLocalDate, hour: String, coach: DaySession) {
        dialog = Dialog.ChangeExisting(date, hour, coach)
    }

    fun dismiss() {
        dialog = Dialog.Hidden
        daySessionViewModel.clearBookingErrorMessage()
        if (dialog !is Dialog.Reservation) {
            resetStateForNewDate()
        }
    }

    private fun resetStateForNewDate() {
        selectedProduct = null
        daySessionViewModel.clearCoachesForHour()
    }

    sealed class Dialog {
        object Hidden : Dialog()
        data class ConfirmContinue(val date: JavaLocalDate) : Dialog()
        data class Reservation(val date: JavaLocalDate) : Dialog()
        object HourOccupied : Dialog()
        object NoCoachesAvailable : Dialog()
        data class SelectCoach(
            val date: JavaLocalDate,
            val hour: String,
            val coaches: List<DaySession>
        ) : Dialog()
        data class Confirm(
            val date: JavaLocalDate,
            val hour: String,
            val coach: DaySession,
            val canBooking: Boolean
        ) : Dialog()
        data class ChangeExisting(
            val date: JavaLocalDate,
            val hour: String,
            val coach: DaySession
        ) : Dialog()
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

    // Ahora 'sessionsState' contiene Success, Loading, Error o Empty
    val sessionsState by daySessionViewModel.sessions.collectAsStateWithLifecycle()
    val userBookingLimits by daySessionViewModel.userBookingLimits.collectAsStateWithLifecycle()

    val userProductsState by serviceProductViewModel.userProductsState.collectAsStateWithLifecycle()

    val availableProducts = remember(userProductsState) {
        (userProductsState as? UserProductsUiState.Success)?.products ?: emptyList()
    }

    val bookingError by daySessionViewModel.bookingErrorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        Log.d("📱 APP DEBUG: ","Solicitando límites para usuario: $userId")
        daySessionViewModel.fetchUserWeeklyLimit(userId)
    }

    val state = remember(userId) {
        ReservationFlowState(
            daySessionViewModel, userViewModel, scope, userId, bookingsList
        )
    }

    val currentLimit = userBookingLimits.find { it.productId == state.selectedProduct?.id }
    val canBook = (currentLimit?.remaining ?: 0) > 0

    if (bookingError != null) {
        InfoDialog(
            title = "Ups, algo ha fallado",
            text = bookingError!!,
            onDismiss = { daySessionViewModel.clearBookingErrorMessage() }
        )
    }

    when (val dialog = state.dialog) {
        is ReservationFlowState.Dialog.Reservation -> {
            ReservationDialog(
                state = state,
                date = dialog.date,
                availableProducts = availableProducts,
                sessionsState = sessionsState
            )
        }
        is ReservationFlowState.Dialog.SelectCoach -> {
            CoachSelectionDialog(state, dialog, canBook)
        }
        is ReservationFlowState.Dialog.Confirm -> {
            // Buscamos el límite
            val currentLimit = userBookingLimits.find { it.productId == state.selectedProduct?.id }

            // LÓGICA CORREGIDA:
            // 1. Si currentLimit es null -> No tiene el producto -> false
            // 2. Si remaining es null -> Es ilimitado -> true
            // 3. Si remaining > 0 -> Tiene saldo -> true
            val canBook = currentLimit != null && (
                currentLimit.remaining == null || currentLimit.remaining!! > 0
            )

            ConfirmDialog(
                state = state,
                dialog = dialog.copy(canBooking = canBook)
            )
        }
        is ReservationFlowState.Dialog.ConfirmContinue -> ConfirmContinueDialog(
            onConfirm = { state.startReservationFlow(dialog.date) },
            onDismiss = state::dismiss
        )
        is ReservationFlowState.Dialog.ChangeExisting -> ChangeExistingDialog(state, dialog)
        is ReservationFlowState.Dialog.HourOccupied -> InfoDialog(
            "Reserva existente",
            "Ya tienes una reserva a esa hora.",
            state::dismiss
        )
        is ReservationFlowState.Dialog.NoCoachesAvailable -> InfoDialog(
            "Sin disponibilidad",
            "No hay entrenadores disponibles.",
            state::dismiss
        )
        else -> Unit
    }

    return state::onDayClicked
}

@Composable
private fun ReservationDialog(
    state: ReservationFlowState,
    date: JavaLocalDate,
    availableProducts: List<Product>,
    sessionsState: DailySessionsUiState // Pásale el estado completo
) {
    AlertDialog(
        onDismissRequest = state::dismiss,
        title = {
            ProductPicker(availableProducts, state.selectedProduct) {
                state.onServiceSelected(it, date)
            }
        },
        text = {
            when (sessionsState) {
                is DailySessionsUiState.Loading -> {
                    Box(Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DailySessionsUiState.Success -> {
                    val sessions = sessionsState.sessions
                    val hours = remember(sessions) {
                        sessions.map { it.hour }.distinct().sorted()
                    }
                    HourSelector(state, hours, date, sessions)
                }
                is DailySessionsUiState.Empty -> {
                    Box(Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center) {
                        Text("No hay horarios disponibles.", color = Color.Gray)
                    }
                }
                is DailySessionsUiState.Error -> {
                    Text("Error: ${sessionsState.message}", color = Color.Red)
                }
                else -> Unit
            }
        },
        confirmButton = { TextButton(onClick = state::dismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun HourSelector(
    state: ReservationFlowState,
    hours: List<String>,
    selectedDate: JavaLocalDate,
    currentSessions: List<DaySession>
) {
    val currentNow = state.now

    if (hours.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No hay horarios disponibles para este día.", color = Color.Gray)
        }
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            hours.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement
                    .spacedBy(8.dp)) {
                    row.forEach { hour ->
                        HourChip(
                            hour = hour,
                            sessions = currentSessions, // Usamos la lista fresca
                            selectedDate = selectedDate,
                            now = currentNow
                        ) {
                            state.onHourClicked(hour, selectedDate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HourChip(
    hour: String,
    sessions: List<DaySession>,
    selectedDate: JavaLocalDate,
    now: LocalDateTime,
    onClick: () -> Unit
) {
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
            .padding(vertical = 4.dp)
            .size(80.dp, 40.dp)
            .background(
                if (isPast) DisabledColor else Color.White,
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .clickable(enabled = !isPast) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(hour.take(5), color = color)
    }
}

@Composable
private fun ChangeExistingDialog(
    state: ReservationFlowState,
    dialog: ReservationFlowState.Dialog.ChangeExisting
) {
    // 1. Obtenemos el ID del producto que el usuario tiene seleccionado actualmente
    val selectedProductId = state.selectedProduct?.id

    // 2. Filtramos las reservas del usuario que coincidan con ese producto
    val availableToChange = remember(state.userBookings, selectedProductId) {
        state.userBookings.filter { booking ->
            val bookingDate = booking.date.toLocalDate()

            // FILTRO:
            // - Que la fecha sea válida y futura (o hoy)
            // - Que el productId de la reserva sea igual al seleccionado
            bookingDate != null &&
                    !bookingDate.isBefore(state.today) &&
                    booking.productId == selectedProductId // <--- ESTO ES LA CLAVE
        }
    }

    // 3. El resto del diálogo se mantiene igual, usando 'availableToChange'
    AlertDialog(
        onDismissRequest = state::dismiss,
        title = { Text("Cambiar reserva") },
        text = {
            if (availableToChange.isEmpty()) {
                Text("No hay reservas de este producto para cambiar.")
            } else {
                LazyColumn(Modifier.heightIn(max = 300.dp)) {
                    items(availableToChange) { booking ->
                        Card(
                            Modifier.fillMaxWidth().padding(4.dp)
                                .clickable {
                                    // Aquí llamamos a la función del State que lanzará el proceso
                                    state.changeSession(booking, dialog.date, dialog.hour, dialog.coach)
                                }
                        ) {
                            Text("${booking.date.take(10)} ${booking.hour.take(5)}", Modifier.padding(12.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = state::dismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ProductPicker(
    availableProducts: List<Product>,
    selected: Product?,
    onSelect: (Product) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            text = selected?.name ?: "Seleccionar servicio",
            modifier = Modifier
                .clickable { expanded = true }
                .background(ErrorColor, RoundedCornerShape(8.dp))
                .padding(16.dp),
            color = Color.White)
        DropdownMenu(expanded, { expanded = false }) {
            availableProducts.forEach { s -> DropdownMenuItem(text = { Text(s.name) },
                onClick = { onSelect(s); expanded = false })
            }
        }
    }
}

@Composable
private fun CoachSelectionDialog(
    state: ReservationFlowState,
    dialog: ReservationFlowState.Dialog.SelectCoach,
    canBook: Boolean // Pasamos esto para propagarlo luego
) {
    AlertDialog(
        onDismissRequest = state::dismiss,
        title = { Text("Selecciona profesional") },
        text = {
            LazyColumn {
                items(dialog.coaches) { coach ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Al hacer click, avanzamos al Confirm
                                state.onCoachSelected(coach, dialog.date, dialog.hour, canBook)
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            coach.coachName?.let { Text(
                                text = it,
                                style = MaterialTheme.typography.titleMedium
                            ) }
                            // Opcional: Mostrar ocupación
                            Text(
                                text = "${coach.booked}/${coach.capacity} ocupado",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = state::dismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ConfirmDialog(state: ReservationFlowState, dialog: ReservationFlowState.Dialog.Confirm) {
    AlertDialog(
        onDismissRequest = state::dismiss,
        title = { Text("Confirmar Reserva") },
        text = {
            Text("¿Deseas confirmar tu sesión con ${dialog.coach.coachName} a las " +
                    "${dialog.hour.take(5)}?"
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // SI TIENE SALDO: Mostramos Reservar
                if (dialog.canBooking) {
                    Button(onClick = { state.bookSession(dialog.date, dialog.hour, dialog.coach) }) {
                        Text("Reservar")
                    }
                }

                // SIEMPRE mostramos Cambiar (o solo si no tiene saldo, según tu regla)
                TextButton(onClick = {
                    state.showChangeSelector(dialog.date, dialog.hour, dialog.coach)
                }) {
                    Text("Cambiar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = state::dismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ConfirmContinueDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aviso") },
        text = { Text("Ya tienes una reserva hoy. ¿Continuar?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Sí") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("No") } }
    )
}

@Composable
private fun InfoDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
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
    val hour = this.take(2).toInt()
    val minute = this.substring(3, 5).toInt()
    return date.atTime(hour, minute)
}
