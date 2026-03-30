package com.humanperformcenter.shared.data.repository

import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.data.remote.UserBookingsRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserBookingsRepository

class UserBookingsRepositoryImpl(
    private val remote: UserBookingsRemoteDataSource,
) : UserBookingsRepository {
    override suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> = remote.getUserBookings(userId)
    override suspend fun cancelUserBooking(bookingId: Int): Result<Unit> = remote.cancelUserBooking(bookingId)
}
