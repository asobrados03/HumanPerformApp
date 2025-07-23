package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.UserStatistics
import com.humanperformcenter.shared.data.model.Professional
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.model.UserBooking
import com.humanperformcenter.shared.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class UserUseCase(private val userRepository: UserRepository) {
    suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> = withContext(Dispatchers.IO) {
        return@withContext userRepository.updateUser(user, profilePicBytes)
    }

    suspend fun deleteUser(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext userRepository.deleteUser(email)
    }

    suspend fun getCoaches(): Result<List<Professional>> = withContext(Dispatchers.IO) {
        return@withContext userRepository.getCoaches()
    }

    suspend fun markFavorite(
        coachId: Int, serviceName: String?, userId: Int?
    ): Result<String> = withContext(Dispatchers.IO) {
            return@withContext userRepository.markFavorite(coachId, serviceName, userId)
        }

    suspend fun deleteProfilePic(req: DeleteProfilePicRequest) = withContext(Dispatchers.IO) {
        return@withContext userRepository.deleteProfilePic(req)
    }

    suspend fun getUserAllowedServices(customerId: Int): List<ServiceAvailable> = withContext(Dispatchers.IO) {
        return@withContext userRepository.getUserAllowedServices(customerId)
    }

    suspend fun getUserBookings(customerId: Int): List<UserBooking> = withContext(Dispatchers.IO) {
        return@withContext userRepository.getUserBookings(customerId)
    }

    suspend fun cancelUserBooking(bookingId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext userRepository.cancelUserBooking(bookingId)
    }

    suspend fun getUserStats(customerId: Int): UserStatistics = withContext(Dispatchers.IO) {
        return@withContext userRepository.getUserStats(customerId)
    }
}