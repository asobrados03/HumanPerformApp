package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.ReserveRequest
import com.humanperformcenter.shared.data.model.ReserveResponse
import com.humanperformcenter.shared.data.model.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.ReserveUpdateResponse
import com.humanperformcenter.shared.data.model.DaySession
import com.humanperformcenter.shared.data.model.UserWeeklyLimitResponse
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class DaySessionUseCase(private val repository: DaySessionRepository) {

    suspend fun getSessionsByDay(serviceId: Int, date: LocalDate): List<DaySession> = withContext(Dispatchers.IO) {
        return@withContext repository.getSessionsByDay(serviceId, date)
    }
    suspend fun reservarSesion(request: ReserveRequest): ReserveResponse = withContext(Dispatchers.IO) {
        return@withContext repository.reservarSesion(request)
    }
    suspend fun cambiarReservaSesion(request: ReserveUpdateRequest): ReserveUpdateResponse = withContext(Dispatchers.IO) {
        return@withContext repository.cambiarReservaSesion(request)
    }
    suspend fun getUserProductId(customerId: Int): Int = withContext(Dispatchers.IO) {
        return@withContext repository.getUserProductId(customerId)
    }
    suspend fun getPreferredCoach(customerId: Int, serviceId: Int): Int? = withContext(Dispatchers.IO) {
        return@withContext repository.getPreferredCoach(customerId, serviceId)
    }
    suspend fun getTimeslotId(hora: String): Int = withContext(Dispatchers.IO) {
        return@withContext repository.getTimeslotId(hora)
    }
    suspend fun getUserWeeklyLimit(customerId: Int): UserWeeklyLimitResponse = withContext(Dispatchers.IO) {
        return@withContext repository.getUserWeeklyLimit(customerId)
    }
    suspend fun getHolidays(): List<String> = withContext(Dispatchers.IO) {
        return@withContext repository.getHolidays()
    }
}
