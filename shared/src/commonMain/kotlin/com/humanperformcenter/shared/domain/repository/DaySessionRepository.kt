package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.BookingQuestionnaireRequest
import com.humanperformcenter.shared.data.model.ReserveRequest
import com.humanperformcenter.shared.data.model.ReserveResponse
import com.humanperformcenter.shared.data.model.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.ReserveUpdateResponse
import com.humanperformcenter.shared.data.model.DaySession
import com.humanperformcenter.shared.data.model.UserBooking
import com.humanperformcenter.shared.data.model.UserWeeklyLimitResponse
import kotlinx.datetime.LocalDate

interface DaySessionRepository {
    suspend fun getSessionsByDay(serviceId: Int, weekStart: LocalDate): List<DaySession>
    suspend fun reservarSesion(reserveRequest: ReserveRequest): ReserveResponse
    suspend fun cambiarReservaSesion(reserveUpdateRequest: ReserveUpdateRequest): ReserveUpdateResponse
    suspend fun getUserProductId(customerId: Int): Int
    suspend fun getPreferredCoach(customerId: Int, serviceId: Int): Int?
    suspend fun getTimeslotId(hora: String): Int
    suspend fun getUserWeeklyLimit(userId: Int): UserWeeklyLimitResponse
    suspend fun getHolidays(): List<String>
    suspend fun enviarCuestionarioReserva(bookingForm: BookingQuestionnaireRequest): Boolean
    suspend fun cuestionarioEnviado(booking_id: Int): Boolean
}