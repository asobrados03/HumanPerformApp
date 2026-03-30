package com.humanperformcenter.shared.domain

sealed class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data class BadRequest(val details: String = "Solicitud inválida", val origin: Throwable? = null) :
        DomainException(details, origin)

    data class Unauthorized(val origin: Throwable? = null) :
        DomainException("No autorizado", origin)

    data class Forbidden(val origin: Throwable? = null) :
        DomainException("Acceso denegado", origin)

    data class NotFound(val origin: Throwable? = null) :
        DomainException("Recurso no encontrado", origin)

    data class Server(val statusCode: Int, val origin: Throwable? = null) :
        DomainException("Error del servidor ($statusCode)", origin)

    data class Network(val origin: Throwable? = null) :
        DomainException("Error de red", origin)

    data class Timeout(val origin: Throwable? = null) :
        DomainException("Tiempo de espera agotado", origin)

    data class Parsing(val origin: Throwable? = null) :
        DomainException("Error procesando la respuesta", origin)

    data class Unknown(val origin: Throwable? = null) :
        DomainException("Error inesperado", origin)
}

sealed class AuthDomainError(message: String, cause: Throwable? = null) : DomainException(message, cause) {
    data class InvalidCredentials(val origin: Throwable? = null) :
        AuthDomainError("Email o contraseña inválidos", origin)

    data class SessionExpired(val origin: Throwable? = null) :
        AuthDomainError("Tu sesión ha expirado", origin)

    data class AccessDenied(val origin: Throwable? = null) :
        AuthDomainError("No tienes permisos para esta acción", origin)

    data class UserNotFound(val origin: Throwable? = null) :
        AuthDomainError("Usuario no encontrado", origin)

    data class ServerFailure(val statusCode: Int, val origin: Throwable? = null) :
        AuthDomainError("Error del servidor en autenticación ($statusCode)", origin)

    data class NetworkFailure(val origin: Throwable? = null) :
        AuthDomainError("No se pudo conectar con el servicio de autenticación", origin)

    data class TimeoutFailure(val origin: Throwable? = null) :
        AuthDomainError("Tiempo de espera agotado en autenticación", origin)

    data class ParsingFailure(val origin: Throwable? = null) :
        AuthDomainError("No se pudo interpretar la respuesta de autenticación", origin)

    data class UnknownFailure(val origin: Throwable? = null) :
        AuthDomainError("Error inesperado en autenticación", origin)
}
