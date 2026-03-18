package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.SessionNotificationManager
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.usecase.BookingsUseCase
import com.humanperformcenter.shared.presentation.ui.FetchUserBookingsState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserBookingsViewModel(
    private val bookingsUseCase: BookingsUseCase,
    private val notificationManager: SessionNotificationManager
) : ViewModel() {
    private val _userBookings = MutableStateFlow<FetchUserBookingsState>(FetchUserBookingsState.Loading)
    @NativeCoroutinesState
    val userBookings: StateFlow<FetchUserBookingsState> = _userBookings

    fun fetchUserBookings(userId: Int) {
        viewModelScope.launch {
            _userBookings.value = FetchUserBookingsState.Loading

            bookingsUseCase.getUserBookings(userId).onSuccess { bookings ->
                _userBookings.value = FetchUserBookingsState.Success(bookings)
            }.onFailure { exception ->
                _userBookings.value = FetchUserBookingsState.Error(
                    exception.message ?: "Ocurrió un error inesperado"
                )
            }
        }
    }

    fun cancelUserBooking(bookingId: Int, currentUser: User?) {
        viewModelScope.launch {
            bookingsUseCase.cancelUserBooking(bookingId).fold(
                onSuccess = {
                    notificationManager.cancelNotification(bookingId)
                    fetchUserBookings(currentUser?.id ?: 0)
                },
                onFailure = { throwable ->
                    println("❌ Error al cancelar reserva: ${throwable.message}")
                }
            )
        }
    }
}
