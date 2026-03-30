package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.data.remote.DaySessionRemoteDataSource
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import kotlinx.datetime.LocalDate

class DaySessionRepositoryImpl(
    private val remoteDataSource: DaySessionRemoteDataSource,
) : DaySessionRepository {
    override suspend fun getSessionsByDay(productId: Int, weekStart: LocalDate): Result<List<DaySession>> =
        remoteDataSource.getSessionsByDay(productId, weekStart)

    override suspend fun makeBooking(bookingRequest: BookingRequest): Result<ReserveResponse> =
        remoteDataSource.makeBooking(bookingRequest)

    override suspend fun modifyBookingSession(reserveUpdateRequest: ReserveUpdateRequest): Result<ReserveUpdateResponse> =
        remoteDataSource.modifyBookingSession(reserveUpdateRequest)

    override suspend fun getUserProductId(customerId: Int): Result<Int> = remoteDataSource.getUserProductId(customerId)
    override suspend fun getProductServiceInfo(productId: Int): Result<Int> = remoteDataSource.getProductServiceInfo(productId)
    override suspend fun getTimeslotId(serviceId: Int, dayOfWeek: String, hour: String): Result<Int> =
        remoteDataSource.getTimeslotId(serviceId, dayOfWeek, hour)

    override suspend fun getHolidays(): Result<List<String>> = remoteDataSource.getHolidays()
}
