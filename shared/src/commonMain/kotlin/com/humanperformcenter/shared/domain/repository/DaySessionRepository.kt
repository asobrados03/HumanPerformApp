package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.WeeklyLimitsWrapper
import kotlinx.datetime.LocalDate

interface DaySessionRepository {
    suspend fun getSessionsByDay(productId: Int, weekStart: LocalDate): Result<List<DaySession>>
    suspend fun makeBooking(bookingRequest: BookingRequest): Result<ReserveResponse>
    suspend fun modifyBookingSession(reserveUpdateRequest: ReserveUpdateRequest): Result<ReserveUpdateResponse>
    suspend fun getUserProductId(customerId: Int): Result<Int>
    suspend fun getProductServiceInfo(productId: Int): Result<Int>
    suspend fun getTimeslotId(serviceId: Int, dayOfWeek: String, hour: String): Result<Int>
    suspend fun getUserWeeklyLimit(userId: Int): Result<WeeklyLimitsWrapper>
    suspend fun getHolidays(): Result<List<String>>
}