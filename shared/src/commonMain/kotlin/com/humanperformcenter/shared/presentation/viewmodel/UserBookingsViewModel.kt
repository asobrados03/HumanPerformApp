package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.SessionNotificationManager
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.usecase.UserBookingsUseCase
import com.humanperformcenter.shared.presentation.ui.FetchUserBookingsState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserBookingsViewModel(
    private val userBookingsUseCase: UserBookingsUseCase,
    private val notificationManager: SessionNotificationManager
) : ViewModel() {
    private val _userBookings = MutableStateFlow<FetchUserBookingsState>(FetchUserBookingsState.Loading)
    @NativeCoroutinesState
    val userBookings: StateFlow<FetchUserBookingsState> = _userBookings

    fun fetchUserBookings(userId: Int) {
        viewModelScope.launch {
            _userBookings.value = FetchUserBookingsState.Loading

            userBookingsUseCase.getUserBookings(userId).onSuccess { bookings ->
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
            userBookingsUseCase.cancelUserBooking(bookingId).fold(
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
