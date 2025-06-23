package com.humanperformcenter.shared.domain.security

actual object Base64 {

    actual fun encode(bytes: ByteArray): String = ""

    actual fun decode(str: String): ByteArray = ByteArray(12)
}