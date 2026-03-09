package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.ProductLimit
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
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _sessions = MutableStateFlow<DailySessionsUiState>(DailySessionsUiState.Idle)
    private var fetchSessionsRequestId: Long = 0
    private var fetchSessionsJob: Job? = null
    @NativeCoroutinesState
    val sessions: StateFlow<DailySessionsUiState> = _sessions.asStateFlow()

    private val _bookingErrorMessage = MutableStateFlow<String?>(null)
    @NativeCoroutinesState
    val bookingErrorMessage: StateFlow<String?> = _bookingErrorMessage.asStateFlow()

    private val _userBookingLimits = MutableStateFlow<List<ProductLimit>>(emptyList())
    @NativeCoroutinesState
    val userBookingLimits: StateFlow<List<ProductLimit>> = _userBookingLimits.asStateFlow()

    private val _holidays = MutableStateFlow<List<LocalDate>>(emptyList())
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
        val currentLimit = useCase.getUserWeeklyLimit(customerId).fold(
            onSuccess = { wrapper ->
                wrapper.weekly_limits.find { it.productId == productId }
            },
            onFailure = { error ->
                log.error { "❌ Error obteniendo límites antes de reservar: ${error.message}" }
                _bookingErrorMessage.value = error.toBookingErrorMessage()
                return false
            }
        )

        val limitValidationError = validateBookingLimit(currentLimit)
        if (limitValidationError != null) {
            _bookingErrorMessage.value = limitValidationError.message
            log.info { "⛔ Reserva bloqueada por límite de producto: ${limitValidationError.message}" }
            return false
        }

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
                _bookingErrorMessage.value = resolveBookingErrorMessage(
                    throwable = error,
                    customerId = customerId,
                    productId = productId
                )
                false
            }
        )
    }

    private suspend fun resolveBookingErrorMessage(
        throwable: Throwable,
        customerId: Int,
        productId: Int
    ): String {
        if (throwable is BookingDomainException && throwable !is BookingDomainException.GenericBookingFailure) {
            return throwable.message ?: GENERIC_BOOKING_ERROR_MESSAGE
        }

        val refreshedLimit = useCase.getUserWeeklyLimit(customerId).fold(
            onSuccess = { wrapper -> wrapper.weekly_limits.find { it.productId == productId } },
            onFailure = {
                log.error { "❌ No se pudieron refrescar límites tras error de reserva: ${it.message}" }
                null
            }
        )

        val refreshedLimitError = validateBookingLimit(refreshedLimit)
        if (refreshedLimitError != null) {
            return refreshedLimitError.message ?: GENERIC_BOOKING_ERROR_MESSAGE
        }

        return throwable.toBookingErrorMessage()
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
        viewModelScope.launch {
            log.debug { "🕒 Buscando Timeslot para: Service=$newServiceId, Dia=$newDayOfWeek, Hora='$hour'" }
            useCase.getTimeslotId(newServiceId, newDayOfWeek, hour).onFailure { error ->
                log.error { "❌ Error obteniendo timeslotId: ${error.message}" }
                _bookingErrorMessage.value = error.toBookingErrorMessage()
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
                    _bookingErrorMessage.value = error.toBookingErrorMessage()
                }
            }
        }
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

    fun fetchUserWeeklyLimit(userId: Int) {
        viewModelScope.launch {
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

    private fun validateBookingLimit(limit: ProductLimit?): BookingDomainException? {
        if (limit == null) return null // Sin límites definidos o no se encontró el producto

        val isRecurring = limit.typeOfProduct.isRecurringType()
        val isPack = limit.typeOfProduct.isPackType()

        // 1. Lógica para Suscripciones / Recurrentes
        if (isRecurring && limit.weeklyLimit != null) {
            // Si es recurrente, asumimos que 'remaining' indica lo que le queda ESTA semana.
            limit.remaining?.let {
                if (it <= 0) {
                    return BookingDomainException.WeeklyLimitExceeded
                }
            }
        }

        // 2. Lógica para Bonos / Packs
        if (isPack && limit.totalLimit != null) {
            // Si es un bono, asumimos que 'remaining' indica el total que le queda al bono.
            limit.remaining?.let {
                if (it <= 0) {
                    return BookingDomainException.TotalSessionsLimitExceeded
                }
            }
        }

        // 3. Fallbacks de seguridad (por si el backend manda un tipo de producto raro pero sí manda límites)
        limit.remaining?.let {
            if (it <= 0) {
                return when {
                    limit.weeklyLimit != null -> BookingDomainException.WeeklyLimitExceeded
                    limit.totalLimit != null -> BookingDomainException.TotalSessionsLimitExceeded
                    else -> null
                }
            }
        }

        // 4. Si llegamos aquí, 'remaining' es mayor que 0 y no ha roto las reglas anteriores
        return null
    }

    private fun String.isRecurringType(): Boolean {
        val normalized = lowercase()
        return normalized in listOf("recurrent", "subscription", "suscription", "recurrente", "suscripcion", "suscripción")
    }

    private fun String.isPackType(): Boolean {
        val normalized = lowercase()
        return normalized == "bonus" || normalized == "bono" || normalized == "single_session"
    }

    private fun Throwable.toBookingErrorMessage(): String {
        return when (this) {
            is BookingDomainException -> message ?: GENERIC_BOOKING_ERROR_MESSAGE
            else -> GENERIC_BOOKING_ERROR_MESSAGE
        }
    }
}
