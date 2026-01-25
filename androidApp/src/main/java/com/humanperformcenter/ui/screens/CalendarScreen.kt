package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.presentation.ui.FetchUserBookingsState
import com.humanperformcenter.ui.components.CalendarGrid
import com.humanperformcenter.ui.components.CalendarHeader
import com.humanperformcenter.ui.components.CalendarWeekDays
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.ui.components.UserBookingsSection
import com.humanperformcenter.ui.components.reservationFlowDialogs
import com.humanperformcenter.ui.viewmodel.DaySessionViewModel
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModel
import com.humanperformcenter.worker.scheduleSessionNotification
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun CalendarScreen(
    navController: NavHostController,
    daySessionViewModel: DaySessionViewModel,
    serviceProductViewModel: ServiceProductViewModel,
    userViewModel: UserViewModel,
    onPlaySound: (Int) -> Unit
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var displayedMonth by remember { mutableStateOf(today.month) }
    var displayedYear by remember { mutableIntStateOf(today.year) }

    val user by userViewModel.userData.collectAsStateWithLifecycle()
    val userBookings by userViewModel.userBookings.collectAsStateWithLifecycle()
    val userId = user?.id

    val context = LocalContext.current

    // Carga inicial de datos
    LaunchedEffect(userId) {
        if (userId != null && userId != -1) {
            if (userBookings !is FetchUserBookingsState.Success) {
                userViewModel.fetchUserBookings(userId)
            }
            daySessionViewModel.fetchUserWeeklyLimit(userId)
            daySessionViewModel.fetchHolidays()
        }
    }

    // Programar notificaciones
    LaunchedEffect(userBookings) {
        // 1. Verificamos que el estado sea Success
        if (userBookings is FetchUserBookingsState.Success) {
            // 2. Extraemos la lista del estado
            val bookings = (userBookings as FetchUserBookingsState.Success).bookings

            bookings.forEach { booking ->
                scheduleSessionNotification(context, booking)
            }
        }
    }

    // Obtenemos el callback que maneja todo el flujo de reserva
    val onDayClicked = reservationFlowDialogs(
        daySessionViewModel = daySessionViewModel,
        serviceProductViewModel = serviceProductViewModel,
        userViewModel = userViewModel
    )

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = false,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        bottomBar = { NavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CalendarHeader(
                displayedMonth = displayedMonth,
                displayedYear = displayedYear,
                onPreviousMonth = {
                    if (displayedMonth == Month.JANUARY) {
                        displayedMonth = Month.DECEMBER
                        displayedYear -= 1
                    } else {
                        displayedMonth = Month.entries[displayedMonth.ordinal - 1]
                    }
                },
                onNextMonth = {
                    if (displayedMonth == Month.DECEMBER) {
                        displayedMonth = Month.JANUARY
                        displayedYear += 1
                    } else {
                        displayedMonth = Month.entries[displayedMonth.ordinal + 1]
                    }
                }
            )

            CalendarWeekDays()

            val bookingsList = (userBookings as? FetchUserBookingsState.Success)?.bookings ?: emptyList()

            CalendarGrid(
                displayedMonth = displayedMonth,
                displayedYear = displayedYear,
                today = today,
                daySessionViewModel = daySessionViewModel,
                userBookings = bookingsList,
                onDayClicked = onDayClicked  // ← Directamente el callback devuelto
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = userBookings) {
                is FetchUserBookingsState.Loading -> {
                    CircularProgressIndicator()
                }
                is FetchUserBookingsState.Error -> {
                    Text("Error al cargar: ${state.message}")
                }
                is FetchUserBookingsState.Success -> {
                    UserBookingsSection(
                        userViewModel = userViewModel,
                        serviceProductViewModel = serviceProductViewModel,
                        userBookings = state.bookings,
                        userId = user?.id
                    )
                }
            }
        }
    }
}