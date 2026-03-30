package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.model.user.UserBooking

interface UserBookingsRemoteDataSource {
    suspend fun getUserBookings(userId: Int): Result<List<UserBooking>>
    suspend fun cancelUserBooking(bookingId: Int): Result<Unit>
}
