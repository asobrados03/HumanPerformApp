package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.ui.components.CalendarGrid
import com.humanperformcenter.ui.components.CalendarHeader
import com.humanperformcenter.ui.components.CalendarWeekDays
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.ui.components.UserBookingsSection
import com.humanperformcenter.ui.components.reservationFlowDialogs  // ← Nota el nombre en minúscula
import com.humanperformcenter.ui.viewmodel.DaySessionViewModel
import com.humanperformcenter.ui.viewmodel.SessionViewModel
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
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel,
    onPlaySound: (Int) -> Unit
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var displayedMonth by remember { mutableStateOf(today.month) }
    var displayedYear by remember { mutableIntStateOf(today.year) }

    val user by userViewModel.userData.collectAsState()
    val userBookings by userViewModel.userBookings.collectAsState()
    val userId = user?.id

    val context = LocalContext.current

    // Carga inicial de datos
    LaunchedEffect(userId) {
        userId?.let {
            userViewModel.fetchUserBookings(it)
            daySessionViewModel.fetchUserWeeklyLimit(it)
            sessionViewModel.cargarServiciosPermitidos(it)
            daySessionViewModel.fetchHolidays()
        }
    }

    // Programar notificaciones
    LaunchedEffect(userBookings) {
        userBookings.forEach { booking ->
            scheduleSessionNotification(context, booking)
        }
    }

    // Obtenemos el callback que maneja todo el flujo de reserva
    val onDayClicked = reservationFlowDialogs(
        daySessionViewModel = daySessionViewModel,
        sessionViewModel = sessionViewModel,
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

            CalendarGrid(
                displayedMonth = displayedMonth,
                displayedYear = displayedYear,
                today = today,
                daySessionViewModel = daySessionViewModel,
                userBookings = userBookings,
                onDayClicked = onDayClicked  // ← Directamente el callback devuelto
            )

            Spacer(modifier = Modifier.height(16.dp))

            UserBookingsSection(
                userViewModel = userViewModel,
                sessionViewModel = sessionViewModel,
                userBookings = userBookings,
                userId = userId
            )
        }
    }
}