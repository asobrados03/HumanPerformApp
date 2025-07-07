package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.ReservaRequest
import com.humanperformcenter.shared.data.model.ReservaUpdateRequest
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

    private val _weeklyLimits = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val weeklyLimits: StateFlow<Map<Int, Int>> = _weeklyLimits

    private val _unlimitedSessions = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val unlimitedSessions: StateFlow<Map<Int, Int>> get() = _unlimitedSessions


    fun fetchAvailableSessions(serviceId: Int, date: LocalDate) {
        val weekStart = date.toString()

        println("==== FETCH DE SESIONES DISPONIBLES ====")
        println("Fecha seleccionada: $date")
        println("Inicio de semana: $weekStart")
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
                    it.service_id == serviceId
                }

                println("→ Sesiones filtradas: ${_sessions.value.size}")
            } catch (e: Exception) {
                println("❌ ERROR AL CONSULTAR SESIONES: ${e.message}")
            }
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

    suspend fun cambiarReservaSesion(
        customerId: Int,
        bookingId: Int,
        newCoachId: Int,
        newServiceId: Int,
        newStartDate: String, // "2025-07-03"
        hour: String
    ) {
        viewModelScope.launch {
            try {
                val productId = useCase.getUserProductId(customerId)
                val timeslotId = useCase.getTimeslotId(hour)

                val request = ReservaUpdateRequest(
                    booking_id = bookingId,
                    new_coach_id = newCoachId,
                    new_service_id = newServiceId,
                    new_product_id = productId,
                    new_session_timeslot_id = timeslotId,
                    new_start_date = newStartDate
                )

                useCase.cambiarReservaSesion(request)

                println("✅ Reserva actualizada correctamente")
                _mensajeErrorReserva.value = null
            } catch (e: Exception) {
                println("❌ Error al actualizar reserva: ${e.message}")
                _mensajeErrorReserva.value = e.message
            }
        }
    }

    fun clearSessions() {
        _sessions.value = emptyList()
    }

    suspend fun getPreferredCoachId(customerId: Int, serviceId: Int): Int? {
        return useCase.getPreferredCoach(customerId, serviceId)
    }
    fun clearMensajeError() {
        _mensajeErrorReserva.value = null
    }

    fun fetchUserWeeklyLimit(userId: Int) {
        viewModelScope.launch {
            try {
                val response = useCase.getUserWeeklyLimit(userId)
                _weeklyLimits.value = response.weekly_limit
                _unlimitedSessions.value = response.unlimited_sessions
            } catch (e: Exception) {
                println("Error al cargar límites semanales: ${e.message}")
            }
        }
    }
}

