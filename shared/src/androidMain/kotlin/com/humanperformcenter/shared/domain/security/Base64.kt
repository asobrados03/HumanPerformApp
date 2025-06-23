package com.humanperformcenter.shared.domain.security

import java.util.Base64

actual object Base64 {
    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()

    actual fun encode(bytes: ByteArray): String =
        encoder.encodeToString(bytes)

    actual fun decode(str: String): ByteArray =
        decoder.decode(str)
}