package com.humanperformcenter.shared.domain.security

object EncryptionHandler {
    var encryptionCallback: ((ByteArray) -> ByteArray)? = null
    var decryptionCallback: ((ByteArray) -> ByteArray)? = null

    fun registerEncryptor(callback: (ByteArray) -> ByteArray) {
        encryptionCallback = callback
    }

    fun registerDecryptor(callback: (ByteArray) -> ByteArray) {
        decryptionCallback = callback
    }
}
