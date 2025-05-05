package es.uva.sg.psm.planificadorfinanciero

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import es.uva.sg.psm.planificadorfinanciero.data.Budget
import es.uva.sg.psm.planificadorfinanciero.data.Category
import es.uva.sg.psm.planificadorfinanciero.data.Transaction
import es.uva.sg.psm.planificadorfinanciero.data.TransactionType
import es.uva.sg.psm.planificadorfinanciero.viewModels.BudgetViewModel
import es.uva.sg.psm.planificadorfinanciero.viewModels.CategoryViewModel
import es.uva.sg.psm.planificadorfinanciero.viewModels.TransactionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import es.uva.sg.psm.planificadorfinanciero.ui.components.MostrarDialogoReserva


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    budgetViewModel: BudgetViewModel,
    onPlaySound: (Int) -> Unit
) {
    // Estado para el diálogo
    var showDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    // Scope para llamadas asincronas
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppBarView(
                title = "Transacciones",
                showBackArrow = false
            ) { navController.navigateUp() }
        },
        bottomBar = { NavigationBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues()),
        content = { paddingValues ->
            val transactionList = transactionViewModel.getAllTransactions
                .collectAsState(initial = emptyList())

            if (transactionList.value.isEmpty()) {
                Text(
                    text = "No hay transacciones disponibles.",
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                )
            }

            // Procesar transacciones para el calendario y selección de día
            var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
            // Estados para mostrar el diálogo de reserva y la disponibilidad
            var showReservaDialog by remember { mutableStateOf(false) }
            var disponibilidad by remember { mutableStateOf<Map<Int, List<String>>>(emptyMap()) }
            val todayInstant = Clock.System.now()
            val todayLocalDate = todayInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val currentYear = todayLocalDate.year
            val currentMonth = todayLocalDate.month

            // Estados para mes y año mostrados (navegación)
            var displayedMonth by remember { mutableStateOf(currentMonth) }
            var displayedYear by remember { mutableStateOf(currentYear) }

            // Map LocalDate -> Color (green for ingresos, red for gastos)
            val dayColorMap = remember(transactionList.value, displayedMonth, displayedYear) {
                val map = mutableMapOf<LocalDate, Color>()
                transactionList.value.forEach { transaction ->
                    val instant = Instant.fromEpochMilliseconds(transaction.date)
                    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    if (localDate.year == displayedYear && localDate.month == displayedMonth) {
                        val existingColor = map[localDate]
                        val newColor = if (transaction.type == TransactionType.Ingreso) Color(0xFF4CAF50) else Color(0xFFF44336)
                        // If already exists a color, prioritize red if any expense present
                        if (existingColor == null) {
                            map[localDate] = newColor
                        } else if (existingColor == Color(0xFF4CAF50) && newColor == Color(0xFFF44336)) {
                            map[localDate] = newColor
                        }
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
                    Text(
                        text = "◀️",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clickable {
                                // Ir al mes anterior
                                val prevMonthNumber = displayedMonth.ordinal
                                if (prevMonthNumber < 1) {
                                    displayedMonth = Month.DECEMBER
                                    displayedYear -= 1
                                } else {
                                    displayedMonth = Month.values()[prevMonthNumber - 1]
                                }
                            }
                            .padding(end = 8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Nombre del mes y año
                    Text(
                        text = "${displayedMonth.name.lowercase().replaceFirstChar { it.uppercase() }} $displayedYear",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Botón mes siguiente
                    Text(
                        text = "▶️",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clickable {
                                // Ir al mes siguiente
                                val nextMonthNumber = displayedMonth.ordinal + 2
                                if (nextMonthNumber > 12) {
                                    displayedMonth = Month.JANUARY
                                    displayedYear += 1
                                } else {
                                    displayedMonth = Month.values()[nextMonthNumber - 1]
                                }
                            }
                            .padding(start = 8.dp)
                    )
                }

                // Mostrar días de la semana (lunes a resumen)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Aseguramos el orden correcto: lunes a domingo, pero la última columna es "Res"
                    listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Res").forEach { dayName ->
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

                // Simulación de reservas por semana (por ejemplo, para 5 semanas)
                val reservasPorSemana = listOf(0, 1, 2, 0, 1, 2) // Puedes ajustar el tamaño según weeks

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
                                // Empty cell
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                )
                            } else {
                                val date = LocalDate(displayedYear, displayedMonth, dayNumber)
                                val eventColor = dayColorMap[date] ?: Color.Transparent
                                val isSelected = selectedDate == date
                                // Sombrear fines de semana si no hay evento (color)
                                val isWeekend = (dayIndex == 5 || dayIndex == 6)
                                // Color de fondo de celda: prioridad evento, luego sombreado fin de semana, si no transparente
                                val cellBackgroundColor = when {
                                    eventColor != Color.Transparent -> eventColor
                                    isWeekend -> Color.LightGray
                                    else -> Color.Transparent
                                }
                                val textColor = if (eventColor != Color.Transparent) Color.White else MaterialTheme.colorScheme.onSurface
                                val borderModifier =
                                    if (isSelected)
                                        Modifier.background(color = cellBackgroundColor, shape = RoundedCornerShape(50))
                                            .then(
                                                Modifier
                                                    .background(
                                                        color = Color.Transparent,
                                                        shape = RoundedCornerShape(50)
                                                    )
                                            )
                                    else Modifier.background(color = cellBackgroundColor, shape = RoundedCornerShape(50))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .then(
                                            if (isSelected)
                                                Modifier
                                                    .background(
                                                        color = cellBackgroundColor,
                                                        shape = RoundedCornerShape(50)
                                                    )
                                                    .border(
                                                        width = 2.dp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = RoundedCornerShape(50)
                                                    )
                                            else Modifier
                                                .background(color = cellBackgroundColor, shape = RoundedCornerShape(50))
                                        )
                                        .clickable {
                                            // Si el día es seleccionable
                                            if (true) { // Aquí puedes poner lógica para filtrar días válidos
                                                selectedDate = if (selectedDate == date) null else date
                                                // Si se selecciona (no se deselecciona)
                                                if (selectedDate == date) {
                                                    // Simula llamada a API para obtener disponibilidad
                                                    // Sustituye esto por la llamada real a tu API Flask
                                                    disponibilidad = mapOf(
                                                        1 to listOf("10:00", "11:00", "12:00"),
                                                        2 to listOf("09:30", "10:30", "13:00")
                                                    )
                                                    showReservaDialog = true
                                                }
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
                        // Columna "Res" vacía en el grid de calendario
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        )
                    }
                }

                // Fila de resumen de reservas por semana bajo el calendario
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayIndex in 0..5) {
                        // Celdas vacías para los días (no mostrar nada)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            contentAlignment = Alignment.Center
                        ) {}
                    }
                    // Columna "Res": mostrar reserva semanal
                    for (week in 0 until numWeeks) {
                        // Solo la última columna de cada fila de semana
                        // Pero como solo hay una fila de resumen, mostramos los valores por semana en la última columna de cada fila
                        // Para mostrar solo una columna, lo hacemos así:
                        // Pero necesitamos una fila por semana, así que repetimos la fila.
                        // Pero aquí, mostramos todos los resúmenes en una sola fila, solo en la última columna de cada semana.
                        // En vez de esto, debemos mostrar solo una celda por semana, en la última columna.
                        // Así que, en vez de for (week in 0 until numWeeks) aquí, lo hacemos abajo del grid, una fila, y en la última columna de cada semana, el resumen.
                        // Pero la instrucción era: debajo de cada fila semanal, una celda en la columna "Res".
                        // Pero aquí, debajo del grid, una fila, y en la última columna de cada semana, el resumen.
                        // Pero la instrucción dice: debajo del calendario, una fila, y en la columna "Res" de cada semana, el resumen.
                        // Así que la fila tiene tantas columnas como semanas, y cada celda es el resumen de la semana correspondiente, alineado en la columna "Res".
                        // Pero visualmente, en el calendario, la columna "Res" es la séptima, así que en la fila de resumen, cada celda está en la séptima posición de cada semana.
                        // Así que, para cada semana, ponemos celdas vacías para las primeras seis columnas, y el resumen en la séptima.
                        // Por lo tanto, debajo del grid, para cada semana, una fila con seis celdas vacías y el resumen en la última.
                    }
                }
                // Debajo del grid, una fila por cada semana, con el resumen en la última columna
                for (week in 0 until numWeeks) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (dayIndex in 0..5) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                            )
                        }
                        // Celda de resumen de reservas para la semana
                        val reservas = reservasPorSemana.getOrNull(week) ?: 0
                        val color = when (reservas) {
                            0 -> Color(0xFFF44336) // rojo
                            1 -> Color(0xFFFFC107) // amarillo
                            2 -> Color(0xFF4CAF50) // verde
                            else -> Color.Gray
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${reservas}/2",
                                color = color,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .background(
                                        color = color.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = color,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtrar las transacciones por el día seleccionado
                val filteredTransactions = if (selectedDate != null) {
                    transactionList.value.filter { transaction ->
                        val instant = Instant.fromEpochMilliseconds(transaction.date)
                        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                        localDate == selectedDate
                    }
                } else {
                    // Solo mostrar transacciones del mes/año mostrado
                    transactionList.value.filter { transaction ->
                        val instant = Instant.fromEpochMilliseconds(transaction.date)
                        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                        localDate.year == displayedYear && localDate.month == displayedMonth
                    }
                }

                // Mostrar el diálogo de reserva si corresponde
                if (showReservaDialog && selectedDate != null) {
                    MostrarDialogoReserva(
                        fecha = selectedDate!!, // Aquí selectedDate ya está validado como no nulo
                        disponibilidad = disponibilidad,
                        onClose = { showReservaDialog = false },
                        onReservar = { hora, centroId ->
                            // Lógica al reservar
                            showReservaDialog = false
                        }
                    )
                }

                // Columna eficiente que muestra la lista de transacciones añadidas al sistema
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTransactions, key = { transaction -> transaction.id }) { transaction ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                                    transactionToDelete = transaction
                                    showDialog = true
                                    false // Impide la eliminación automática hasta confirmar
                                } else false
                            }
                        )

                        //Este elemento permite eliminar y editar las transacciones de la lista
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
                                TransactionItem(transaction = transaction, categoryViewModel = categoryViewModel) {
                                    val id = transaction.id
                                    navController.navigate(Screen.AddEditTransactionScreen.route + "/$id")
                                }
                            }
                        )
                    }
                }
            }
        }
    )
    // Diálogo de confirmación
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¿Desea eliminar esta transacción?") },
            text = {
                Text(
                    "Esta acción conlleva que esta transacción y toda la información relacionada con ella se elimine permanentemente de la aplicación."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val posibleBudget =
                                checkIfExistBudgetAssociatedWithTransaction(
                                    budgetViewModel,
                                    transactionToDelete
                                )
                            if (posibleBudget != null) {
                                val amountOfTransactionToDelete = transactionToDelete?.amount

                                val currentExpenditureAfterDeleteTransaction =
                                    posibleBudget.currentExpenditure - amountOfTransactionToDelete!!

                                val budget = Budget(
                                    posibleBudget.id,
                                    posibleBudget.category,
                                    posibleBudget.monthlyLimit,
                                    currentExpenditureAfterDeleteTransaction,
                                    posibleBudget.month,
                                    posibleBudget.year
                                )

                                budgetViewModel.updateBudget(budget)
                            }
                        }

                        transactionToDelete?.let { transactionViewModel.deleteTransactionAndCheckCategory(it) }
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



suspend fun checkIfExistBudgetAssociatedWithTransaction(
    budgetViewModel: BudgetViewModel,
    transactionToDelete: Transaction?
): Budget? {
    // Convertir el timestamp (la fecha de la transacción) a un objeto Instant
    val instant = transactionToDelete?.date?.let {
        Instant.fromEpochMilliseconds(it)
    }

    // Convertir Instant a LocalDateTime usando una zona horaria
    val localDateTime =
        instant?.toLocalDateTime(TimeZone.currentSystemDefault())

    // Extraer el mes y el año
    val month: Int = localDateTime?.monthNumber ?: 0
    val monthFormatted = month.toString()
        .padStart(2, '0')

    val year: Int = localDateTime?.year ?: 0

    // Conseguimos el posible presupuesto en el caso de que haya uno
    val posibleBudget = transactionToDelete?.category?.let {
        budgetViewModel
            .getBudgetForCategoryMonthAndYear(
                it,
                monthFormatted,
                year
            ).first()
    }

    return posibleBudget
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    categoryViewModel: CategoryViewModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp) // Mejorado margen externo
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { // Mejorado padding interno
            // Filas horizontales para la descripción y el importe
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.description,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Clip,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = if (transaction.type.name == "Ingreso") "${transaction.amount}€" else "-${transaction.amount}€",
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type.name == "Ingreso") {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Información adicional debajo
            val categoryFlow = categoryViewModel.getCategoryById(transaction.category)

            val categoryState = categoryFlow.collectAsState(initial = Category(0L, "", TransactionType.Ingreso))

            val currentCategory = categoryState.value

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Categoría: " + currentCategory.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = convertTimestampToString(transaction.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

