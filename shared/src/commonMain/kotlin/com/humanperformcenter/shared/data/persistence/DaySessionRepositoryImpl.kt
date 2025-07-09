package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ReserveRequest
import com.humanperformcenter.shared.data.model.ReserveResponse
import com.humanperformcenter.shared.data.model.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.ReserveUpdateResponse
import com.humanperformcenter.shared.data.model.DaySession
import com.humanperformcenter.shared.data.model.UserWeeklyLimitResponse
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

object DaySessionRepositoryImpl : DaySessionRepository {

    override suspend fun getSessionsByDay(serviceId: Int, weekStart: LocalDate): List<DaySession> {
        val client = ApiClient.apiClient

        return client.get("${ApiClient.baseUrl}/mobile/daily") {
            parameter("service_id", serviceId)
            parameter("date", weekStart.toString()) // formato yyyy-MM-dd
        }.body()
    }

    override suspend fun reservarSesion(reserveRequest: ReserveRequest): ReserveResponse {
        val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/reserve") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(reserveRequest)
        }
        if (response.status.value == 409) {
            throw IllegalStateException("Ya tienes una reserva a esta hora.")
        }
        return response.body()
    }

    override suspend fun cambiarReservaSesion(reserveUpdateRequest: ReserveUpdateRequest): ReserveUpdateResponse {
        val response = ApiClient.apiClient.put("${ApiClient.baseUrl}/mobile/update-booking") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(reserveUpdateRequest)
        }
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Error al actualizar reserva: ${response.status}")
        }
        return response.body()
    }

    override suspend fun getUserProductId(customerId: Int): Int {
        val client = ApiClient.apiClient
        val response = client.get("${ApiClient.baseUrl}/mobile/user-product") {
            parameter("user_id", customerId)
        }
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
        val formattedHour = if (hora.length == 5) "$hora:00" else hora
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/timeslot-id") {
            parameter("hour", formattedHour)
        }

        val json = response.body<Map<String, Int>>()
        return json["session_timeslot_id"] ?: throw IllegalStateException("Respuesta inválida")
    }

    override suspend fun getUserWeeklyLimit(userId: Int): UserWeeklyLimitResponse {
        val response: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-weekly-limit") {
            url {
                parameters.append("user_id", userId.toString())
            }
        }

        if (!response.status.isSuccess()) {
            throw IllegalStateException("Error al obtener límite semanal: ${response.status}")
        }

        return response.body()
    }

    override suspend fun getHolidays(): List<String> {
        val response: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/holidays")
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Error al obtener festivos: ${response.status}")
        }
        return response.body()
    }

}
