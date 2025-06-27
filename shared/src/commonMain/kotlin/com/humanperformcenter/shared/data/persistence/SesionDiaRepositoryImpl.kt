package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ReservaRequest
import com.humanperformcenter.shared.data.model.ReservaResponse
import com.humanperformcenter.shared.data.model.SesionesDia
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.SesionDiaRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

object SesionDiaRepositoryImpl : SesionDiaRepository {

    override suspend fun getSessionsByDay(serviceId: Int, weekStart: LocalDate): List<SesionesDia> {
        val client = ApiClient.apiClient

        return client.get("${ApiClient.baseUrl}/mobile/daily") {
            parameter("service_id", serviceId)
            parameter("date", weekStart.toString()) // formato yyyy-MM-dd
        }.body()
    }

    override suspend fun reservarSesion(reservaRequest: ReservaRequest): ReservaResponse {
        val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/reserve") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(reservaRequest)
        }
        return response.body()
    }

    override suspend fun getUserProductId(customerId: Int): Int {
        val client = ApiClient.apiClient
        val response = client.get("${ApiClient.baseUrl}/mobile/user-product")
        val json = response.body<Map<String, Int>>()
        return json["product_id"] ?: throw IllegalStateException("No se encontró el product_id")
    }

    override suspend fun getPreferredCoach(customerId: Int, serviceId: Int): Int? {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/preferred-coach") {
            parameter("customer_id", customerId)
            parameter("service_id", serviceId)
        }

        val json = response.body<JsonObject>()
        return json["coach_id"]?.jsonPrimitive?.intOrNull
    }
    override suspend fun getTimeslotId(hora: String): Int {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/timeslot-id") {
            parameter("hour", hora)
        }

        val json = response.body<JsonObject>()
        return json["timeslot_id"]?.jsonPrimitive?.intOrNull
            ?: throw IllegalStateException("No se encontró el timeslot_id para la hora $hora")
    }

}
