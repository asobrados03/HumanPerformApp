package com.humanperformcenter.shared.domain.security

expect object Crypto {
    fun encrypt(plain: ByteArray): ByteArray
    fun decrypt(cipherMessage: ByteArray): ByteArray
}