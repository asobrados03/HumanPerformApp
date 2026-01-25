package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.booking.BookingQuestionnaireRequest
import com.humanperformcenter.shared.data.model.booking.ReserveRequest
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.UserWeeklyLimitResponse
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

object DaySessionRepositoryImpl : DaySessionRepository {

    override suspend fun getSessionsByDay(serviceId: Int, weekStart: LocalDate): Result<List<DaySession>> {
        return try {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/daily") {
                parameter("service_id", serviceId)
                parameter("date", weekStart.toString()) // Ktor maneja bien el toString de LocalDate
            }

            if (response.status.isSuccess()) {
                val sessions = response.body<List<DaySession>>()
                Result.success(sessions)
            } else {
                // Intentamos capturar un error amigable si el backend lo envía
                Result.failure(Exception("Error al cargar sesiones: ${response.status.value}"))
            }
        } catch (e: Exception) {
            println("❌ Error en getSessionsByDay: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun reservarSesion(reserveRequest: ReserveRequest): Result<ReserveResponse> = runCatching {
        val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/reserve") {
            contentType(ContentType.Application.Json)
            setBody(reserveRequest)
        }

        if (response.status.value == 409) return Result.failure(Exception("Ya tienes una reserva a esta hora."))
        if (!response.status.isSuccess()) return Result.failure(Exception("Error de reserva: ${response.status}"))

        response.body<ReserveResponse>()
    }

    override suspend fun cambiarReservaSesion(reserveUpdateRequest: ReserveUpdateRequest): Result<ReserveUpdateResponse> = runCatching {
        val response = ApiClient.apiClient.put("${ApiClient.baseUrl}/mobile/update-booking") {
            contentType(ContentType.Application.Json)
            setBody(reserveUpdateRequest)
        }

        if (!response.status.isSuccess()) return Result.failure(Exception("Error al actualizar reserva: ${response.status}"))

        response.body<ReserveUpdateResponse>()
    }

    override suspend fun getUserProductId(customerId: Int): Result<Int> = runCatching {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-product") {
            parameter("user_id", customerId)
        }
        val json = response.body<Map<String, Int>>()
        json["product_id"] ?: throw Exception("No se encontró el product_id")
    }

    override suspend fun getPreferredCoach(customerId: Int, serviceId: Int): Result<Int?> = runCatching {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/preferred-coach") {
            parameter("customer_id", customerId)
            parameter("service_id", serviceId)
        }
        // Usamos body<JsonObject> para manejar la nulabilidad de forma segura
        val json = response.body<JsonObject>()
        json["coach_id"]?.jsonPrimitive?.intOrNull
    }

    override suspend fun getTimeslotId(hora: String): Result<Int> = runCatching {
        val formattedHour = if (hora.length == 5) "$hora:00" else hora
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/timeslot-id") {
            parameter("hour", formattedHour)
        }
        val json = response.body<Map<String, Int>>()
        json["session_timeslot_id"] ?: throw Exception("ID de franja horaria no válido")
    }

    override suspend fun getUserWeeklyLimit(userId: Int): Result<UserWeeklyLimitResponse> = runCatching {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-weekly-limit") {
            parameter("user_id", userId)
        }
        if (!response.status.isSuccess()) throw Exception("Error de servidor: ${response.status}")
        response.body<UserWeeklyLimitResponse>()
    }

    override suspend fun getHolidays(): Result<List<String>> = runCatching {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/holidays")
        if (!response.status.isSuccess()) throw Exception("Error al cargar festivos")
        response.body<List<String>>()
    }

    override suspend fun enviarCuestionarioReserva(bookingForm: BookingQuestionnaireRequest): Result<Unit> = runCatching {
        val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/booking-questionnaire") {
            contentType(ContentType.Application.Json)
            setBody(bookingForm)
        }
        if (!response.status.isSuccess()) throw Exception("Error al enviar cuestionario")
    }

    override suspend fun cuestionarioEnviado(bookingId: Int): Result<Boolean> = runCatching {
        val response = ApiClient.apiClient.get(
            "${ApiClient.baseUrl}/mobile/booking-questionnaire/$bookingId"
        )

        when {
            response.status.value == 404 -> false
            response.status.isSuccess() -> response.body<Boolean>()
            else -> throw Exception("Error al consultar estado: ${response.status}")
        }
    }
}
