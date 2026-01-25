package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.user.UserBooking

sealed interface FetchUserBookingsState {
    data object Loading : FetchUserBookingsState
    data class Success(val bookings: List<UserBooking>) : FetchUserBookingsState
    data class Error(val message: String) : FetchUserBookingsState
}
