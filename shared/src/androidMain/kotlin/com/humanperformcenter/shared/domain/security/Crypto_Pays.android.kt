package com.humanperformcenter.shared.domain.security

import com.humanperformcenter.shared.data.model.EncryptedResult

actual object Crypto_Pays {
    actual fun sha256(input: String): String {
        TODO("Not yet implemented")
    }

    actual fun encryptAES(
        input: String,
        key: String
    ): EncryptedResult {
        TODO("Not yet implemented")
    }
}