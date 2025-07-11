package com.humanperformcenter.shared.domain.security

sealed class CryptoException(message: String? = null, cause: Throwable? = null) : Throwable(message, cause) {
    object DecryptionFailed : CryptoException("No se pudo descifrar el mensaje")
}
