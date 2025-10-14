package com.humanperformcenter.shared.domain.security

actual object EncryptionHandler {
    actual var encryptionCallback: ((ByteArray) -> ByteArray)?
        get() = encryptionCallbackInternal
        set(value) {
            encryptionCallbackInternal = value
        }

    actual var decryptionCallback: ((ByteArray) -> ByteArray)?
        get() = decryptionCallbackInternal
        set(value) {
            decryptionCallbackInternal = value
        }

    actual fun registerEncryptor(callback: (ByteArray) -> ByteArray) {
        encryptionCallback = callback
    }

    actual fun registerDecryptor(callback: (ByteArray) -> ByteArray) {
        decryptionCallback = callback
    }

    private var encryptionCallbackInternal: ((ByteArray) -> ByteArray)? = null
    private var decryptionCallbackInternal: ((ByteArray) -> ByteArray)? = null
}
