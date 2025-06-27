package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.SesionesDia
import com.humanperformcenter.shared.data.persistence.SesionDiaRepositoryImpl
import com.humanperformcenter.shared.domain.usecase.SesionDiaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import io.ktor.client.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class SesionesDiaViewModel(
    private val useCase: SesionDiaUseCase // inyectalo aquí
) : ViewModel() {

    private val repository = SesionDiaRepositoryImpl

    private val _sessions = MutableStateFlow<List<SesionesDia>>(emptyList())
    val sessions: StateFlow<List<SesionesDia>> get() = _sessions

    fun fetchAvailableSessions(serviceId: Int, date: LocalDate, tipoSesion: String) {
        val weekStart = date.toString()

        println("==== FETCH DE SESIONES DISPONIBLES ====")
        println("Fecha seleccionada: $date")
        println("Inicio de semana: $weekStart")
        println("Tipo de sesión: $tipoSesion")
        println("Service ID enviado: $serviceId")
        println("=======================================")

        viewModelScope.launch {
            try {
                val result = useCase.getSessionsByDay(serviceId, date)

                println("---- Resultados recibidos (${result.size}) ----")
                result.forEach {
                    println("→ ${it.date} | ${it.hour} | service_id=${it.service_id} | coach=${it.coach_name} | booked=${it.booked}/${it.capacity}")
                }

                _sessions.value = result.filter {
                    itMatchesTipo(it.service_id, tipoSesion)
                }

                println("→ Sesiones filtradas: ${_sessions.value.size}")
            } catch (e: Exception) {
                println("❌ ERROR AL CONSULTAR SESIONES: ${e.message}")
            }
        }
    }

    private fun itMatchesTipo(serviceId: Int, tipo: String): Boolean {
        return when (tipo.lowercase()) {
            "nutrición" -> serviceId == 1
            "entrenamiento" -> serviceId == 2
            "fisioterapia" -> serviceId == 3
            else -> false
        }
    }
    private val _coachesForHour = MutableStateFlow<List<SesionesDia>>(emptyList())
    val coachesForHour: StateFlow<List<SesionesDia>> get() = _coachesForHour

    fun obtenerEntrenadoresPorHora(hora: String) {
        _coachesForHour.value = _sessions.value.filter { it.hour == hora }
    }

    fun realizarReserva(
        token: String,
        customerId: Int,
        coachId: Int,
        sessionTimeslotId: Int,
        serviceId: Int,
        productId: Int,
        //centerId: Int,
        startDate: String
    ) {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(DefaultRequest) {
                header("Authorization", "Bearer $token")
            }
        }

        viewModelScope.launch {
            try {
                val response: HttpResponse = client.post("https://tudominio.com/api/mobile/reserve") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "customer_id" to customerId,
                            "coach_id" to coachId,
                            "session_timeslot_id" to sessionTimeslotId,
                            "service_id" to serviceId,
                            "product_id" to productId,
                            //"center_id" to centerId,
                            "start_date" to startDate,
                            "status" to "active",
                            "payment_status" to "pending",
                            "payment_method" to "card"
                        )
                    )
                }

                if (response.status == HttpStatusCode.Created) {
                    println("✅ Reserva creada correctamente")
                } else {
                    println("❌ Error HTTP ${response.status.value}: ${response.bodyAsText()}")
                }
            } catch (e: Exception) {
                println("❌ Excepción al reservar: ${e.message}")
            }
        }
    }

    suspend fun getPreferredCoachId(customerId: Int, serviceId: Int): Int? {
        return useCase.getPreferredCoach(customerId, serviceId)
    }
    suspend fun getUserProductId(): Int {
        return useCase.getUserProductId()
    }
    suspend fun getTimeslotId(hora: String): Int {
        return useCase.getTimeslotId(hora)
    }

}

