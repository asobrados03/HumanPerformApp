package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.ReserveRequest
import com.humanperformcenter.shared.data.model.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.DaySession
import com.humanperformcenter.shared.data.model.SharedPool
import com.humanperformcenter.shared.data.persistence.DaySessionRepositoryImpl
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

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

    private val _sharedSessions = MutableStateFlow<List<SharedPool>>(emptyList())
    val sharedSessions: StateFlow<List<SharedPool>> get() = _sharedSessions

    private val _serviceToPrimary = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val serviceToPrimary: StateFlow<Map<Int, Int>> get() = _serviceToPrimary


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
                _sharedSessions.value = response.unlimited_shared
                _serviceToPrimary.value = response.service_to_primary
            } catch (e: Exception) {
                println("Error al cargar límites semanales: ${e.message}")
            }
        }
    }

    fun seSuperoLimiteReserva(
        serviceId: Int?,
        selectedDate: LocalDate,
        weeklyLimits: Map<Int, Int>,
        unlimitedSessions: Map<Int, Int>,
        sharedSessions: List<SharedPool>,
        bookings: List<com.humanperformcenter.shared.data.model.UserBooking>,
        serviceToPrimary: Map<Int, Int>
    ): Boolean {
        if (serviceId == null) return false

        val primaryTarget = serviceToPrimary[serviceId] ?: serviceId

        val semanaInicio = selectedDate.minus(selectedDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val semanaFin = semanaInicio.plus(6, DateTimeUnit.DAY)

        val bookingsByPrimary = bookings.map { b ->
            val primary = serviceToPrimary[b.service_id] ?: b.service_id
            b.copy(service_id = primary)
        }

        val reservasServicio = bookingsByPrimary.filter { it.service_id == primaryTarget }

        val esRecurrente = weeklyLimits.containsKey(primaryTarget)
        val limiteSemanal = weeklyLimits[primaryTarget] ?: Int.MAX_VALUE
        val totalPermitido = unlimitedSessions[primaryTarget] ?: 0

        return if (esRecurrente) {
            val estaSemana = reservasServicio.count {
                val fecha = runCatching { LocalDate.parse(it.date.substring(0, 10)) }.getOrNull()
                fecha != null && fecha in semanaInicio..semanaFin
            }
            estaSemana >= limiteSemanal
        } else {
            val usadasTotalesServicio = reservasServicio.size
            val dedicadoRestante = (totalPermitido - usadasTotalesServicio).coerceAtLeast(0)

            val poolsQueAplican = sharedSessions.filter { pool -> primaryTarget in pool.services }

            val restanteCompartidoSum = poolsQueAplican.sumOf { pool ->
                val usadasEnPool = bookingsByPrimary.count { b -> b.service_id in pool.services }
                (pool.sessions - usadasEnPool).coerceAtLeast(0)
            }

            println("Pools para $primaryTarget: ${sharedSessions.filter { primaryTarget in it.services }}")
            println("Bookings primario: ${reservasServicio.size}")


            val disponibleTotal = dedicadoRestante + restanteCompartidoSum

            disponibleTotal <= 0
        }
    }
}

