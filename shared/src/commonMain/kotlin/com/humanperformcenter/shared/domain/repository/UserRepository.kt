package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.Professional
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.model.UserBooking

interface UserRepository {
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(email: String): Result<Unit>
    suspend fun getCoaches(): Result<List<Professional>>
    suspend fun getUserAllowedServices(customerId: Int) : List<ServiceAvailable>
    suspend fun getUserBookings(customerId: Int): List<UserBooking>
    suspend fun cancelUserBooking(bookingId: Int): Result<Unit>
}