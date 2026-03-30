package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.domain.AuthDomainError
import com.humanperformcenter.shared.domain.DomainException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

internal enum class ErrorCategory {
    AUTH,
    GENERIC,
}

internal fun <T> Result<T>.mapDomainError(category: ErrorCategory = ErrorCategory.GENERIC): Result<T> {
    val failure = exceptionOrNull() ?: return this
    val mapped = failure.toDomainException(category)
    return Result.failure(mapped)
}

private fun Throwable.toDomainException(category: ErrorCategory): DomainException {
    val statusCode = extractHttpStatusCode()
    if (statusCode != null) {
        return statusToDomainError(statusCode, category, this)
    }

    return when {
        this is HttpRequestTimeoutException -> timeoutDomainError(category, this)
        this is IOException || this is UnresolvedAddressException -> networkDomainError(category, this)
        isParsingFailure() -> parsingDomainError(category, this)
        else -> unknownDomainError(category, this)
    }
}

private fun Throwable.extractHttpStatusCode(): Int? {
    if (this is ResponseException) return response.status.value

    val message = message ?: return null
    val match = Regex("HTTP\\s+(\\d{3})").find(message)
    return match?.groupValues?.getOrNull(1)?.toIntOrNull()
}

private fun Throwable.isParsingFailure(): Boolean =
    this is SerializationException ||
        this is NoTransformationFoundException ||
        setOf("JsonConvertException", "ContentConvertException").contains(this::class.simpleName)

private fun statusToDomainError(statusCode: Int, category: ErrorCategory, origin: Throwable): DomainException =
    when (statusCode) {
        400 -> if (category == ErrorCategory.AUTH) {
            AuthDomainError.InvalidCredentials(origin)
        } else {
            DomainException.BadRequest(origin = origin)
        }

        401 -> if (category == ErrorCategory.AUTH) {
            AuthDomainError.SessionExpired(origin)
        } else {
            DomainException.Unauthorized(origin)
        }

        403 -> if (category == ErrorCategory.AUTH) {
            AuthDomainError.AccessDenied(origin)
        } else {
            DomainException.Forbidden(origin)
        }

        404 -> if (category == ErrorCategory.AUTH) {
            AuthDomainError.UserNotFound(origin)
        } else {
            DomainException.NotFound(origin)
        }

        in 500..599 -> if (category == ErrorCategory.AUTH) {
            AuthDomainError.ServerFailure(statusCode, origin)
        } else {
            DomainException.Server(statusCode, origin)
        }

        else -> unknownDomainError(category, origin)
    }

private fun timeoutDomainError(category: ErrorCategory, origin: Throwable): DomainException =
    if (category == ErrorCategory.AUTH) {
        AuthDomainError.TimeoutFailure(origin)
    } else {
        DomainException.Timeout(origin)
    }

private fun networkDomainError(category: ErrorCategory, origin: Throwable): DomainException =
    if (category == ErrorCategory.AUTH) {
        AuthDomainError.NetworkFailure(origin)
    } else {
        DomainException.Network(origin)
    }

private fun parsingDomainError(category: ErrorCategory, origin: Throwable): DomainException =
    if (category == ErrorCategory.AUTH) {
        AuthDomainError.ParsingFailure(origin)
    } else {
        DomainException.Parsing(origin)
    }

private fun unknownDomainError(category: ErrorCategory, origin: Throwable): DomainException =
    if (category == ErrorCategory.AUTH) {
        AuthDomainError.UnknownFailure(origin)
    } else {
        DomainException.Unknown(origin)
    }
