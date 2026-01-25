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

    suspend fun getSessionsByDay(serviceId: Int, date: LocalDate): Result<List<DaySession>> {
        return repository.getSessionsByDay(serviceId, date)
    }

    suspend fun reservarSesion(request: ReserveRequest): Result<ReserveResponse> {
        return repository.reservarSesion(request)
    }

    suspend fun cambiarReservaSesion(request: ReserveUpdateRequest): Result<ReserveUpdateResponse> {
        return repository.cambiarReservaSesion(request)
    }

    suspend fun getUserProductId(customerId: Int): Result<Int> {
        return repository.getUserProductId(customerId)
    }

    suspend fun getPreferredCoach(customerId: Int, serviceId: Int): Result<Int?> {
        return repository.getPreferredCoach(customerId, serviceId)
    }

    suspend fun getTimeslotId(hora: String): Result<Int> {
        return repository.getTimeslotId(hora)
    }

    suspend fun getUserWeeklyLimit(customerId: Int): Result<UserWeeklyLimitResponse> {
        return repository.getUserWeeklyLimit(customerId)
    }

    suspend fun getHolidays(): Result<List<String>> {
        return repository.getHolidays()
    }

    suspend fun enviarCuestionarioReserva(bookingForm: BookingQuestionnaireRequest): Result<Unit> {
        return repository.enviarCuestionarioReserva(bookingForm)
    }

    suspend fun cuestionarioEnviado(bookingId: Int): Result<Boolean> {
        return repository.cuestionarioEnviado(bookingId)
    }

}
