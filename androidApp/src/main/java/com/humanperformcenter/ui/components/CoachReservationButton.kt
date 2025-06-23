package com.humanperformcenter.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
fun CoachReservationButton(
    sessionViewModel: SessionViewModel,
    coachId: Int,
    coachName: String,
    coachCenterId: Int,
    serviceId: Int,
    selectedDate: LocalDate,
    hour: String,
    onSuccess: () -> Unit
) {
    val accessToken by sessionViewModel.accessToken.collectAsState()
    val customerId by sessionViewModel.userId.collectAsState()
    val scope = rememberCoroutineScope()

    if (accessToken != null && customerId != null) {
        androidx.compose.material3.Button(
            onClick = {
                scope.launch {
                    /*sessionViewModel.realizarReserva(
                        coachId = coachId,
                        hour = hour,
                        serviceId = serviceId,
                        selectedDate = selectedDate,
                        coachCenterId = coachCenterId
                    )*/
                    onSuccess()
                }
            }
        ) {
            androidx.compose.material3.Text("Reservar con $coachName")
        }
    }
}