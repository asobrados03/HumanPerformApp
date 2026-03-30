package com.humanperformcenter.shared.data.repository

import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.data.remote.DaySessionRemoteDataSource
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import kotlinx.datetime.LocalDate

class DaySessionRepositoryImpl(
    private val remote: DaySessionRemoteDataSource,
) : DaySessionRepository {
    override suspend fun getSessionsByDay(productId: Int, weekStart: LocalDate): Result<List<DaySession>> =
        remote.getSessionsByDay(productId, weekStart).mapDomainError()

    override suspend fun makeBooking(bookingRequest: BookingRequest): Result<ReserveResponse> =
        remote.makeBooking(bookingRequest).mapDomainError()

    override suspend fun modifyBookingSession(reserveUpdateRequest: ReserveUpdateRequest): Result<ReserveUpdateResponse> =
        remote.modifyBookingSession(reserveUpdateRequest).mapDomainError()

    override suspend fun getUserProductId(customerId: Int): Result<Int> = remote.getUserProductId(customerId).mapDomainError()
    override suspend fun getProductServiceInfo(productId: Int): Result<Int> = remote.getProductServiceInfo(productId).mapDomainError()
    override suspend fun getTimeslotId(serviceId: Int, dayOfWeek: String, hour: String): Result<Int> =
        remote.getTimeslotId(serviceId, dayOfWeek, hour).mapDomainError()

    override suspend fun getHolidays(): Result<List<String>> = remote.getHolidays().mapDomainError()
}
