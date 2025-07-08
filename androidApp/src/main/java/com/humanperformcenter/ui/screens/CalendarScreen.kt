package com.humanperformcenter.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.R
import com.humanperformcenter.data.Session
import com.humanperformcenter.shared.data.model.ServicioDispo
import com.humanperformcenter.shared.data.model.SesionesDia
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.ui.util.createICSFile
import com.humanperformcenter.ui.util.shareICS
import com.humanperformcenter.ui.viewmodel.SesionesDiaViewModel
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.humanperformcenter.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.collections.get
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


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
    var tipoSesion by remember { mutableStateOf<ServicioDispo?>(null) } // o "Fisioterapia"
    var servicioSeleccionadoId by remember { mutableStateOf<Int?>(null) }

    // Scope para llamadas asincronas
    val scope = rememberCoroutineScope()
    val sesionesRemotas by sesionesDiaViewModel.sessions.collectAsState()

    // Estado para el coach elegido y botón de confirmación
    var coachElegido by remember { mutableStateOf<SesionesDia?>(null) }
    var mostrarBotonConfirmar by remember { mutableStateOf(false) }

    var mostrarSelectorReservaExistente by remember { mutableStateOf(false) }

    // Estado para el selector de coach
    var mostrarSelectorCoach by remember { mutableStateOf(false) }
    var horaSeleccionada by remember { mutableStateOf<String?>(null) }

    // Estado para el dropdown de tipo de sesión
    val serviciosPermitidos = sessionViewModel.allowedServices.collectAsState().value

    val menuExpandedMap = remember { mutableStateMapOf<Int, Boolean>() }

    var showReservaConfirmDialog by remember { mutableStateOf(false) }
    var pendingSelectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // Estado para el filtro de servicio
    var servicioFiltro by remember { mutableStateOf<ServicioDispo?>(null) }
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var soloPuedeCambiar by remember { mutableStateOf(false) }

    val user = userViewModel.userData.collectAsState().value
    val userBookings = userViewModel.userBookings.collectAsState().value
    val weeklyLimits = sesionesDiaViewModel.weeklyLimits.collectAsState().value
    val unlimitedSessions = sesionesDiaViewModel.unlimitedSessions.collectAsState().value



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

            // Render calendar grid
            val bookings by userViewModel.userBookings.collectAsState()

            val reservedDates = bookings.mapNotNull { booking ->
                try {
                    LocalDate.parse(booking.date.substring(0, 10))
                } catch (e: Exception) {
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                items(calendarDays) { date ->
                    if (date == null) {
                        Box(modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                        )
                    } else {
                        val isToday = date == todayLocalDate
                        val isSunday = date.dayOfWeek.ordinal == 6
                        val isSelected = selectedDate == date
                        val isReserved = reservedDates.contains(date)

                        val bgColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isReserved -> Color(0xFF64B5F6)
                            isSunday -> Color.LightGray
                            else -> Color.Transparent
                        }

                        val textColor = when {
                            isSelected || isReserved -> Color.White
                            else -> Color.Black
                        }
                        val isPast = date < todayLocalDate
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .alpha(if (isPast) 0.4f else 1f)
                                .border(
                                    width = if (isToday) 2.dp else 0.dp,
                                    color = if (isToday) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable(enabled = !isSunday && !isPast) {
                                    if (isReserved) {
                                        pendingSelectedDate = date
                                        showReservaConfirmDialog = true
                                    } else {
                                        selectedDate = date
                                        showReservaDialog = true
                                        tipoSesion = null
                                        sesionesDiaViewModel.clearSessions()

                                        servicioSeleccionadoId?.let { servicioId ->
                                            sesionesDiaViewModel.fetchAvailableSessions(servicioId, date)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔁 NUEVO BLOQUE DE RESERVAS DEL USUARIO
            Spacer(modifier = Modifier.height(16.dp))

            val user = userViewModel.userData.collectAsState().value
            val userId = user?.id

            LaunchedEffect(userId) {
                userId?.let {
                    userViewModel.fetchUserBookings(it)
                    sesionesDiaViewModel.fetchUserWeeklyLimit(it)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)

            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)) {

                    Text(
                        text = "Filtrar por servicio:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    var expandedFiltro by remember { mutableStateOf(false) }
                    var anchoBotonFiltro by remember { mutableIntStateOf(0) }

                    Box {
                        Text(
                            text = servicioFiltro?.name ?: "Todas",
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF455A64), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                                .clickable { expandedFiltro = true }
                                .onGloballyPositioned { anchoBotonFiltro = it.size.width }
                        )

                        DropdownMenu(
                            expanded = expandedFiltro,
                            onDismissRequest = { expandedFiltro = false },
                            modifier = Modifier
                                .width(with(LocalDensity.current) { anchoBotonFiltro.toDp() })
                                .background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = {
                                    servicioFiltro = null
                                    expandedFiltro = false
                                }
                            )
                            serviciosPermitidos.forEach { servicio ->
                                DropdownMenuItem(
                                    text = { Text(servicio.name) },
                                    onClick = {
                                        servicioFiltro = servicio
                                        expandedFiltro = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Tus sesiones reservadas",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (bookings.isEmpty()) {
                    Text(
                        text = "No tienes sesiones reservadas.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        val reservasFiltradas = bookings.filter { booking ->
                            val fecha = try {
                                LocalDate.parse(booking.date.substring(0, 10))
                            } catch (e: Exception) {
                                null
                            }

                            val pasaFiltroFecha = fecha != null && fecha >= today
                            val pasaFiltroServicio = servicioFiltro == null || booking.service_id == servicioFiltro!!.id

                            pasaFiltroFecha && pasaFiltroServicio
                        }
                        items(reservasFiltradas) { booking ->
                            val dateformateada = booking.date.substring(0, 10)
                            val horaformateada = booking.hour.substring(0, 5)
                            val isExpanded = menuExpandedMap[booking.id] ?: false
                            val context = LocalContext.current

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                booking.coach_profile_pic?.let { imageUrl ->
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Coach image",
                                        modifier = Modifier
                                            .size(86.dp)
                                            .padding(end = 12.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("📅 $dateformateada - 🕒 $horaformateada")
                                    Text("🧘 Servicio: ${booking.service}")
                                    Text("🏋️ Entrenador: ${booking.coach_name}")
                                }

                                Box {
                                    IconButton(onClick = {
                                        menuExpandedMap[booking.id] = true
                                    }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                    }

                                    DropdownMenu(
                                        expanded = isExpanded,
                                        onDismissRequest = { menuExpandedMap[booking.id] = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Descargar evento") },
                                            onClick = {
                                                menuExpandedMap[booking.id] = false
                                                val startDateTimeStr = LocalDateTime.parse("${dateformateada}T${horaformateada}:00")
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
                                                booking.id.let { reservaId ->
                                                    userViewModel.cancelUserBooking(reservaId)
                                                    userId?.let { userViewModel.fetchUserBookings(it) }
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
    }
    if (showReservaConfirmDialog && pendingSelectedDate != null) {
        AlertDialog(
            onDismissRequest = {
                showReservaConfirmDialog = false
                pendingSelectedDate = null
            },
            title = { Text("Ya tienes una reserva") },
            text = { Text("¿Quieres continuar? Ya tienes una reserva este día.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = pendingSelectedDate
                        showReservaDialog = true
                        tipoSesion = null
                        sesionesDiaViewModel.clearSessions()

                        servicioSeleccionadoId?.let { servicioId ->
                            sesionesDiaViewModel.fetchAvailableSessions(servicioId, pendingSelectedDate!!)
                        }

                        showReservaConfirmDialog = false
                        pendingSelectedDate = null
                    }
                ) {
                    Text("Sí, continuar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReservaConfirmDialog = false
                        pendingSelectedDate = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    // Diálogo de reserva de sesión
    if (showReservaDialog && selectedDate != null) {
        LaunchedEffect(showReservaDialog) {
            if (showReservaDialog) {
                tipoSesion = null
                servicioSeleccionadoId = null
                sesionesDiaViewModel.clearSessions()
            }
        }
        AlertDialog(
            onDismissRequest = {
                showReservaDialog = false
                user?.id?.let { userId ->
                        userViewModel.fetchUserBookings(userId)
                }
            },
            confirmButton = {},
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    var buttonWidth by remember { mutableStateOf(0) }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Selecciona el tipo de sesión:",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Botón principal
                            Text(
                                text = tipoSesion?.name ?: "Seleccionar servicio",
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFD32F2F), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                                    .clickable { dropdownExpanded = true }
                                    .onGloballyPositioned { coordinates ->
                                        buttonWidth = coordinates.size.width
                                    }
                            )

                            // Dropdown
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .width(with(LocalDensity.current) { buttonWidth.toDp() })
                                    .background(Color.White)
                            ) {
                                serviciosPermitidos.forEach { servicio ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = servicio.name,
                                                color = if (tipoSesion?.id == servicio.id) Color(0xFFD32F2F) else Color.Black,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        },
                                        onClick = {
                                            tipoSesion = servicio
                                            servicioSeleccionadoId = servicio.id
                                            dropdownExpanded = false
                                            selectedDate?.let { fecha ->
                                                sesionesDiaViewModel.fetchAvailableSessions(servicio.id, fecha)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                }
            },
            text = {
                val horariosDisponibles = if (tipoSesion == null) emptyList() else {
                    sesionesRemotas
                        .filter { it.service_id == tipoSesion?.id }
                        .map { it.hour }
                        .distinct()
                        .sorted()
                }
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
                                    val sesionesPorHora = sesionesRemotas.filter { it.hour == hora }
                                    val totalBooked = sesionesPorHora.sumOf { it.booked }
                                    val totalCapacity = sesionesPorHora.sumOf { it.capacity }
                                    val disponibilidad = if (totalCapacity > 0) {
                                        totalBooked.toFloat() / totalCapacity.toFloat()
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
                                                val serviceId = servicioSeleccionadoId ?: return@clickable
                                                if (user != null && seSuperoLimiteReserva(serviceId, selectedDate!!, weeklyLimits, unlimitedSessions, userBookings)) {
                                                    soloPuedeCambiar = true
                                                    sesionesDiaViewModel.obtenerEntrenadoresPorHora(hora)
                                                    horaSeleccionada = hora
                                                    mostrarSelectorCoach = true
                                                } else {
                                                    soloPuedeCambiar = false
                                                    sesionesDiaViewModel.obtenerEntrenadoresPorHora(hora)
                                                    horaSeleccionada = hora
                                                    mostrarSelectorCoach = true
                                                }

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

        LaunchedEffect(customerId) {
            sessionViewModel.cargarServiciosPermitidos(customerId)
            sesionesDiaViewModel.fetchUserWeeklyLimit(customerId)
        }

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
            val horaSeleccionadaFinal = horaSeleccionada!!

            LaunchedEffect(horaSeleccionadaFinal) {
                val serviceId = servicioSeleccionadoId ?: return@LaunchedEffect
                val preferredCoachId = sesionesDiaViewModel.getPreferredCoachId(customerId, serviceId)
                coachElegido = availableCoaches.firstOrNull { it.coach_id == preferredCoachId }
                    ?: availableCoaches.randomOrNull()
                mostrarBotonConfirmar = true
            }
            if (mostrarBotonConfirmar && coachElegido != null && selectedDate != null && horaSeleccionada != null) {
                AlertDialog(
                    onDismissRequest = {
                        mostrarBotonConfirmar = false
                        coachElegido = null
                        horaSeleccionada = null
                    },
                    confirmButton = {
                        if (soloPuedeCambiar) {
                            TextButton(onClick = {
                                mostrarBotonConfirmar = false
                                mostrarSelectorReservaExistente = true
                                userViewModel.fetchUserBookings(customerId)
                            }) {
                                Text("Cambiar")
                            }
                        } else {
                            Row {
                                Button(onClick = {
                                    scope.launch {
                                        try {
                                            sesionesDiaViewModel.realizarReserva(
                                                customerId = customerId,
                                                coachId = coachElegido!!.coach_id,
                                                serviceId = servicioSeleccionadoId ?: return@launch,
                                                centerId = 1,
                                                selectedDate = selectedDate.toString(),
                                                hour = horaSeleccionada!!
                                            )
                                            userViewModel.fetchUserBookings(customerId)
                                            mostrarSelectorCoach = false
                                            showReservaDialog = false
                                            mostrarBotonConfirmar = false
                                            horaSeleccionada = null
                                            coachElegido = null
                                        } catch (e: Exception) {
                                            println("❌ Error al confirmar reserva: ${e.message}")
                                        }
                                    }
                                }) {
                                    Text("Confirmar")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                TextButton(onClick = {
                                    mostrarBotonConfirmar = false
                                    mostrarSelectorReservaExistente = true
                                    userViewModel.fetchUserBookings(customerId)
                                }) {
                                    Text("Cambiar")
                                }
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            mostrarBotonConfirmar = false
                            coachElegido = null
                            horaSeleccionada = null
                        }) {
                            Text("Cancelar")
                        }
                    },
                    title = { Text("¡Confirmar reserva!") },
                    text = {
                        Column {
                            Text("Reserva con: ${coachElegido!!.coach_name}")
                            Text("Hora: ${horaSeleccionada!!.substring(0, 5)}")
                            Text("Día: ${selectedDate.toString()}")
                        }
                    }
                )
            }
            if (mostrarSelectorReservaExistente) {

                val startOfWeek = selectedDate!!.minus(selectedDate!!.dayOfWeek.ordinal, DateTimeUnit.DAY)
                val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)

                val reservasUsuario = userViewModel.userBookings
                    .collectAsState()
                    .value
                    .filter { reserva ->
                        val fechaReserva = try {
                            LocalDate.parse(reserva.date.substring(0, 10))
                        } catch (e: Exception) {
                            null
                        }

                        reserva.service_id == servicioSeleccionadoId &&
                                fechaReserva != null &&
                                fechaReserva >= today &&
                                fechaReserva in startOfWeek..endOfWeek
                    }

                AlertDialog(
                    onDismissRequest = { mostrarSelectorReservaExistente = false },
                    title = { Text("Selecciona una reserva para cambiar") },
                    text = {
                        Box(modifier = Modifier.heightIn(max = 400.dp)) {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(reservasUsuario) { reserva ->
                                    val fechaFormateada = reserva.date.substring(0, 10)
                                    val horaFormateada = reserva.hour.substring(0, 5)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                            .clickable {
                                                mostrarSelectorReservaExistente = false
                                                scope.launch {
                                                    try {
                                                        sesionesDiaViewModel.cambiarReservaSesion(
                                                            customerId = customerId,
                                                            bookingId = reserva.id,
                                                            newCoachId = coachElegido!!.coach_id,
                                                            newServiceId = servicioSeleccionadoId ?: return@launch,
                                                            newStartDate = selectedDate.toString(),
                                                            hour = horaSeleccionada!!
                                                        )
                                                        userViewModel.fetchUserBookings(customerId)
                                                        mostrarBotonConfirmar = false
                                                        mostrarSelectorCoach = false
                                                        coachElegido = null
                                                        horaSeleccionada = null
                                                        println("✅ Reserva cambiada correctamente.")
                                                    } catch (e: Exception) {
                                                        println("❌ Error al cambiar reserva: ${e.message}")
                                                    }
                                                }
                                                userViewModel.fetchUserBookings(customerId)
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        reserva.coach_profile_pic?.let { imageUrl ->
                                            AsyncImage(
                                                model = imageUrl,
                                                contentDescription = "Coach image",
                                                modifier = Modifier
                                                    .size(86.dp)
                                                    .padding(end = 12.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("📅 $fechaFormateada - 🕒 $horaFormateada")
                                            Text("🧘 Servicio: ${reserva.service}")
                                            Text("🏋️ Entrenador: ${reserva.coach_name}")
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            mostrarSelectorReservaExistente = false
                            mostrarBotonConfirmar = false
                            horaSeleccionada = null
                            coachElegido = null
                            userViewModel.fetchUserBookings(customerId)
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
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

private fun seSuperoLimiteReserva(
    serviceId: Int?,
    selectedDate: LocalDate,
    weeklyLimits: Map<Int, Int>,
    unlimitedSessions: Map<Int, Int>,
    bookings: List<com.humanperformcenter.shared.data.model.UserBooking>
): Boolean {
    val semanaInicio = selectedDate.minus(selectedDate.dayOfWeek.ordinal, kotlinx.datetime.DateTimeUnit.DAY)
    val semanaFin = semanaInicio.plus(6, kotlinx.datetime.DateTimeUnit.DAY)

    val reservasServicio = bookings.filter { it.service_id == serviceId }

    val esRecurrente = weeklyLimits.containsKey(serviceId)
    val limiteSemanal = weeklyLimits[serviceId] ?: Int.MAX_VALUE
    val totalPermitido = unlimitedSessions[serviceId] ?: Int.MAX_VALUE

    return if (esRecurrente) {
        val estaSemana = reservasServicio.count {
            val fecha = try {
                LocalDate.parse(it.date.substring(0, 10))
            } catch (e: Exception) {
                null
            }
            fecha != null && fecha in semanaInicio..semanaFin
        }
        estaSemana >= limiteSemanal
    } else {
        reservasServicio.size >= totalPermitido
    }
}



