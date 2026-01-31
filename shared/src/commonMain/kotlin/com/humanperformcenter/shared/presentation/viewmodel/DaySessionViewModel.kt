package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.ProductLimit
import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
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
import kotlinx.datetime.LocalDate

class DaySessionViewModel(
    private val useCase: DaySessionUseCase
) : ViewModel() {
    companion object {
        val log = logging()
    }

    private val _sessions = MutableStateFlow<DailySessionsUiState>(DailySessionsUiState.Idle)
    @NativeCoroutinesState
    val sessions: StateFlow<DailySessionsUiState> = _sessions.asStateFlow()

    private val _bookingErrorMessage = MutableStateFlow<String?>(null)
    @NativeCoroutinesState
    val bookingErrorMessage: StateFlow<String?> = _bookingErrorMessage.asStateFlow()

    private val _userBookingLimits = MutableStateFlow<List<ProductLimit>>(emptyList())
    @NativeCoroutinesState
    val userBookingLimits: StateFlow<List<ProductLimit>> = _userBookingLimits.asStateFlow()

    private val _coachesForHour = MutableStateFlow<List<DaySession>>(emptyList())
    @NativeCoroutinesState
    val coachesForHour: StateFlow<List<DaySession>> = _coachesForHour.asStateFlow()

    private val _holidays = MutableStateFlow<List<LocalDate>>(emptyList())
    @NativeCoroutinesState
    val holidays: StateFlow<List<LocalDate>> get() = _holidays.asStateFlow()

    fun fetchAvailableSessions(productId: Int, date: LocalDate) {
        _sessions.value = DailySessionsUiState.Loading

        log.debug { "📱 APP DEBUG: Llamando a fetch para ID: $productId en fecha: $date" }

        viewModelScope.launch(Dispatchers.IO) {
            val result = useCase.getSessionsByDay(productId, date)

            result.fold(
                onSuccess = { allSessions ->
                    val filtered = allSessions.filter { it.productId == productId }

                    if (filtered.isEmpty()) {
                        _sessions.value = DailySessionsUiState.Empty
                    } else {
                        _sessions.value = DailySessionsUiState.Success(filtered)
                    }
                    log.debug { "✅ Estado actualizado: ${filtered.size} sesiones." }
                },
                onFailure = { error ->
                    _sessions.value = DailySessionsUiState.Error(
                        error.message ?: "Error desconocido"
                    )
                }
            )
        }
    }

    fun filterCoachesByHour(hora: String) {
        val currentStatus = _sessions.value
        if (currentStatus is DailySessionsUiState.Success) {
            // Extraemos la lista del objeto Success para filtrar
            _coachesForHour.value = currentStatus.sessions.filter { it.hour == hora }
        } else {
            // Si no hay éxito (está cargando o hay error), la lista de coaches por hora queda vacía
            _coachesForHour.value = emptyList()
        }
    }

    fun clearCoachesForHour() {
        _coachesForHour.value = emptyList()
    }

    suspend fun makeBooking(
        customerId: Int,
        coachId: Int,
        serviceId: Int,
        productId: Int,
        dayOfWeek: String,
        centerId: Int,
        selectedDate: String,
        hour: String
    ) {
        useCase.getTimeslotId(serviceId, dayOfWeek, hour).onFailure { error ->
            log.error { "❌ Error obteniendo timeslotId: ${error.message}" }
            _bookingErrorMessage.value = error.message
            return
        }.onSuccess { timeslotId ->
            log.info { "🎯 Realizando reserva exacta:" }
            log.debug { "→ Service ID (Tabla Services): $serviceId" }
            log.debug { "→ Product ID (Tabla Products): $productId" }

            val bookingRequest = BookingRequest(
                customerId = customerId,
                coachId = coachId,
                sessionTimeslotId = timeslotId,
                serviceId = serviceId,
                productId = productId,
                centerId = centerId,
                startDate = selectedDate
            )

            useCase.makeBooking(bookingRequest).onSuccess {
                log.info { "✅ Reserva enviada correctamente" }
                _bookingErrorMessage.value = null
            }.onFailure { error ->
                log.error { "❌ Error al reservar: ${error.message}" }
                _bookingErrorMessage.value = error.message
            }
        }
    }

    suspend fun fetchServiceIdForProduct(productId: Int): Int? {
        return useCase.fetchServiceIdForProduct(productId).getOrNull()
    }

    fun modifyBookingSession(
        bookingId: Int,
        newCoachId: Int,
        newServiceId: Int,
        newProductId: Int,
        newDayOfWeek: String,
        newStartDate: String,
        hour: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            log.debug { "🕒 Buscando Timeslot para: Service=$newServiceId, Dia=$newDayOfWeek, Hora='$hour'" }
            useCase.getTimeslotId(newServiceId, newDayOfWeek, hour).onFailure { error ->
                log.error { "❌ Error obteniendo timeslotId: ${error.message}" }
                _bookingErrorMessage.value = error.message
                return@launch
            }.onSuccess { timeslotId ->
                val request = ReserveUpdateRequest(
                    booking_id = bookingId,
                    new_coach_id = newCoachId,
                    new_service_id = newServiceId,
                    new_product_id = newProductId,
                    new_session_timeslot_id = timeslotId,
                    new_start_date = newStartDate
                )

                useCase.modifyBookingSession(request).onSuccess {
                    log.info { "✅ Reserva actualizada correctamente" }
                    _bookingErrorMessage.value = null
                }.onFailure { error ->
                    log.error { "❌ Error al actualizar reserva: ${error.message}" }
                    _bookingErrorMessage.value = error.message
                }
            }
        }
    }

    fun clearSessions() {
        _sessions.value = DailySessionsUiState.Idle
    }

    fun fetchHolidays() {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.getHolidays().onSuccess { result ->
                _holidays.value = result.map { LocalDate.parse(it) }
            }.onFailure { error ->
                log.error { "❌ Error al cargar festivos: ${error.message}" }
                // Deberiamos dar algún feedback al usuario de que no se han podido cargar los festivos
            }
        }
    }

    fun fetchUserWeeklyLimit(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            log.info { "📡 Iniciando petición de límites..." }

            useCase.getUserWeeklyLimit(userId).onSuccess { response ->
                // Simplemente guardamos la lista de límites que ya viene calculada
                _userBookingLimits.value = response.weekly_limits
                log.debug { "✅ Límites cargados: ${_userBookingLimits.value.size} productos." }
            }.onFailure {
                log.error { "❌ Error al cargar límites semanales: ${it.message}" }
            }
        }
    }

    fun clearBookingErrorMessage() {
        _bookingErrorMessage.value = null
    }
}