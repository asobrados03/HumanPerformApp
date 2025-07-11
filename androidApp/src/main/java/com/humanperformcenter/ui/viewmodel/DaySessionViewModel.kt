package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.ReserveRequest
import com.humanperformcenter.shared.data.model.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.DaySession
import com.humanperformcenter.shared.data.persistence.DaySessionRepositoryImpl
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class DaySessionViewModel(
    private val useCase: DaySessionUseCase // inyectalo aquí
) : ViewModel() {

    private val repository = DaySessionRepositoryImpl

    private val _sessions = MutableStateFlow<List<DaySession>>(emptyList())
    val sessions: StateFlow<List<DaySession>> get() = _sessions

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
                    println("→ ${it.date} | ${it.hour} | service_id=${it.serviceId} | coach=${it.coachName} | booked=${it.booked}/${it.capacity}")
                }

                _sessions.value = result.filter {
                    it.serviceId == serviceId
                }

                println("→ Sesiones filtradas: ${_sessions.value.size}")
            } catch (e: Exception) {
                println("❌ ERROR AL CONSULTAR SESIONES: ${e.message}")
            }
        }
    }

    private val _coachesForHour = MutableStateFlow<List<DaySession>>(emptyList())
    val coachesForHour: StateFlow<List<DaySession>> get() = _coachesForHour

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

        val reserva = ReserveRequest(
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

    fun cambiarReservaSesion(
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

                val request = ReserveUpdateRequest(
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

    private val _holidays = MutableStateFlow<List<LocalDate>>(emptyList())
    val holidays: StateFlow<List<LocalDate>> get() = _holidays

    fun fetchHolidays() {
        viewModelScope.launch {
            try {
                val result = useCase.getHolidays() // Devuelve List<String>
                _holidays.value = result.map { LocalDate.parse(it) }
            } catch (e: Exception) {
                println("❌ Error al cargar festivos: ${e.message}")
            }
        }
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

