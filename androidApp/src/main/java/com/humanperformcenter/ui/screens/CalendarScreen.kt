@file:OptIn(ExperimentalMaterial3Api::class)

package com.humanperformcenter.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.humanperformcenter.NavigationBar
import com.humanperformcenter.R
import com.humanperformcenter.data.Session
import com.humanperformcenter.viewModels.SessionViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@Composable
fun CalendarScreen(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    onPlaySound: (Int) -> Unit
) {
    // Estado para el diálogo de eliminación
    var showDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<Session?>(null) }

    // Estado para el diálogo de reserva y tipo de sesión
    var showReservaDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var tipoSesion by remember { mutableStateOf("Entrenamiento") } // o "Fisioterapia"

    // Scope para llamadas asincronas
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 8.dp)
                        )
                    }
                },
                colors = topAppBarColors(
                    containerColor = Color(0xFFB71C1C), // Rojo fuerte, ajustable
                    titleContentColor = Color.White
                ),
                navigationIcon = {},
                actions = {}
            )
        },
        bottomBar = { NavigationBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 0.dp),
        content = { paddingValues ->
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
                            "entrenamiento" -> Color(0xFF4CAF50) // verde
                            "fisioterapia" -> Color(0xFF2196F3)  // azul
                            "nutrición" -> Color(0xFFD32F2F)     // rojo
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
                val firstDayOfWeek = firstDayOfMonth.dayOfWeek
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
                                                selectedDate = if (selectedDate == date) null else date
                                                showReservaDialog = true
                                            } else {
                                                selectedDate = date
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
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                                    sessionToDelete = session
                                    showDialog = true
                                    false // Impide la eliminación automática hasta confirmar
                                } else false
                            }
                        )

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
                                SessionItem(
                                    session = session,
                                    onClick = {}, // Added missing parameter
                                    showDialog = showDialog,
                                    setShowDialog = { showDialog = it },
                                    sessionToDelete = sessionToDelete,
                                    setSessionToDelete = { sessionToDelete = it }
                                )
                            }
                        )
                    }
                }
            }
        }
    )
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
                            listOf("Entrenamiento", "Fisioterapia", "Nutrición").forEach { tipo ->
                                val selected = tipoSesion == tipo
                                Button(
                                    onClick = { tipoSesion = tipo },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            selected && tipo == "Entrenamiento" -> Color(0xFF4CAF50)
                                            selected && tipo == "Fisioterapia" -> Color(0xFF2196F3)
                                            selected && tipo == "Nutrición" -> Color(0xFFD32F2F)
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
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                        .verticalScroll(scrollState)
                ) {
                    // Centro 1: El cerro
                    Text("Centro 1: El cerro", fontWeight = FontWeight.Bold)
                    val weekday = selectedDate!!.dayOfWeek
                    val centro1MorningHours = when (weekday) {
                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY -> listOf(
                            "06:30", "07:30", "08:30", "09:30", "10:30", "11:30", "12:30"
                        )
                        DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY -> listOf(
                            "08:30", "09:30", "10:30"
                        )
                        else -> emptyList()
                    }

                    val centro1EveningHours = when (weekday) {
                        DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY -> listOf(
                            "16:00", "17:00", "18:00", "19:00", "20:00"
                        )
                        DayOfWeek.TUESDAY, DayOfWeek.THURSDAY -> listOf(
                            "17:00", "18:00"
                        )
                        else -> emptyList()
                    }

                    // Modificador común para los botones de hora
                    val buttonModifier = Modifier
                        .width(80.dp)
                        .height(40.dp)

                    if (centro1MorningHours.isEmpty()) {
                        Text("No hay horas disponibles.", modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        val buttonModifier = Modifier
                            .width(80.dp)
                            .height(40.dp)
                        centro1MorningHours.chunked(3).forEach { rowHoras ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (0 until 3).forEach { index ->
                                    if (index < rowHoras.size) {
                                        val hora = rowHoras[index]
                                        Box(
                                            modifier = buttonModifier
                                                .border(
                                                    width = 1.dp,
                                                    color = when (tipoSesion) {
                                                        "Entrenamiento" -> Color(0xFF388E3C)
                                                        "Fisioterapia" -> Color(0xFF1976D2)
                                                        "Nutrición" -> Color(0xFFC62828)
                                                        else -> Color.Gray
                                                    },
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable {
                                                    val session = Session(
                                                        service = tipoSesion,
                                                        product = "Centro 1",
                                                        date = selectedDate!!.atTime(
                                                            hora.substringBefore(":").toInt(),
                                                            hora.substringAfter(":").toInt()
                                                        ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                                                        hour = hora,
                                                        professional = "Asignado por el sistema"
                                                    )
                                                    scope.launch {
                                                        sessionViewModel.insertSession(session)
                                                    }
                                                    showReservaDialog = false
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = hora)
                                        }
                                    } else {
                                        Spacer(modifier = buttonModifier)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.padding(vertical = 4.dp))

                    if (centro1EveningHours.isEmpty()) {
                        Text("No hay horas disponibles.", modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        val buttonModifier = Modifier
                            .width(80.dp)
                            .height(40.dp)
                        centro1EveningHours.chunked(3).forEach { rowHoras ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (0 until 3).forEach { index ->
                                    if (index < rowHoras.size) {
                                        val hora = rowHoras[index]
                                        Box(
                                            modifier = buttonModifier
                                                .border(
                                                    width = 1.dp,
                                                    color = when (tipoSesion) {
                                                        "Entrenamiento" -> Color(0xFF388E3C)
                                                        "Fisioterapia" -> Color(0xFF1976D2)
                                                        "Nutrición" -> Color(0xFFC62828)
                                                        else -> Color.Gray
                                                    },
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable {
                                                    val session = Session(
                                                        service = tipoSesion,
                                                        product = "Centro 1",
                                                        date = selectedDate!!.atTime(
                                                            hora.substringBefore(":").toInt(),
                                                            hora.substringAfter(":").toInt()
                                                        ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                                                        hour = hora,
                                                        professional = "Asignado por el sistema"
                                                    )
                                                    scope.launch {
                                                        sessionViewModel.insertSession(session)
                                                    }
                                                    showReservaDialog = false
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = hora)
                                        }
                                    } else {
                                        Spacer(modifier = buttonModifier)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.padding(vertical = 8.dp))

                    // Centro 2: El sotillo
                    Text("Centro 2: El sotillo", fontWeight = FontWeight.Bold)
                    val centro2Hours = when (weekday) {
                        DayOfWeek.MONDAY -> listOf("10:00", "11:00", "12:00", "17:00")
                        DayOfWeek.TUESDAY -> listOf("09:00", "10:00")
                        DayOfWeek.WEDNESDAY -> listOf("10:00", "11:00")
                        DayOfWeek.THURSDAY -> listOf("10:00", "11:00", "12:00")
                        DayOfWeek.FRIDAY -> listOf("09:00", "10:00", "11:00")
                        else -> emptyList()
                    }
                    if (tipoSesion == "Nutrición") {
                        Text("No disponible para nutrición", color = Color.Gray)
                    } else {
                        if (centro2Hours.isEmpty()) {
                            Text("No hay horas disponibles.", modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            val buttonModifier = Modifier
                                .width(80.dp)
                                .height(40.dp)
                            centro2Hours.chunked(3).forEach { rowHoras ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    (0 until 3).forEach { index ->
                                        if (index < rowHoras.size) {
                                            val hora = rowHoras[index]
                                            Box(
                                                modifier = buttonModifier
                                                    .border(
                                                        width = 1.dp,
                                                        color = when (tipoSesion) {
                                                            "Entrenamiento" -> Color(0xFF388E3C)
                                                            "Fisioterapia" -> Color(0xFF1976D2)
                                                            "Nutrición" -> Color(0xFFC62828)
                                                            else -> Color.Gray
                                                        },
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable {
                                                        val session = Session(
                                                            service = tipoSesion,
                                                            product = "Centro 2",
                                                            date = selectedDate!!.atTime(
                                                                hora.substringBefore(":").toInt(),
                                                                hora.substringAfter(":").toInt()
                                                            ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                                                            hour = hora,
                                                            professional = "Asignado por el sistema"
                                                        )
                                                        scope.launch {
                                                            sessionViewModel.insertSession(session)
                                                        }
                                                        showReservaDialog = false
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = hora)
                                            }
                                        } else {
                                            Spacer(modifier = buttonModifier)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
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
}




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

