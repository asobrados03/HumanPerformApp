package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.booking.BookingQuestionnaireRequest
import com.humanperformcenter.shared.data.model.booking.ReserveRequest
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.UserWeeklyLimitResponse
import kotlinx.datetime.LocalDate

interface DaySessionRepository {
    suspend fun getSessionsByDay(serviceId: Int, weekStart: LocalDate): Result<List<DaySession>>
    suspend fun reservarSesion(reserveRequest: ReserveRequest): Result<ReserveResponse>
    suspend fun cambiarReservaSesion(reserveUpdateRequest: ReserveUpdateRequest): Result<ReserveUpdateResponse>
    suspend fun getUserProductId(customerId: Int): Result<Int>
    suspend fun getPreferredCoach(customerId: Int, serviceId: Int): Result<Int?>
    suspend fun getTimeslotId(hora: String): Result<Int>
    suspend fun getUserWeeklyLimit(userId: Int): Result<UserWeeklyLimitResponse>
    suspend fun getHolidays(): Result<List<String>>
    suspend fun enviarCuestionarioReserva(bookingForm: BookingQuestionnaireRequest): Result<Unit>
    suspend fun cuestionarioEnviado(bookingId: Int): Result<Boolean>
}