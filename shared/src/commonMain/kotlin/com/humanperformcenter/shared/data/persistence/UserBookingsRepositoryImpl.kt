package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.data.remote.UserBookingsRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserBookingsRepository

class UserBookingsRepositoryImpl(
    private val remoteDataSource: UserBookingsRemoteDataSource,
) : UserBookingsRepository {
    override suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> = remoteDataSource.getUserBookings(userId)
    override suspend fun cancelUserBooking(bookingId: Int): Result<Unit> = remoteDataSource.cancelUserBooking(bookingId)
}
