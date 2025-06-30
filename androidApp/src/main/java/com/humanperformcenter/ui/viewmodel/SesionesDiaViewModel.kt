package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.ReservaRequest
import com.humanperformcenter.shared.data.model.SesionesDia
import com.humanperformcenter.shared.data.persistence.SesionDiaRepositoryImpl
import com.humanperformcenter.shared.domain.usecase.SesionDiaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

class SesionesDiaViewModel(
    private val useCase: SesionDiaUseCase // inyectalo aquí
) : ViewModel() {

    private val repository = SesionDiaRepositoryImpl

    private val _sessions = MutableStateFlow<List<SesionesDia>>(emptyList())
    val sessions: StateFlow<List<SesionesDia>> get() = _sessions

    private val _mensajeErrorReserva = MutableStateFlow<String?>(null)
    val mensajeErrorReserva: StateFlow<String?> = _mensajeErrorReserva

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

    suspend fun realizarReserva(
        customerId: Int,
        coachId: Int,
        serviceId: Int,
        centerId: Int,
        selectedDate: String, // "2025-06-27"
        hour: String          // "09:00"
    ) {
        val productId = useCase.getUserProductId(customerId)
        val timeslotId = useCase.getTimeslotId(hour)

        println("🎯 Realizando reserva con:")
        println("→ customerId = $customerId")
        println("→ coachId = $coachId")
        println("→ timeslotId = $timeslotId")
        println("→ serviceId = $serviceId")
        println("→ productId = $productId")
        println("→ centerId = $centerId")
        println("→ start_date = $selectedDate")

        val reserva = ReservaRequest(
            customer_id = customerId,
            coach_id = coachId,
            session_timeslot_id = timeslotId,
            service_id = serviceId,
            product_id = productId,
            center_id = centerId,
            start_date = selectedDate
        )

        try {
            useCase.reservarSesion(reserva)
            println("✅ Reserva enviada correctamente")
            _mensajeErrorReserva.value = null
        } catch (e: IllegalStateException) {
            println("❌ Error al reservar: ${e.message}")
            _mensajeErrorReserva.value = e.message
        }
    }


    suspend fun getPreferredCoachId(customerId: Int, serviceId: Int): Int? {
        return useCase.getPreferredCoach(customerId, serviceId)
    }
    fun clearMensajeError() {
        _mensajeErrorReserva.value = null
    }

}

