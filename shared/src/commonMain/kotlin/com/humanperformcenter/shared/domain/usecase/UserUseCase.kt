package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.Coupon
import com.humanperformcenter.shared.data.model.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.EwalletTransaction
import com.humanperformcenter.shared.data.model.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.Professional
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.model.UserBooking
import com.humanperformcenter.shared.data.model.UserStatistics
import com.humanperformcenter.shared.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserUseCase(private val userRepository: UserRepository) {
    suspend fun updateUser(user: User, profilePicBytes: ByteArray?): User {
        return userRepository.updateUser(user, profilePicBytes)
    }

    suspend fun getUserById(id: Int): Result<User> {
        return userRepository.getUserById(id)
    }

    suspend fun deleteUser(email: String): Result<Unit> {
        return userRepository.deleteUser(email)
    }


    // 1. Devuelve directamente la lista de coaches
    suspend fun getCoachesRaw(): List<Professional> = withContext(Dispatchers.IO) {
        val result = userRepository.getCoaches()
        result.getOrThrow() // lanza excepción si es Result.Failure
    }

    // 2. Marca favorito y devuelve el mensaje
    suspend fun markFavoriteRaw(
        coachId: Int,
        serviceName: String?,
        userId: Int?
    ): String = withContext(Dispatchers.IO) {
        val result = userRepository.markFavorite(coachId, serviceName, userId)
        result.getOrThrow()
    }

    // 3. Devuelve directamente el preferred coach
    suspend fun getPreferredCoachRaw(customerId: Int): GetPreferredCoachResponse =
        withContext(Dispatchers.IO) {
            val result = userRepository.getPreferredCoach(customerId)
            result.getOrThrow()
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

    suspend fun getUserBookings(customerId: Int): List<UserBooking> {
        return userRepository.getUserBookings(customerId)
    }

    suspend fun cancelUserBooking(bookingId: Int): Result<Unit> {
        return userRepository.cancelUserBooking(bookingId)
    }

    suspend fun getUserStats(customerId: Int): UserStatistics {
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

    /**
     * Métodos auxiliares para iOS que evitan exponer `Result` en Swift.
     */
    fun addCouponToUserForIos(
        userId: Int,
        couponCode: String,
        completion: (Boolean, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = addCouponToUser(userId, couponCode)
            result
                .onSuccess { completion(true, null) }
                .onFailure { error -> completion(false, error.message) }
        }
    }

    fun getUserCouponsForIos(
        userId: Int,
        completion: (List<Coupon>?, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = getUserCoupons(userId)
            result
                .onSuccess { coupons -> completion(coupons, null) }
                .onFailure { error -> completion(emptyList(), error.message) }
        }
    }

    suspend fun uploadDocument(name: String, data: ByteArray): Result<String> {
        return userRepository.uploadDocument(name, data)
    }

    suspend fun uploadDocumentRaw(
        name: String,
        data: ByteArray
    ): String = withContext(Dispatchers.IO) {
        userRepository.uploadDocument(name, data).getOrThrow()
    }


    suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> {
        return userRepository.getPreferredCoach(customerId)
    }

    suspend fun getEwalletBalance(userId: Int): Result<Double?> {
        return userRepository.getEwalletBalance(userId)
    }

    fun getEwalletBalanceForIos(
        userId: Int,
        completion: (Double?, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = getEwalletBalance(userId)
            result
                .onSuccess { balance -> completion(balance ?: 0.0, null) } // <-- devuelve Double nativo
                .onFailure { error -> completion(null, error.message) }
        }
    }

    suspend fun getEwalletTransactions(userId: Int): List<EwalletTransaction> {
        return userRepository.getEwalletTransactions(userId)
    }
}