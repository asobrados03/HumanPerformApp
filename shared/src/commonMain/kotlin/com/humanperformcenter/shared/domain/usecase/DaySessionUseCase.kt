package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.booking.BookingQuestionnaireRequest
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.ReserveRequest
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.data.model.booking.UserWeeklyLimitResponse
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import kotlinx.datetime.LocalDate

class DaySessionUseCase(private val repository: DaySessionRepository) {

    suspend fun getSessionsByDay(serviceId: Int, date: LocalDate): List<DaySession> {
        return repository.getSessionsByDay(serviceId, date)
    }

    suspend fun reservarSesion(request: ReserveRequest): ReserveResponse {
        return repository.reservarSesion(request)
    }

    suspend fun cambiarReservaSesion(request: ReserveUpdateRequest): ReserveUpdateResponse {
        return repository.cambiarReservaSesion(request)
    }

    suspend fun getUserProductId(customerId: Int): Int {
        return repository.getUserProductId(customerId)
    }

    suspend fun getPreferredCoach(customerId: Int, serviceId: Int): Int? {
        return repository.getPreferredCoach(customerId, serviceId)
    }

    suspend fun getTimeslotId(hora: String): Int {
        return repository.getTimeslotId(hora)
    }

    suspend fun getUserWeeklyLimit(customerId: Int): UserWeeklyLimitResponse {
        return repository.getUserWeeklyLimit(customerId)
    }

    suspend fun getHolidays(): List<String> {
        return repository.getHolidays()
    }

    suspend fun enviarCuestionarioReserva(bookingForm: BookingQuestionnaireRequest): Boolean {
        return repository.enviarCuestionarioReserva(bookingForm)
    }

    suspend fun cuestionarioEnviado(bookingId: Int): Boolean {
        return repository.cuestionarioEnviado(bookingId)
    }

}
