package com.humanperformcenter.shared.domain.security

actual object EncryptionHandler {
    actual var encryptionCallback: ((ByteArray) -> ByteArray)? = null
    actual var decryptionCallback: ((ByteArray) -> ByteArray)? = null

    actual fun registerEncryptor(callback: (ByteArray) -> ByteArray) {
        encryptionCallback = callback
    }

    actual fun registerDecryptor(callback: (ByteArray) -> ByteArray) {
        decryptionCallback = callback
    }
}
