package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.ReserveRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.SharedPool
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import com.humanperformcenter.shared.presentation.ui.DailySessionsUiState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.time.Clock

class DaySessionViewModel(
    private val useCase: DaySessionUseCase
) : ViewModel() {
    private val _sessions = MutableStateFlow<List<DaySession>>(emptyList())
    @NativeCoroutinesState
    val sessions: StateFlow<List<DaySession>> get() = _sessions

    private val _mensajeErrorReserva = MutableStateFlow<String?>(null)
    @NativeCoroutinesState
    val mensajeErrorReserva: StateFlow<String?> = _mensajeErrorReserva

    private val _weeklyLimits = MutableStateFlow<Map<Int, Int>>(emptyMap())
    @NativeCoroutinesState
    val weeklyLimits: StateFlow<Map<Int, Int>> = _weeklyLimits

    private val _unlimitedSessions = MutableStateFlow<Map<Int, Int>>(emptyMap())
    @NativeCoroutinesState
    val unlimitedSessions: StateFlow<Map<Int, Int>> get() = _unlimitedSessions

    private val _sharedSessions = MutableStateFlow<List<SharedPool>>(emptyList())
    @NativeCoroutinesState
    val sharedSessions: StateFlow<List<SharedPool>> get() = _sharedSessions

    private val _serviceToPrimary = MutableStateFlow<Map<Int, Int>>(emptyMap())
    @NativeCoroutinesState
    val serviceToPrimary: StateFlow<Map<Int, Int>> get() = _serviceToPrimary

    private val _validFromByPrimary = MutableStateFlow<Map<Int, String>>(emptyMap())
    @NativeCoroutinesState
    val validFromByPrimary: StateFlow<Map<Int, String>> get() = _validFromByPrimary

    private val _cuestionarioActivo = MutableStateFlow(false)
    @NativeCoroutinesState
    val cuestionarioActivo: StateFlow<Boolean> = _cuestionarioActivo

    private val _preguntaActual = MutableStateFlow(0)
    @NativeCoroutinesState
    val preguntaActual: StateFlow<Int> = _preguntaActual

    private val sesionesOmitidas = mutableListOf<Int>()

    private var bookingIdPendiente: Int? = null

    private val _coachesForHour = MutableStateFlow<List<DaySession>>(emptyList())
    @NativeCoroutinesState
    val coachesForHour: StateFlow<List<DaySession>> = _coachesForHour.asStateFlow()

    private val _dailySessionsState = MutableStateFlow<DailySessionsUiState>(DailySessionsUiState.Loading)
    @NativeCoroutinesState
    val dailySessionsState = _dailySessionsState.asStateFlow()

    private val _holidays = MutableStateFlow<List<LocalDate>>(emptyList())
    @NativeCoroutinesState
    val holidays: StateFlow<List<LocalDate>> get() = _holidays

    fun fetchAvailableSessions(serviceId: Int, date: LocalDate) {
        // 1. Preparamos el estado inicial
        _dailySessionsState.value = DailySessionsUiState.Loading

        viewModelScope.launch { // Eliminamos Dispatchers.IO aquí (se encarga el Repo/UseCase)

            // 2. Llamada al UseCase que ahora devuelve Result<List<DaySession>>
            val result = useCase.getSessionsByDay(serviceId, date)

            result.fold(
                onSuccess = { allSessions ->
                    // 3. Filtrado (Si el backend ya filtra por serviceId, esto es opcional pero seguro)
                    val filtered = allSessions.filter { it.serviceId == serviceId }

                    // 4. Actualizamos el estado de éxito
                    _dailySessionsState.value = DailySessionsUiState.Success(filtered)

                    // Logs limpios para depuración
                    println("✅ Sesiones cargadas: ${filtered.size} para el servicio $serviceId")
                },
                onFailure = { error ->
                    // 5. Gestión de error unificada
                    _dailySessionsState.value = DailySessionsUiState.Error(error.message ?: "Error al cargar sesiones")
                    println("❌ ERROR: ${error.message}")
                }
            )
        }
    }

    fun filtrarEntrenadoresPorHora(hora: String) {
        _coachesForHour.value = _sessions.value.filter { it.hour == hora }
    }

    fun clearCoachesForHour() {
        _coachesForHour.value = emptyList()
    }

    suspend fun realizarReserva(
        customerId: Int,
        coachId: Int,
        serviceId: Int,
        centerId: Int,
        selectedDate: String,
        hour: String
    ) {
        useCase.getUserProductId(customerId).onFailure { error ->
                println("❌ Error obteniendo productId: ${error.message}")
                _mensajeErrorReserva.value = error.message
                return
        }.onSuccess { productId ->
            useCase.getTimeslotId(hour).onFailure { error ->
                    println("❌ Error obteniendo timeslotId: ${error.message}")
                    _mensajeErrorReserva.value = error.message
                    return
            }.onSuccess { timeslotId ->
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

                useCase.reservarSesion(reserva).onSuccess {
                    println("✅ Reserva enviada correctamente")
                    _mensajeErrorReserva.value = null
                }.onFailure { error ->
                    println("❌ Error al reservar: ${error.message}")
                    _mensajeErrorReserva.value = error.message
                }
            }
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
        viewModelScope.launch(Dispatchers.IO) {

            useCase.getUserProductId(customerId).onFailure { error ->
                println("❌ Error obteniendo productId: ${error.message}")
                _mensajeErrorReserva.value = error.message
                return@launch
            }
            .onSuccess { productId ->

                useCase.getTimeslotId(hour).onFailure { error ->
                    println("❌ Error obteniendo timeslotId: ${error.message}")
                    _mensajeErrorReserva.value = error.message
                    return@launch
                }.onSuccess { timeslotId ->
                    val request = ReserveUpdateRequest(
                        booking_id = bookingId,
                        new_coach_id = newCoachId,
                        new_service_id = newServiceId,
                        new_product_id = productId,
                        new_session_timeslot_id = timeslotId,
                        new_start_date = newStartDate
                    )

                    useCase.cambiarReservaSesion(request).onSuccess {
                        println("✅ Reserva actualizada correctamente")
                        _mensajeErrorReserva.value = null
                    }.onFailure { error ->
                        println("❌ Error al actualizar reserva: ${error.message}")
                        _mensajeErrorReserva.value = error.message
                    }
                }
            }
        }
    }

    fun clearSessions() {
        _sessions.value = emptyList()
    }

    suspend fun getPreferredCoachId(
        customerId: Int,
        serviceId: Int
    ): Int? {
        return useCase.getPreferredCoach(customerId, serviceId).onFailure { error ->
            println("❌ Error obteniendo coach preferido: ${error.message}")
        }.getOrNull()
    }

    fun fetchHolidays() {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.getHolidays().onSuccess { result ->
                _holidays.value = result.map { LocalDate.parse(it) }
            }.onFailure { error ->
                println("❌ Error al cargar festivos: ${error.message}")
            }
        }
    }

    fun fetchUserWeeklyLimit(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.getUserWeeklyLimit(userId).onSuccess { response ->
                    _weeklyLimits.value = response.weekly_limit
                    _unlimitedSessions.value = response.unlimited_sessions
                    _sharedSessions.value = response.unlimited_shared
                    _serviceToPrimary.value = response.service_to_primary
                    _validFromByPrimary.value = response.valid_from_by_primary
            }.onFailure { error ->
                println("❌ Error al cargar límites semanales: ${error.message}")
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
        bookings: List<UserBooking>,
        serviceToPrimary: Map<Int, Int>,
        validFromByPrimary: Map<Int, String> = emptyMap()
    ): Boolean {
        if (serviceId == null) return false

        val primaryId = serviceToPrimary[serviceId] ?: serviceId
        val weekStart = selectedDate.minus(selectedDate.dayOfWeek.ordinal.toLong(), DateTimeUnit.DAY)
        val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)

        // Preprocesar bookings: mapear a primary + parsear fecha una sola vez
        val processedBookings = bookings.mapNotNull { booking ->
            val primary = serviceToPrimary[booking.service_id] ?: booking.service_id
            val date = booking.date.takeIf { it.length >= 10 }
                ?.substring(0, 10)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: return@mapNotNull null

            ProcessedBooking(primary, date)
        }

        // Caso recurrente (límite semanal)
        if (primaryId in weeklyLimits) {
            val limit = weeklyLimits[primaryId]!!
            val countThisWeek = processedBookings.count {
                it.primaryId == primaryId && it.date in weekStart..weekEnd
            }
            return countThisWeek >= limit
        }

        // Caso bonos: dedicados + pools compartidos
        val validFromDate = validFromByPrimary[primaryId]?.let(::parseDateOrNull)

        val usedDedicated = processedBookings.count {
            it.primaryId == primaryId &&
                    (validFromDate == null || it.date >= validFromDate)
        }

        val dedicatedAllowed = unlimitedSessions[primaryId] ?: 0
        val dedicatedRemaining = (dedicatedAllowed - usedDedicated).coerceAtLeast(0)

        val sharedRemaining = sharedSessions
            .filter { primaryId in it.services }
            .sumOf { pool ->
                val poolFrom = pool.valid_from?.let(::parseDateOrNull)
                val poolTo = pool.valid_to?.let(::parseDateOrNull)

                val usedInPool = processedBookings.count { b ->
                    b.primaryId in pool.services &&
                            (poolFrom == null || b.date >= poolFrom) &&
                            (poolTo == null || b.date <= poolTo)
                }

                (pool.sessions - usedInPool).coerceAtLeast(0)
            }

        val totalAvailable = dedicatedRemaining + sharedRemaining
        return totalAvailable <= 0
    }

    // Data class auxiliar
    private data class ProcessedBooking(
        val primaryId: Int?,
        val date: LocalDate
    )

    suspend fun cargarFormularioSiProcede(proximasSesiones: List<UserBooking>) {
        val zona = TimeZone.currentSystemDefault()

        // Simplificación: Obtén el Instant actual directamente (es más preciso para comparaciones)
        val nowInstant = Clock.System.now()

        val sesionesFuturas = proximasSesiones.filter { sesion ->
            try {
                // Aseguramos formato ISO-8601: "YYYY-MM-DDTHH:MM:SS"
                val horaFormateada = when (sesion.hour.length) {
                    5 -> "${sesion.hour}:00" // "10:30" -> "10:30:00"
                    8 -> sesion.hour         // "10:30:00" -> se queda igual
                    else -> sesion.hour      // Manejo de otros casos
                }
                val fechaSolo = sesion.date.take(10) // "YYYY-MM-DD"

                val sesionDateTime = LocalDateTime.parse("${fechaSolo}T$horaFormateada")
                val sesionInstant = sesionDateTime.toInstant(zona)

                sesionInstant > nowInstant
            } catch (e: Exception) {
                println("❌ Error filtrando sesión: ${e.message}")
                false
            }
        }

        // Buscamos la primera sesión que ocurra en los próximos 60 minutos
        val sesionProxima = sesionesFuturas.firstOrNull { sesion ->
            try {
                val horaFormateada = if (sesion.hour.length == 5) "${sesion.hour}:00" else sesion.hour
                val sesionDateTime = LocalDateTime.parse("${sesion.date.take(10)}T$horaFormateada")
                val sesionInstant = sesionDateTime.toInstant(zona)

                // Usamos Duration para una comparación más legible
                val diferencia = sesionInstant - nowInstant

                // inWholeMinutes devuelve 0 para segundos restantes, lo cual es correcto
                val enMargen = diferencia.inWholeMinutes in 0..60

                if (enMargen) println("✅ Sesión próxima (${sesion.id}) en menos de una hora")
                enMargen
            } catch (_: Exception) {
                false
            }
        }

        // Lógica de UI/Negocio
        sesionProxima?.let { sesion ->
            useCase.cuestionarioEnviado(sesion.id).onSuccess { yaRespondido ->
                if (!yaRespondido) {
                    bookingIdPendiente = sesion.id
                    _cuestionarioActivo.value = true
                } else {
                    println("⛔ Ya se respondió para la sesión: ${sesion.id}")
                }
            }.onFailure { error ->
                println("❌ Error comprobando cuestionario: ${error.message}")
            }
        } ?: println("🔕 No hay sesiones en la próxima hora.")
    }
}