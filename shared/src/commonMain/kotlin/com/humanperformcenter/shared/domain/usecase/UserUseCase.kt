package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.domain.repository.UserRepository

class UserUseCase(private val userRepository: UserRepository) {
    suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> {
        return userRepository.updateUser(user, profilePicBytes)
    }

    suspend fun getUserById(id: Int): Result<User> {
        return userRepository.getUserById(id)
    }

    suspend fun deleteUser(email: String): Result<Unit> {
        return userRepository.deleteUser(email)
    }

    suspend fun getCoaches(): Result<List<Professional>> {
        return userRepository.getCoaches()
    }

    suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String> {
        return userRepository.markFavorite(coachId, serviceName, userId)
    }

    suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> {
        return userRepository.deleteProfilePic(req)
    }

    suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> {
        return userRepository.getUserBookings(userId)
    }

    suspend fun cancelUserBooking(bookingId: Int): Result<Unit> {
        return userRepository.cancelUserBooking(bookingId)
    }

    suspend fun getUserStats(customerId: Int): Result<UserStatistics> {
        return userRepository.getUserStats(customerId)
    }

    /**
     * Añade un código de cupón al perfil del usuario.
     * El repo devuelve Result<Unit> indicando éxito o fallo.
     */
    suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> {
        return userRepository.addCouponToUser(userId, couponCode)
    }

    /**
     * Recupera el cupón activo del usuario, o null si no tiene.
     */
    suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> {
        return userRepository.getUserCoupons(userId)
    }

    suspend fun uploadDocument(name: String, data: ByteArray): Result<String> {
        return userRepository.uploadDocument(name, data)
    }

    suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> {
        return userRepository.getPreferredCoach(customerId)
    }

    suspend fun getEwalletBalance(userId: Int): Result<Double?> {
        return userRepository.getEwalletBalance(userId)
    }

    suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>> {
        return userRepository.getEwalletTransactions(userId)
    }
}