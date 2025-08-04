package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.Coupon
import com.humanperformcenter.shared.data.model.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.UserStatistics
import com.humanperformcenter.shared.data.model.Professional
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.model.UserBooking

interface UserRepository {
    suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User>
    suspend fun deleteUser(email: String): Result<Unit>
    suspend fun getCoaches(): Result<List<Professional>>
    suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String>
    suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse>
    suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit>
    suspend fun getUserAllowedServices(customerId: Int) : List<ServiceAvailable>
    suspend fun getUserBookings(customerId: Int): List<UserBooking>
    suspend fun cancelUserBooking(bookingId: Int): Result<Unit>
    suspend fun getUserStats(customerId: Int): UserStatistics
    suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit>
    suspend fun getUserCoupon(userId: Int): Result<Coupon?>
    suspend fun uploadDocument(name: String, data: ByteArray): Result<String>
}