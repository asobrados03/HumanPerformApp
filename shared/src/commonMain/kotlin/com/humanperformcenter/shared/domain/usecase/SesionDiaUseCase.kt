package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.ReservaRequest
import com.humanperformcenter.shared.data.model.ReservaResponse
import com.humanperformcenter.shared.data.model.SesionesDia
import com.humanperformcenter.shared.domain.repository.SesionDiaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class SesionDiaUseCase(private val repository: SesionDiaRepository) {

    suspend fun getSessionsByDay(serviceId: Int, date: LocalDate): List<SesionesDia> = withContext(Dispatchers.IO) {
        return@withContext repository.getSessionsByDay(serviceId, date)
    }
    suspend fun reservarSesion(request: ReservaRequest): ReservaResponse = withContext(Dispatchers.IO) {
        return@withContext repository.reservarSesion(request)
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
}
