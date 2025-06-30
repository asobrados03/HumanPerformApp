package com.humanperformcenter.ui.screens

import android.util.Log.e
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.humanperformcenter.R
import com.humanperformcenter.data.Session
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.ui.components.SessionItem
import com.humanperformcenter.ui.util.createICSFile
import com.humanperformcenter.ui.util.shareICS
import com.humanperformcenter.ui.viewmodel.SesionesDiaViewModel
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.humanperformcenter.ui.viewmodel.UserViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun CalendarScreen(
    navController: NavHostController,
    sesionesDiaViewModel: SesionesDiaViewModel,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel,
    onPlaySound: (Int) -> Unit
) {
    // Estado para el diálogo de eliminación
    var showDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<Session?>(null) }

    // Estado para el diálogo de reserva y tipo de sesión
    var showReservaDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var tipoSesion by remember { mutableStateOf("Nutrición") } // o "Fisioterapia"

    // Scope para llamadas asincronas
    val scope = rememberCoroutineScope()
    val sesionesRemotas by sesionesDiaViewModel.sessions.collectAsState()

    // Estado para el selector de coach
    var mostrarSelectorCoach by remember { mutableStateOf(false) }
    var horaSeleccionada by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = false,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        bottomBar = { NavigationBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 0.dp)
    ) { paddingValues ->
        val sessionList = sessionViewModel.getAllSessions
            .collectAsState(initial = emptyList())

        if (sessionList.value.isEmpty()) {
            Text(
                text = "No hay sesiones disponibles.",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
        }

        // Procesar transacciones para el calendario y selección de día
        val todayInstant = Clock.System.now()
        val todayLocalDate = todayInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val currentYear = todayLocalDate.year
        val currentMonth = todayLocalDate.month

        // Estados para mes y año mostrados (navegación)
        var displayedMonth by remember { mutableStateOf(currentMonth) }
        var displayedYear by remember { mutableIntStateOf(currentYear) }

        // Nombres de los meses en español
        val monthNames = mapOf(
            Month.JANUARY to "Enero",
            Month.FEBRUARY to "Febrero",
            Month.MARCH to "Marzo",
            Month.APRIL to "Abril",
            Month.MAY to "Mayo",
            Month.JUNE to "Junio",
            Month.JULY to "Julio",
            Month.AUGUST to "Agosto",
            Month.SEPTEMBER to "Septiembre",
            Month.OCTOBER to "Octubre",
            Month.NOVEMBER to "Noviembre",
            Month.DECEMBER to "Diciembre"
        )

        // Map LocalDate -> Color según el tipo de sesión
        val dayColorMap = remember(sessionList.value, displayedMonth, displayedYear) {
            val map = mutableMapOf<LocalDate, Color>()
            sessionList.value.forEach { session ->
                val instant = Instant.fromEpochMilliseconds(session.date)
                val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                if (localDate.year == displayedYear && localDate.month == displayedMonth) {
                    val color = when (session.service.lowercase()) {
                        "Nutrición" -> Color(0xFFD32F2F)     // rojo
                        "Entrenamiento" -> Color(0xFF4CAF50) // verde
                        "Fisioterapia" -> Color(0xFF2196F3)  // azul
                        else -> Color(0xFF2196F3)            // por defecto azul
                    }
                    map[localDate] = color
                }
            }
            map
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mostrar el mes y año arriba con navegación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Botón mes anterior
                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowLeft,
                    contentDescription = "Mes anterior",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            // Ir al mes anterior
                            val prevMonthNumber = displayedMonth.ordinal
                            if (prevMonthNumber < 1) {
                                displayedMonth = Month.DECEMBER
                                displayedYear -= 1
                            } else {
                                displayedMonth = Month.entries[prevMonthNumber - 1]
                            }
                        }
                        .padding(end = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Nombre del mes y año
                Text(
                    text = "${monthNames[displayedMonth]} $displayedYear",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Botón mes siguiente
                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowRight,
                    contentDescription = "Mes siguiente",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            // Ir al mes siguiente
                            val nextMonthNumber = displayedMonth.ordinal + 2
                            if (nextMonthNumber > 12) {
                                displayedMonth = Month.JANUARY
                                displayedYear += 1
                            } else {
                                displayedMonth = Month.entries[nextMonthNumber - 1]
                            }
                        }
                        .padding(start = 8.dp)
                )
            }

            // Mostrar días de la semana (lunes a domingo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Solo lunes a domingo
                listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mostrar el calendario del mes mostrado
            val firstDayOfMonth = LocalDate(displayedYear, displayedMonth, 1)
            val isLeapYear = (displayedYear % 4 == 0) && (displayedYear % 100 != 0 || displayedYear % 400 == 0)
            val daysInMonth = displayedMonth.length(isLeapYear)
            // Offset para que la semana empiece en lunes
            val offset = firstDayOfMonth.dayOfWeek.ordinal

            // Total cells to show in calendar grid (weeks * 7)
            val totalCells = ((offset + daysInMonth + 6) / 7) * 7

            // Render calendar grid
            val numWeeks = totalCells / 7
            for (week in 0 until numWeeks) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayIndex in 0..6) {
                        val cellIndex = week * 7 + dayIndex
                        val dayNumber = cellIndex - offset + 1
                        if (cellIndex < offset || dayNumber > daysInMonth) {
                            val dayText = if (cellIndex < offset) {
                                val prevMonth = if (displayedMonth.ordinal == 0) Month.DECEMBER else Month.entries[displayedMonth.ordinal - 1]
                                val prevYear = if (displayedMonth.ordinal == 0) displayedYear - 1 else displayedYear
                                val daysInPrevMonth = prevMonth.length((prevYear % 4 == 0) && (prevYear % 100 != 0 || prevYear % 400 == 0))
                                (daysInPrevMonth - (offset - cellIndex) + 1).toString()
                            } else {
                                ((dayNumber - daysInMonth)).toString()
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = dayText, color = Color.Gray)
                            }
                        } else {
                            val date = LocalDate(displayedYear, displayedMonth, dayNumber)
                            val eventColor = dayColorMap[date] ?: Color.Transparent
                            val isSelected = selectedDate == date
                            val isSunday = (dayIndex == 6)
                            val cellBackgroundColor = when {
                                eventColor != Color.Transparent -> eventColor
                                isSunday -> Color.LightGray
                                else -> Color.Transparent
                            }
                            val textColor = if (eventColor != Color.Transparent) Color.White else MaterialTheme.colorScheme.onSurface
                            val isToday = date == todayLocalDate
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .background(color = cellBackgroundColor, shape = RoundedCornerShape(50))
                                    .border(
                                        width = if (isToday) 2.dp else 0.dp,
                                        color = if (isToday) Color.Black else Color.Transparent,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .then(
                                        if (isSelected)
                                            Modifier
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(50)
                                                )
                                        else Modifier
                                    )
                                    .clickable {
                                        val isSunday = (dayIndex == 6)
                                        if (!isSunday) {
                                            selectedDate = date
                                            tipoSesion = "Nutrición"
                                            showReservaDialog = true
                                            val tipoActual = tipoSesion
                                            val serviceId = when (tipoActual.lowercase()) {
                                                "Nutrición" -> 1
                                                "Entrenamiento" -> 2
                                                "Fisioterapia" -> 3

                                                else -> 1
                                            }
                                            sesionesDiaViewModel.fetchAvailableSessions(serviceId, date, tipoActual)
                                        } else {
                                            // Si es domingo, no se puede seleccionar
                                            selectedDate = null
                                            showReservaDialog = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    color = textColor,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar siempre todas las sesiones del mes/año mostrado, independientemente de si hay día seleccionado
            val filteredSessions = sessionList.value.filter { session ->
                val instant = Instant.fromEpochMilliseconds(session.date)
                val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                localDate.year == displayedYear && localDate.month == displayedMonth
            }

            // Columna eficiente que muestra la lista de sesiones añadidas al sistema
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSessions, key = { session -> session.id }) { session ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        SwipeToDismissBoxValue.Settled,
                        SwipeToDismissBoxDefaults.positionalThreshold
                    )

                    // Observa cuando se completa el swipe
                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart ||
                            dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                            sessionToDelete = session
                            showDialog = true
                            // Resetea el estado para evitar que se quede "dismissed"
                            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                        }
                    }

                    //Este elemento permite eliminar y editar las sesiones de la lista
                    SwipeToDismissBox(
                        modifier = Modifier.animateContentSize(),
                        state = dismissState,
                        backgroundContent = {
                            val color by animateColorAsState(MaterialTheme.colorScheme.error,
                                label = "dismiss_background"
                            )

                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Icon",
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                }
                            } else if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Icon",
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        },
                        enableDismissFromEndToStart = true,
                        enableDismissFromStartToEnd = true,
                        content = {
                            // Context menu state for each SessionItem
                            var expanded by remember { mutableStateOf(false) }
                            val context = LocalContext.current
                            Box {
                                SessionItem(
                                    session = session,
                                    onClick = { expanded = true },
                                    showDialog = showDialog,
                                    setShowDialog = { showDialog = it },
                                    sessionToDelete = sessionToDelete,
                                    setSessionToDelete = { sessionToDelete = it }
                                )
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Eliminar cita") },
                                        onClick = {
                                            sessionToDelete = session
                                            showDialog = true
                                            expanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Cambiar cita") },
                                        onClick = {
                                            // Lógica para cambiar cita
                                            expanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Descargar evento") },
                                        onClick = {
                                            val icsContent = createICSFile(
                                                eventTitle = session.service,
                                                startDateTime = Instant.fromEpochMilliseconds(session.date)
                                            )
                                            shareICS(context, icsContent)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    // Diálogo de reserva de sesión
    if (showReservaDialog && selectedDate != null) {
        AlertDialog(
            onDismissRequest = { showReservaDialog = false },
            confirmButton = {},
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Selecciona sesión")
                        Column {
                            listOf("Nutrición", "Entrenamiento", "Fisioterapia").forEach { tipo ->
                                val selected = tipoSesion == tipo
                                Button(
                                    onClick = {
                                        tipoSesion = tipo

                                        selectedDate?.let { fecha ->
                                            val serviceId = when (tipo.lowercase()) {
                                                "nutrición" -> 1
                                                "entrenamiento" -> 2
                                                "fisioterapia" -> 3
                                                else -> 1
                                            }
                                            sesionesDiaViewModel.fetchAvailableSessions(serviceId, fecha, tipo)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            selected && tipo == "Nutrición" -> Color(0xFFD32F2F)
                                            selected && tipo == "Entrenamiento" -> Color(0xFF4CAF50)
                                            selected && tipo == "Fisioterapia" -> Color(0xFF2196F3)
                                            else -> Color(0xFFE0E0E0)
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(tipo)
                                }
                            }
                        }
                    }
                    IconButton(onClick = { showReservaDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            },
            text = {
                val horariosDisponibles = sesionesRemotas
                    .map { it.hour }
                    .filter { it.endsWith(":00:00") || it.endsWith(":30:00") }
                    .distinct()
                    .sorted()
                if (horariosDisponibles.isEmpty()) {
                    Text("No hay sesiones disponibles para este día.", modifier = Modifier.padding(8.dp))
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        horariosDisponibles.chunked(3).forEach { fila ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                fila.forEach { hora ->
                                    val sesion = sesionesRemotas.firstOrNull { it.hour == hora }
                                    val disponibilidad = if (sesion != null && sesion.capacity > 0) {
                                        sesion.booked.toFloat() / sesion.capacity.toFloat()
                                    } else 1f
                                    val bgColor = when {
                                        disponibilidad < 0.5f -> Color(0xFF4CAF50) // verde
                                        disponibilidad < 1f -> Color(0xFFFFA000) // amarillo
                                        else -> Color(0xFFD32F2F) // rojo
                                    }

                                    val horaFormateada = hora.substring(0,5) // Asegúrate de que la hora tenga el formato correcto

                                    Box(
                                        modifier = Modifier
                                            .width(70.dp)
                                            .height(40.dp)
                                            .border(1.dp, bgColor, RoundedCornerShape(12.dp))
                                            .clickable {
                                                sesionesDiaViewModel.obtenerEntrenadoresPorHora(hora)
                                                horaSeleccionada = hora
                                                mostrarSelectorCoach = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(horaFormateada, color = bgColor)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        )
    }
    // Diálogo de confirmación de borrado
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¿Desea eliminar esta sesión?") },
            text = {
                Text(
                    "Esta acción conlleva que esta sesión y toda la información relacionada con ella se elimine permanentemente de la aplicación."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sessionToDelete?.let { sessionViewModel.deleteSession(it) }
                        onPlaySound(R.raw.delete_sound)
                        showDialog = false
                    }
                ) {
                    Text("Aceptar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (mostrarSelectorCoach && horaSeleccionada != null && selectedDate != null) {
        val user = userViewModel.userData.collectAsState().value
        requireNotNull(user) { "Usuario no disponible. Asegúrate de estar autenticado." }
        val customerId = user.id
        var errorReserva by remember { mutableStateOf<String?>(null) }

        val coaches = sesionesDiaViewModel.coachesForHour.collectAsState().value

        if (coaches.none { it.booked < it.capacity }) {
            AlertDialog(
                onDismissRequest = { mostrarSelectorCoach = false },
                confirmButton = {},
                title = { Text("Sin entrenadores disponibles") },
                text = { Text("No hay entrenadores disponibles para esta hora.") }
            )
        } else {
            val availableCoaches = coaches.filter { it.booked < it.capacity }
            val serviceId = when (tipoSesion.lowercase()) {
                "nutrición" -> 1
                "entrenamiento" -> 2
                "fisioterapia" -> 3
                else -> 1
            }
            val fechaISO = selectedDate.toString()
            val horaSeleccionadaFinal = horaSeleccionada!!

            LaunchedEffect(horaSeleccionadaFinal) {
                val preferredCoachId = sesionesDiaViewModel.getPreferredCoachId(customerId, serviceId)
                val coachElegido = availableCoaches.firstOrNull { it.coach_id == preferredCoachId }
                    ?: availableCoaches.randomOrNull()

                if (coachElegido != null) {
                    try {
                        sesionesDiaViewModel.realizarReserva(
                            customerId = customerId,
                            coachId = coachElegido.coach_id,
                            serviceId = serviceId,
                            centerId = 1,
                            selectedDate = fechaISO,
                            hour = horaSeleccionadaFinal
                        )
                        mostrarSelectorCoach = false
                        showReservaDialog = false
                        horaSeleccionada = null

                        println("Reserva creada con éxito")
                    } catch (e: Exception) {
                        println("❌ Error al reservar: ${e.message}")
                    }
                }
            }
        }
    }
    val mensajeError by sesionesDiaViewModel.mensajeErrorReserva.collectAsState()

    if (mensajeError != null) {
        AlertDialog(
            onDismissRequest = { sesionesDiaViewModel.clearMensajeError() },
            title = { Text("Error de reserva") },
            text = { Text(mensajeError ?: "") },
            confirmButton = {
                TextButton(onClick = { sesionesDiaViewModel.clearMensajeError() }) {
                    Text("OK")
                }
            }
        )
    }

}

private fun Month.length(isLeapYear: Boolean): Int = when (this) {
    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
    Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31

    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30

    Month.FEBRUARY -> if (isLeapYear) 29 else 28
}
