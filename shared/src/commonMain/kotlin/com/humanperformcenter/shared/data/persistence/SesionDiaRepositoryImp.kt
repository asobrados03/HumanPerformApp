package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.SesionesDia
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.SesionDiaRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate

object SesionDiaRepositoryImp : SesionDiaRepository {

    override suspend fun getSessionsByDay(serviceId: Int, weekStart: LocalDate): List<SesionesDia> {
        val client = ApiClient.httpClient

        return client.get("${ApiClient.apibaseUrl}/api/mobile/daily") {
            parameter("service_id", serviceId)
            parameter("date", weekStart.toString()) // formato yyyy-MM-dd
        }.body()
    }
}
