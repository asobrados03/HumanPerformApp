package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.domain.repository.UserBookingsRepository

class UserBookingsUseCase(
    private val userBookingsRepository: UserBookingsRepository,
) {
    suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> {
        return userBookingsRepository.getUserBookings(userId)
    }

    suspend fun cancelUserBooking(bookingId: Int): Result<Unit> {
        return userBookingsRepository.cancelUserBooking(bookingId)
    }
}
