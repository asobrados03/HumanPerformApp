package com.humanperformcenter.shared.domain.security

import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze

actual object EncryptionHandler {
    private val encryptorRef = AtomicReference<((ByteArray) -> ByteArray)?>(null)
    private val decryptorRef = AtomicReference<((ByteArray) -> ByteArray)?>(null)

    actual var encryptionCallback: ((ByteArray) -> ByteArray)?
        get() = encryptorRef.value
        set(value) {
            encryptorRef.value = value?.freeze()
        }

    actual var decryptionCallback: ((ByteArray) -> ByteArray)?
        get() = decryptorRef.value
        set(value) {
            decryptorRef.value = value?.freeze()
        }

    actual fun registerEncryptor(callback: (ByteArray) -> ByteArray) {
        encryptionCallback = callback
    }

    actual fun registerDecryptor(callback: (ByteArray) -> ByteArray) {
        decryptionCallback = callback
    }
}
