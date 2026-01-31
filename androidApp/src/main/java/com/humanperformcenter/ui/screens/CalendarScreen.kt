package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.presentation.ui.FetchUserBookingsState
import com.humanperformcenter.shared.presentation.viewmodel.DaySessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import com.humanperformcenter.ui.components.CalendarGrid
import com.humanperformcenter.ui.components.CalendarHeader
import com.humanperformcenter.ui.components.CalendarWeekDays
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.ui.components.UserBookingsSection
import com.humanperformcenter.ui.components.reservationFlowDialogs
import com.humanperformcenter.worker.scheduleSessionNotification
import java.time.LocalDate
import java.time.Month

@Composable
fun CalendarScreen(
    navController: NavHostController,
    daySessionViewModel: DaySessionViewModel,
    serviceProductViewModel: ServiceProductViewModel,
    userViewModel: UserViewModel,
    onPlaySound: (Int) -> Unit
) {
    // 1. Fecha actual usando java.time
    val today = remember { LocalDate.now() }

    // Estados de navegación
    var displayedMonth by remember { mutableStateOf(today.month) }
    var displayedYear by remember { mutableIntStateOf(today.year) }

    val user by userViewModel.userData.collectAsStateWithLifecycle()
    val userBookings by userViewModel.userBookings.collectAsStateWithLifecycle()
    val userId = user?.id

    val context = LocalContext.current

    // Carga inicial (Sin cambios en lógica, solo triggers)
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
        if (userBookings is FetchUserBookingsState.Success) {
            val bookings = (userBookings as FetchUserBookingsState.Success).bookings
            bookings.forEach { booking ->
                // Asegúrate que esta función interna use java.time para parsear
                scheduleSessionNotification(context, booking)
            }
        }
    }

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
            // 2. Cabecera con lógica de meses simplificada
            CalendarHeader(
                displayedMonth = displayedMonth,
                displayedYear = displayedYear,
                onPreviousMonth = {
                    if (displayedMonth == Month.JANUARY) {
                        displayedYear -= 1
                    }
                    // minus(1) de java.time.Month maneja el ciclo de forma segura
                    displayedMonth = displayedMonth.minus(1)
                },
                onNextMonth = {
                    if (displayedMonth == Month.DECEMBER) {
                        displayedYear += 1
                    }
                    displayedMonth = displayedMonth.plus(1)
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
                onDayClicked = onDayClicked
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Sección inferior de estados
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                when (val state = userBookings) {
                    is FetchUserBookingsState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is FetchUserBookingsState.Error -> {
                        Text(
                            text = "Error al cargar: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is FetchUserBookingsState.Success -> {
                        UserBookingsSection(
                            userViewModel = userViewModel,
                            serviceProductViewModel = serviceProductViewModel,
                            userBookings = state.bookings,
                            userId = userId
                        )
                    }
                }
            }
        }
    }
}