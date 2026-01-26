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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.datetime.number
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.YearMonth

@Composable
fun CalendarGrid(
    displayedMonth: Month,
    displayedYear: Int,
    today: LocalDate,
    daySessionViewModel: DaySessionViewModel,
    userBookings: List<UserBooking>,
    onDayClicked: (LocalDate) -> Unit
) {
    val yearMonth = YearMonth.of(displayedYear, displayedMonth)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)

    val offset = firstDayOfMonth.dayOfWeek.value - 1

    // 2. Procesar reservas (Set para búsqueda rápida O(1))
    val reservedDates = remember(userBookings) {
        userBookings.mapNotNull { booking ->
            try { LocalDate.parse(booking.date.take(10)) } catch (_: Exception) { null }
        }.toSet()
    }

    // 3. Construir lista de días
    val calendarDays = remember(displayedYear, displayedMonth) {
        buildList {
            repeat(offset) { add(null) }
            for (day in 1..daysInMonth) {
                add(yearMonth.atDay(day))
            }
        }
    }

    val coloresPorServicio = mapOf(
        1 to Color(0xFF97DE98),
        2 to Color(0xFF84B8E3),
        3 to Color(0xFFECDB6C),
        4 to Color(0xFFDE8B75)
    )

    // 4. Lógica de ventanas (Día 15)
    val mesSiguiente = today.month.plus(1)
    val anioMesSiguiente = if (today.month == Month.DECEMBER) today.year + 1 else today.year
    val nowDateTime = LocalDateTime.now() // Para la hora actual

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        items(calendarDays) { date ->
            if (date == null) {
                Box(modifier = Modifier.size(40.dp).padding(4.dp))
            } else {
                val isToday = date.isEqual(today)
                val isSunday = date.dayOfWeek.value == 7 // Domingo en java.time es 7

                val holidaysKmm by daySessionViewModel.holidays.collectAsState()

                val holidaysJava = remember(holidaysKmm) {
                    holidaysKmm.map { kmmDate ->
                        LocalDate.of(kmmDate.year, kmmDate.month.number, kmmDate.day)
                    }.toSet()
                }

                val isHoliday = holidaysJava.contains(date)
                val isReserved = reservedDates.contains(date)

                // Buscar reserva específica para color
                val reservaEseDia = userBookings.firstOrNull {
                    try { LocalDate.parse(it.date.take(10)).isEqual(date) } catch (_: Exception) { false }
                }

                val colorReserva = reservaEseDia?.service_id?.let { coloresPorServicio[it] }
                    ?: MaterialTheme.colorScheme.tertiary

                val isPast = date.isBefore(today)

                // Lógica de validación de selección
                val isAfterMiddayOn15 = today.dayOfMonth == 15 && nowDateTime.hour >= 12
                val isAfter15 = today.dayOfMonth > 15 || isAfterMiddayOn15

                val esMesActual = date.year == today.year && date.month == today.month
                val esMesSiguiente = date.year == anioMesSiguiente && date.month == mesSiguiente

                val puedeSeleccionarFecha = !isSunday && !isPast && !isHoliday && (
                        (today.dayOfMonth < 15 && esMesActual) ||
                                (isAfter15 && (esMesActual || esMesSiguiente))
                        )

                val isDark = isSystemInDarkTheme()

                // Fondo
                val bgColor = when {
                    isReserved -> colorReserva
                    isDark -> {
                        when {
                            isSunday || isHoliday -> MaterialTheme.colorScheme.surfaceVariant
                            else -> Color.Transparent
                        }
                    }
                    else -> {
                        when {
                            isPast -> MaterialTheme.colorScheme.surfaceVariant
                            isSunday || isHoliday -> Color.Transparent
                            else -> MaterialTheme.colorScheme.surface
                        }
                    }
                }

                val textColor = if (isReserved) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface

                val showOutline = !isDark && bgColor == MaterialTheme.colorScheme.surface
                val borderWidth = if (isToday) 2.dp else if (showOutline) 1.dp else 0.dp
                val borderColor = when {
                    isToday -> MaterialTheme.colorScheme.secondary
                    showOutline -> MaterialTheme.colorScheme.outline
                    else -> Color.Transparent
                }

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
                    Text(
                        text = date.dayOfMonth.toString(), // .dayOfMonth en java.time
                        color = if (puedeSeleccionarFecha) textColor else textColor.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
