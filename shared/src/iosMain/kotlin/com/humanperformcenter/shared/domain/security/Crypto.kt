package com.humanperformcenter.shared.domain.security

actual object Crypto {
    actual fun encrypt(plain: ByteArray): ByteArray {
        return EncryptionHandler.encryptionCallback?.invoke(plain)
            ?: throw IllegalStateException("Encryption callback not set")
    }

    actual fun decrypt(cipherMessage: ByteArray): ByteArray {
        val result = EncryptionHandler.decryptionCallback?.invoke(cipherMessage)
            ?: throw IllegalStateException("Decryption callback not set")

        // Si Swift devolvió vacío, significa error de descifrado (padding, clave, etc.)
        if (result.isEmpty()) {
            throw CryptoException.DecryptionFailed
        }

        return result
    }
}