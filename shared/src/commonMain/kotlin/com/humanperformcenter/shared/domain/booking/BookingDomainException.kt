package com.humanperformcenter.shared.domain.booking

sealed class BookingDomainException(message: String) : Exception(message) {
    data object WeeklyLimitExceeded : BookingDomainException("Has alcanzado tu máximo semanal.")
    data object TotalSessionsLimitExceeded : BookingDomainException("Has consumido todas las sesiones del bono.")
    data object DuplicateBooking : BookingDomainException("Ya tienes una reserva a esta hora.")
    class GenericBookingFailure(message: String) : BookingDomainException(message)
}

