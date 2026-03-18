package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.domain.repository.UserAccountRepository
import com.humanperformcenter.shared.domain.repository.UserBookingsRepository
import com.humanperformcenter.shared.domain.repository.UserCouponsRepository
import com.humanperformcenter.shared.domain.repository.UserDocumentsRepository
import com.humanperformcenter.shared.domain.repository.UserFavoritesRepository
import com.humanperformcenter.shared.domain.repository.UserProfileRepository
import com.humanperformcenter.shared.domain.repository.UserStatsRepository
import com.humanperformcenter.shared.domain.repository.UserWalletRepository

class UserUseCase(
    private val userProfileRepository: UserProfileRepository,
    private val userAccountRepository: UserAccountRepository,
    private val userFavoritesRepository: UserFavoritesRepository,
    private val userBookingsRepository: UserBookingsRepository,
    private val userStatsRepository: UserStatsRepository,
    private val userCouponsRepository: UserCouponsRepository,
    private val userDocumentsRepository: UserDocumentsRepository,
    private val userWalletRepository: UserWalletRepository,
) {
    suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> {
        return userProfileRepository.updateUser(user, profilePicBytes)
    }

    suspend fun getUserById(id: Int): Result<User> {
        return userProfileRepository.getUserById(id)
    }

    suspend fun deleteUser(email: String): Result<Unit> {
        return userAccountRepository.deleteUser(email)
    }

    suspend fun getCoaches(): Result<List<Professional>> {
        return userFavoritesRepository.getCoaches()
    }

    suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String> {
        return userFavoritesRepository.markFavorite(coachId, serviceName, userId)
    }

    suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> {
        return userProfileRepository.deleteProfilePic(req)
    }

    suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> {
        return userBookingsRepository.getUserBookings(userId)
    }

    suspend fun cancelUserBooking(bookingId: Int): Result<Unit> {
        return userBookingsRepository.cancelUserBooking(bookingId)
    }

    suspend fun getUserStats(customerId: Int): Result<UserStatistics> {
        return userStatsRepository.getUserStats(customerId)
    }

    /**
     * Añade un código de cupón al perfil del usuario.
     * El repo devuelve Result<Unit> indicando éxito o fallo.
     */
    suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> {
        return userCouponsRepository.addCouponToUser(userId, couponCode)
    }

    /**
     * Recupera el cupón activo del usuario, o null si no tiene.
     */
    suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> {
        return userCouponsRepository.getUserCoupons(userId)
    }

    suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String> {
        return userDocumentsRepository.uploadDocument(userId, name, data)
    }

    suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> {
        return userFavoritesRepository.getPreferredCoach(customerId)
    }

    suspend fun getEwalletBalance(userId: Int): Result<Double?> {
        return userWalletRepository.getEwalletBalance(userId)
    }

    suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>> {
        return userWalletRepository.getEwalletTransactions(userId)
    }
}