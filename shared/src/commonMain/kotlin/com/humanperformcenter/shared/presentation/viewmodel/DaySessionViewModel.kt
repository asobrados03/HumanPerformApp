package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.domain.booking.BookingDomainException
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import com.humanperformcenter.shared.presentation.ui.DailySessionsUiState
import com.humanperformcenter.shared.presentation.ui.SessionsRequestContext
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.Job
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

class DaySessionViewModel(
    private val useCase: DaySessionUseCase
) : ViewModel() {
    companion object {
        val log = logging()
        private const val GENERIC_BOOKING_ERROR_MESSAGE = "No se pudo completar la reserva. Inténtalo de nuevo más tarde."
    }

    private val _sessions = MutableStateFlow<DailySessionsUiState>(viewModelScope, DailySessionsUiState.Idle)
    private var fetchSessionsRequestId: Long = 0
    private var fetchSessionsJob: Job? = null
    @NativeCoroutinesState
    val sessions: StateFlow<DailySessionsUiState> = _sessions.asStateFlow()

    private val _bookingErrorMessage = MutableStateFlow<String?>(viewModelScope, null)
    @NativeCoroutinesState
    val bookingErrorMessage: StateFlow<String?> = _bookingErrorMessage.asStateFlow()

    private val _holidays = MutableStateFlow<List<LocalDate>>(viewModelScope, emptyList())
    @NativeCoroutinesState
    val holidays: StateFlow<List<LocalDate>> get() = _holidays.asStateFlow()

    fun fetchAvailableSessions(productId: Int, date: LocalDate) {
        val requestContext = SessionsRequestContext(productId = productId, date = date)
        val requestId = ++fetchSessionsRequestId

        fetchSessionsJob?.cancel()
        _sessions.value = DailySessionsUiState.Loading(requestContext)

        log.debug { "📱 APP DEBUG: Llamando a fetch para ID: $productId en fecha: $date, requestId=$requestId" }

        fetchSessionsJob = viewModelScope.launch {
            val result = useCase.getSessionsByDay(productId, date)

            if (requestId != fetchSessionsRequestId) {
                log.debug { "⏭️ Ignorando respuesta obsoleta de sesiones. requestId=$requestId, activo=$fetchSessionsRequestId" }
                return@launch
            }

            result.fold(
                onSuccess = { allSessions ->
                    val filtered = allSessions.filter { it.productId == productId }

                    if (filtered.isEmpty()) {
                        _sessions.value = DailySessionsUiState.Empty(requestContext)
                    } else {
                        _sessions.value = DailySessionsUiState.Success(filtered, requestContext)
                    }
                    log.debug { "✅ Estado actualizado: ${filtered.size} sesiones. requestId=$requestId" }
                },
                onFailure = { error ->
                    _sessions.value = DailySessionsUiState.Error(
                        message = error.message ?: "Error desconocido",
                        context = requestContext
                    )
                }
            )
        }
    }

    fun getAvailableCoachesForHour(hour: String): List<DaySession> {
        val currentStatus = _sessions.value as? DailySessionsUiState.Success ?: return emptyList()
        return currentStatus.sessions.filter { it.hour == hour && it.booked < it.capacity }
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
    ): Boolean {
        val timeslotId = useCase.getTimeslotId(serviceId, dayOfWeek, hour).fold(
            onSuccess = { it },
            onFailure = { error ->
                log.error { "❌ Error obteniendo timeslotId: ${error.message}" }
                _bookingErrorMessage.value = error.toBookingErrorMessage()
                return false
            }
        )

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

        return useCase.makeBooking(bookingRequest).fold(
            onSuccess = {
                log.info { "✅ Reserva enviada correctamente" }
                _bookingErrorMessage.value = null
                true
            },
            onFailure = { error ->
                log.error { "❌ Error al reservar: ${error.message}" }
                _bookingErrorMessage.value = error.toBookingErrorMessage()
                false
            }
        )
    }

    suspend fun fetchServiceIdForProduct(productId: Int): Int? {
        return useCase.fetchServiceIdForProduct(productId).getOrNull()
    }


    fun fetchServiceIdForProductAsync(productId: Int, onResult: (Int?) -> Unit) {
        viewModelScope.launch {
            onResult(fetchServiceIdForProduct(productId))
        }
    }

    fun makeBookingAsync(
        customerId: Int,
        coachId: Int,
        serviceId: Int,
        productId: Int,
        dayOfWeek: String,
        centerId: Int,
        selectedDate: String,
        hour: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            onResult(
                makeBooking(
                    customerId = customerId,
                    coachId = coachId,
                    serviceId = serviceId,
                    productId = productId,
                    dayOfWeek = dayOfWeek,
                    centerId = centerId,
                    selectedDate = selectedDate,
                    hour = hour
                )
            )
        }
    }

    fun modifyBookingSessionAsync(
        bookingId: Int,
        newCoachId: Int,
        newServiceId: Int,
        newProductId: Int,
        newDayOfWeek: String,
        newStartDate: String,
        hour: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            onResult(
                modifyBookingSession(
                    bookingId = bookingId,
                    newCoachId = newCoachId,
                    newServiceId = newServiceId,
                    newProductId = newProductId,
                    newDayOfWeek = newDayOfWeek,
                    newStartDate = newStartDate,
                    hour = hour
                )
            )
        }
    }

    suspend fun modifyBookingSession(
        bookingId: Int,
        newCoachId: Int,
        newServiceId: Int,
        newProductId: Int,
        newDayOfWeek: String,
        newStartDate: String,
        hour: String
    ): Boolean {
        log.debug { "🕒 Buscando Timeslot para: Service=$newServiceId, Dia=$newDayOfWeek, Hora='$hour'" }
        val timeslotId = useCase.getTimeslotId(newServiceId, newDayOfWeek, hour).fold(
            onSuccess = { it },
            onFailure = { error ->
                log.error { "❌ Error obteniendo timeslotId: ${error.message}" }
                _bookingErrorMessage.value = error.toBookingErrorMessage()
                return false
            }
        )

        val request = ReserveUpdateRequest(
            booking_id = bookingId,
            new_coach_id = newCoachId,
            new_service_id = newServiceId,
            new_product_id = newProductId,
            new_session_timeslot_id = timeslotId,
            new_start_date = newStartDate
        )

        return useCase.modifyBookingSession(request).fold(
            onSuccess = {
                log.info { "✅ Reserva actualizada correctamente" }
                _bookingErrorMessage.value = null
                true
            },
            onFailure = { error ->
                log.error { "❌ Error al actualizar reserva: ${error.message}" }
                _bookingErrorMessage.value = error.toBookingErrorMessage()
                false
            }
        )
    }

    fun clearSessions() {
        fetchSessionsJob?.cancel()
        _sessions.value = DailySessionsUiState.Idle
    }

    fun fetchHolidays() {
        viewModelScope.launch {
            useCase.getHolidays().onSuccess { result ->
                _holidays.value = result.map { LocalDate.parse(it) }
            }.onFailure { error ->
                log.error { "❌ Error al cargar festivos: ${error.message}" }
                // Deberiamos dar algún feedback al usuario de que no se han podido cargar los festivos
            }
        }
    }

    fun clearBookingErrorMessage() {
        _bookingErrorMessage.value = null
    }

    private fun Throwable.toBookingErrorMessage(): String {
        return when (this) {
            is BookingDomainException -> message ?: GENERIC_BOOKING_ERROR_MESSAGE
            else -> GENERIC_BOOKING_ERROR_MESSAGE
        }
    }
}
