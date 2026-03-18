package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.user.UserBooking

interface UserBookingsRepository {
    suspend fun getUserBookings(userId: Int): Result<List<UserBooking>>
    suspend fun cancelUserBooking(bookingId: Int): Result<Unit>
}
