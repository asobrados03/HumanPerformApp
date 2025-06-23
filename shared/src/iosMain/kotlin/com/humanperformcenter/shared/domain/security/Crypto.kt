package com.humanperformcenter.shared.domain.security

actual object Crypto {
    actual fun encrypt(plain: ByteArray): ByteArray = ByteArray(12)
    actual fun decrypt(cipherMessage: ByteArray): ByteArray = ByteArray(12)
}