package com.humanperformcenter.shared.domain.security

expect object EncryptionHandler {
    var encryptionCallback: ((ByteArray) -> ByteArray)?
    var decryptionCallback: ((ByteArray) -> ByteArray)?

    fun registerEncryptor(callback: (ByteArray) -> ByteArray)
    fun registerDecryptor(callback: (ByteArray) -> ByteArray)
}
