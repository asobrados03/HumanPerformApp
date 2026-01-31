package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.data.model.booking.WeeklyLimitsWrapper
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import kotlinx.datetime.LocalDate

class DaySessionUseCase(private val repository: DaySessionRepository) {

    suspend fun getSessionsByDay(productId: Int, date: LocalDate): Result<List<DaySession>> {
        return repository.getSessionsByDay(productId, date)
    }

    suspend fun makeBooking(bookingRequest: BookingRequest): Result<ReserveResponse> {
        return repository.makeBooking(bookingRequest)
    }

    suspend fun modifyBookingSession(request: ReserveUpdateRequest): Result<ReserveUpdateResponse> {
        return repository.modifyBookingSession(request)
    }

    suspend fun getTimeslotId(serviceId: Int, dayOfWeek: String, hour: String): Result<Int> {
        return repository.getTimeslotId(serviceId, dayOfWeek, hour)
    }

    suspend fun fetchServiceIdForProduct(productId: Int): Result<Int> {
        return repository.getProductServiceInfo(productId)
    }

    suspend fun getUserWeeklyLimit(customerId: Int): Result<WeeklyLimitsWrapper> {
        return repository.getUserWeeklyLimit(customerId)
    }

    suspend fun getHolidays(): Result<List<String>> {
        return repository.getHolidays()
    }
}
