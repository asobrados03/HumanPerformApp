package com.humanperformcenter.shared.domain.security

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create

actual object Base64 {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun encode(bytes: ByteArray): String {
        return memScoped {
            val nsData = bytes.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = bytes.size.convert()
                )
            }
            nsData.base64EncodedStringWithOptions(0u)
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun decode(str: String): ByteArray {
        val nsData = NSData.create(base64EncodedString = str, options = 0u)
            ?: return ByteArray(0)

        return ByteArray(nsData.length.toInt()).apply {
            usePinned { pinned ->
                platform.posix.memcpy(
                    pinned.addressOf(0),
                    nsData.bytes,
                    nsData.length
                )
            }
        }
    }
}