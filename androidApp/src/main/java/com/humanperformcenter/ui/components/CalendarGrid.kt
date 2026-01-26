package com.humanperformcenter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.presentation.viewmodel.DaySessionViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.collections.buildList
import kotlin.time.Clock
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun CalendarGrid(
    displayedMonth: Month,
    displayedYear: Int,
    today: LocalDate,
    daySessionViewModel: DaySessionViewModel,
    userBookings: List<UserBooking>,
    onDayClicked: (LocalDate) -> Unit
) {
    // Mostrar el calendario del mes mostrado
    val firstDayOfMonth = LocalDate(displayedYear, displayedMonth, 1)
    val isLeapYear = (displayedYear % 4 == 0) && (displayedYear % 100 != 0 || displayedYear % 400 == 0)
    val daysInMonth = displayedMonth.length(isLeapYear)
    // Offset para que la semana empiece en lunes
    val offset = firstDayOfMonth.dayOfWeek.ordinal

    // Render calendar grid
    val bookings = userBookings

    val reservedDates = bookings.mapNotNull { booking ->
        try {
            LocalDate.parse(booking.date.take(10))
        } catch (_: Exception) {
            null
        }
    }.toSet()

    val calendarDays = buildList {
        repeat(offset) {
            add(null) // Días vacíos antes del 1er día del mes
        }
        for (day in 1..daysInMonth) {
            add(LocalDate(displayedYear, displayedMonth, day))
        }
    }

    val coloresPorServicio = mapOf(
        1 to Color(0xFF97DE98),    // Verde claro
        2 to Color(0xFF84B8E3), // Azul claro
        3 to Color(0xFFECDB6C),               // Amarillo suave
        4 to Color(0xFFDE8B75)         // Naranja suave
    )

    val currentMonth = today.month
    val mesSiguiente = Month.entries[(currentMonth.ordinal + 1) % 12]
    val anioMesSiguiente = if (currentMonth == Month.DECEMBER) today.year + 1 else today.year

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        items(calendarDays) { date ->
            if (date == null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                )
            } else {
                val isToday = date == today
                val isSunday = date.dayOfWeek.ordinal == 6

                // Festivos desde VM
                val holidays by daySessionViewModel.holidays.collectAsState()
                val isHoliday = holidays.contains(date)

                val isReserved = reservedDates.contains(date)

                // Reservas del día y color por servicio
                val reservasEseDia = bookings.filter {
                    try {
                        LocalDate.parse(it.date.substring(0, 10)) == date
                    } catch (_: Exception) {
                        false
                    }
                }
                val colorReserva = reservasEseDia
                    .firstOrNull()?.service_id
                    ?.let { coloresPorServicio[it] }
                    ?: MaterialTheme.colorScheme.tertiary

                val isPast = date < today

                // Ventanas de selección (tu lógica original)
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val currentHour = now.hour

                val isAfterMiddayOn15 = today.day == 15 && currentHour >= 12
                val isAfter15 = today.day > 15 || isAfterMiddayOn15

                val esMesActual = date.year == today.year && date.month == currentMonth
                val esMesSiguiente = date.year == anioMesSiguiente && date.month == mesSiguiente

                val puedeSeleccionarFecha = !isSunday && !isPast && !isHoliday && (
                        (today.day < 15 && esMesActual) ||
                                (isAfter15 && (esMesActual || esMesSiguiente))
                        )

                // ====== Estilos claro/oscuro ======
                val isDark = isSystemInDarkTheme()

                // Fondo del día
                val bgColor = when {
                    reservasEseDia.isNotEmpty() -> colorReserva
                    isDark -> { // MODO OSCURO: mantenemos como estaba
                        when {
                            isSunday || isHoliday -> MaterialTheme.colorScheme.surfaceVariant
                            else -> Color.Transparent
                        }
                    }
                    else -> { // MODO CLARO: swap pasados ↔ festivos y disponibles con caja blanca
                        when {
                            isPast -> MaterialTheme.colorScheme.surfaceVariant      // pasados: chip gris
                            isSunday || isHoliday -> Color.Transparent              // festivos: sin relleno
                            else -> MaterialTheme.colorScheme.surface               // disponibles: caja blanca
                        }
                    }
                }

                // Color del texto
                val textColor = when {
                    isReserved -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                }

                // Borde: hoy siempre marcado; en claro, si caja blanca, borde sutil
                val showOutline = !isDark && bgColor == MaterialTheme.colorScheme.surface
                val borderWidth = if (isToday) 2.dp else if (showOutline) 1.dp else 0.dp
                val borderColor = when {
                    isToday -> MaterialTheme.colorScheme.secondary
                    showOutline -> MaterialTheme.colorScheme.outline
                    else -> Color.Transparent
                }

                // Opacidad si no se puede seleccionar
                val boxBg = bgColor.copy(alpha = if (puedeSeleccionarFecha) 1f else 0.4f)
                val overallAlpha = if (!puedeSeleccionarFecha) 0.4f else 1f

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(boxBg)
                        .alpha(overallAlpha)
                        .border(borderWidth, borderColor, CircleShape)
                        .clickable(enabled = puedeSeleccionarFecha) {
                            onDayClicked(date)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Texto: si el día no es seleccionable, baja un poco la opacidad
                    val dayTextColor = if (puedeSeleccionarFecha)
                        textColor
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

                    Text(
                        text = date.day.toString(),
                        color = dayTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun Month.length(isLeapYear: Boolean): Int = when (this) {
    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
    Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31

    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30

    Month.FEBRUARY -> if (isLeapYear) 29 else 28
}