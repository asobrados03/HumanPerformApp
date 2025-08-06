package com.humanperformcenter.ui.viewmodel


import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.BookingQuestionnaireRequest
import com.humanperformcenter.shared.data.model.ReserveRequest
import com.humanperformcenter.shared.data.model.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.DaySession
import com.humanperformcenter.shared.data.model.SharedPool
import com.humanperformcenter.shared.data.model.UserBooking
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime


class DaySessionViewModel(
    private val useCase: DaySessionUseCase // inyectalo aquí
) : ViewModel() {
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

    private val _validFromByPrimary = MutableStateFlow<Map<Int, String>>(emptyMap())
    val validFromByPrimary: StateFlow<Map<Int, String>> get() = _validFromByPrimary

    private val _cuestionarioActivo = MutableStateFlow(false)
    val cuestionarioActivo: StateFlow<Boolean> = _cuestionarioActivo

    private val _preguntaActual = MutableStateFlow(0)
    val preguntaActual: StateFlow<Int> = _preguntaActual

    private val _respuestas = mutableStateListOf<String?>()
    val respuestas: List<String?> = _respuestas

    private val sesionesOmitidas = mutableListOf<Int>()

    private var bookingIdPendiente: Int? = null

    init {
        repeat(5) { _respuestas.add(null) }
    }

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
        newStartDate: String,
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
                _validFromByPrimary.value = response.valid_from_by_primary
            } catch (e: Exception) {
                println("Error al cargar límites semanales: ${e.message}")
            }
        }
    }

    private fun parseDateOrNull(iso: String?): LocalDate? =
        iso?.let { runCatching { LocalDate.parse(it.substring(0, 10)) }.getOrNull() }

    fun seSuperoLimiteReserva(
        serviceId: Int?,
        selectedDate: LocalDate,
        weeklyLimits: Map<Int, Int>,
        unlimitedSessions: Map<Int, Int>,
        sharedSessions: List<SharedPool>,
        bookings: List<com.humanperformcenter.shared.data.model.UserBooking>,
        serviceToPrimary: Map<Int, Int>,
        validFromByPrimary: Map<Int, String> = emptyMap() // NUEVO parámetro (con default)
    ): Boolean {
        if (serviceId == null) return false

        val primaryTarget = serviceToPrimary[serviceId] ?: serviceId

        val semanaInicio = selectedDate.minus(selectedDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val semanaFin = semanaInicio.plus(6, DateTimeUnit.DAY)

        val bookingsByPrimary = bookings
            .map { b ->
                val primary = serviceToPrimary[b.service_id] ?: b.service_id
                b.copy(service_id = primary)
            }

        val reservasServicio = bookingsByPrimary.filter { it.service_id == primaryTarget }

        val esRecurrente = weeklyLimits.containsKey(primaryTarget)
        val limiteSemanal = weeklyLimits[primaryTarget] ?: Int.MAX_VALUE

        if (esRecurrente) {
            val estaSemana = reservasServicio.count {
                val fecha = runCatching { LocalDate.parse(it.date.substring(0, 10)) }.getOrNull()
                fecha != null && fecha in semanaInicio..semanaFin
            }
            return estaSemana >= limiteSemanal
        }

        val poolsQueAplican = sharedSessions.filter { primaryTarget in it.services }

        val validFromPrimaryDate = parseDateOrNull(validFromByPrimary[primaryTarget])
        val usadasTotalesServicio = reservasServicio.count { b ->
            val f = runCatching { LocalDate.parse(b.date.substring(0, 10)) }.getOrNull()
            f != null && (validFromPrimaryDate == null || f >= validFromPrimaryDate)
        }
        val dedicadoPermitido = unlimitedSessions[primaryTarget] // puede ser null si no hay bono dedicado
        val dedicadoRestante = if (dedicadoPermitido != null)
            (dedicadoPermitido - usadasTotalesServicio).coerceAtLeast(0)
        else 0

        val restanteCompartidoSum = if (poolsQueAplican.isNotEmpty()) {
            poolsQueAplican.sumOf { pool ->
                val vf = parseDateOrNull(pool.valid_from)
                val vt = parseDateOrNull(pool.valid_to)
                val usadasEnPool = bookingsByPrimary.count { b ->
                    val f = runCatching { LocalDate.parse(b.date.substring(0, 10)) }.getOrNull()
                    f != null &&
                            (vf == null || f >= vf) &&
                            (vt == null || f <= vt) &&
                            b.service_id in pool.services
                }
                (pool.sessions - usadasEnPool).coerceAtLeast(0)
            }
        } else 0

        if (dedicadoPermitido == null && poolsQueAplican.isEmpty()) {
            return false
        }

        val disponibleTotal = (dedicadoRestante + restanteCompartidoSum).coerceAtLeast(0)
        return disponibleTotal <= 0
    }

    @OptIn(ExperimentalTime::class)
    suspend fun cargarFormularioSiProcede(proximasSesiones: List<UserBooking>) {
        val zona = TimeZone.currentSystemDefault()

        // Obtener la fecha y hora actuales desde java.time (Android)
        val nowJava = java.time.LocalDateTime.now()
        val ahora = LocalDateTime(
            year = nowJava.year,
            month = nowJava.monthValue,
            day = nowJava.dayOfMonth,
            hour = nowJava.hour,
            minute = nowJava.minute,
            second = nowJava.second,
            nanosecond = 0
        )

        val nowInstant = ahora.toInstant(zona)

        val sesionesFuturas = proximasSesiones.filter { sesion ->
            try {
                val horaFormateada = if (sesion.hour.length == 5) "${sesion.hour}:00" else sesion.hour
                val fechaSolo = sesion.date.take(10)
                val sesionDateTime = LocalDateTime.parse("${fechaSolo}T$horaFormateada")
                val sesionInstant = sesionDateTime.toInstant(zona)
                sesionInstant > nowInstant
            } catch (e: Exception) {
                println("❌ Error filtrando sesión: ${e.message}")
                false
            }
        }

        val sesionProxima = sesionesFuturas.firstOrNull { sesion ->
            try {

                val horaFormateada = if (sesion.hour.length == 5) "${sesion.hour}:00" else sesion.hour
                val fechaSolo = sesion.date.take(10) // "2025-06-03"
                val sesionDateTime = LocalDateTime.parse("${fechaSolo}T$horaFormateada")
                val sesionInstant = sesionDateTime.toInstant(zona)

                val diferencia = sesionInstant - nowInstant



                val enMargen = diferencia.inWholeMinutes in 0..60
                if (enMargen) println("✅ Sesión próxima en menos de una hora")
                enMargen
            } catch (e: Exception) {
                println("❌ Error al procesar sesión: ${e.message}")
                false
            }
        }

        if (sesionProxima != null) {
            val yaRespondido = useCase.cuestionarioEnviado(sesionProxima.id)

            if (!yaRespondido) {
                bookingIdPendiente = sesionProxima.id
                _cuestionarioActivo.value = true
            } else {
                println("⛔ Ya se respondió, no se mostrará el formulario.")
            }
        } else {
            println("🔕 No hay sesiones en la próxima hora.")
        }
    }

    fun responderPregunta(respuesta: String) {
        _respuestas[_preguntaActual.value] = respuesta
        if (_preguntaActual.value < 4) {
            _preguntaActual.value += 1
        } else {
            enviarRespuestas()
        }
    }

    fun omitirFormulario() {
        bookingIdPendiente?.let { sesionesOmitidas.add(it) }
        _cuestionarioActivo.value = false
    }

    private fun yaSeHaRespondido(bookingId: Int): Boolean {
        return sesionesOmitidas.contains(bookingId) ||
               _respuestas.all { it != null } && bookingId == bookingIdPendiente
    }

    private fun enviarRespuestas() {
        val bookingId = bookingIdPendiente ?: return
        viewModelScope.launch {
            try {
                val enviado = useCase.enviarCuestionarioReserva(
                    BookingQuestionnaireRequest(
                        booking_id = bookingId,
                        sleep_quality = respuestas[0]!!,
                        energy_level = respuestas[1]!!,
                        muscle_pain = respuestas[2]!!,
                        stress_level = respuestas[3]!!,
                        mood = respuestas[4]!!
                    )
                )
                if (enviado) {
                    _cuestionarioActivo.value = false
                }
            } catch (e: Exception) {
                println("❌ Error al enviar cuestionario: ${e.message}")
            }
        }
    }

    fun reiniciarFormulario() {
        _preguntaActual.value = 0
        _respuestas.clear()
        repeat(5) { _respuestas.add(null) }
    }

}

