package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.data.model.user.UserStatistics

interface UserRepository {
    suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User>
    suspend fun getUserById(id: Int): Result<User>
    suspend fun deleteUser(email: String): Result<Unit>
    suspend fun getCoaches(): Result<List<Professional>>
    suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String>
    suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse>
    suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit>
    suspend fun getUserBookings(userId: Int): Result<List<UserBooking>>
    suspend fun cancelUserBooking(bookingId: Int): Result<Unit>
    suspend fun getUserStats(customerId: Int): Result<UserStatistics>
    suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit>
    suspend fun getUserCoupons(userId: Int): Result<List<Coupon>>
    suspend fun uploadDocument(name: String, data: ByteArray): Result<String>
    suspend fun getEwalletBalance(userId: Int): Result<Double?>
    suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>>
}