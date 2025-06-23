package com.humanperformcenter.shared.domain.usecase

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
}
