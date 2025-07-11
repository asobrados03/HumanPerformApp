package com.humanperformcenter.shared.domain.security
import com.humanperformcenter.shared.data.model.EncryptedResult

expect object Crypto_Pays {
    fun sha256(input: String): String
    fun encryptAES(input: String, key: String): EncryptedResult
}