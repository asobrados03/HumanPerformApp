package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.ServicioDispo
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.model.UserBooking
import com.humanperformcenter.shared.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class UserUseCase(private val userRepository: UserRepository) {
    suspend fun updateUser(user: User): Result<User> = withContext(Dispatchers.IO) {
        return@withContext userRepository.updateUser(user)
    }

    suspend fun deleteUser(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext userRepository.deleteUser(email)
    }
    suspend fun getUserAllowedServices(customerId: Int): List<ServicioDispo> = withContext(Dispatchers.IO) {
        return@withContext userRepository.getUserAllowedServices(customerId)
    }
    suspend fun getUserBookings(customerId: Int): List<UserBooking> = withContext(Dispatchers.IO) {
        return@withContext userRepository.getUserBookings(customerId)
    }
    suspend fun deleteUserBooking(bookingId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext userRepository.deleteUserBooking(bookingId)
    }
}