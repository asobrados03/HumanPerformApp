package com.humanperformcenter.shared.domain.security

actual object Crypto {
    actual fun encrypt(plain: ByteArray): ByteArray {
        return EncryptionHandler.encryptionCallback?.invoke(plain)
            ?: throw IllegalStateException("Encryption callback not set")
    }

    actual fun decrypt(cipherMessage: ByteArray): ByteArray {
        return EncryptionHandler.decryptionCallback?.invoke(cipherMessage)
            ?: throw IllegalStateException("Decryption callback not set")
    }
}