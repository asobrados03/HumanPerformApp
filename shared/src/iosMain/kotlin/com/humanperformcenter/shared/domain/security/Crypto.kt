package com.humanperformcenter.shared.domain.security

actual object Crypto {
    private const val KEY_SIZE = 32 // 256 bits
    private const val IV_SIZE = 16 // AES block size

    // Clave hardcodeada por simplicidad - en producción deberías generarla/almacenarla de forma segura
    private val fixedKey = byteArrayOf(
        0x2b, 0x7e, 0x15, 0x16, 0x28, 0xa, 0xd2.toByte(), 0xa6.toByte(),
        0xab.toByte(), 0xf7.toByte(), 0x15, 0x1d, 0x09, 0xcf.toByte(), 0x4f, 0x3c,
        0x2b, 0x7e, 0x15, 0x16, 0x28, 0xa, 0xd2.toByte(), 0xa6.toByte(),
        0xab.toByte(), 0xf7.toByte(), 0x15, 0x29, 0x09, 0xcf.toByte(), 0x4f, 0x3c
    )

    private fun generateRandomIV(): ByteArray {
        val iv = ByteArray(IV_SIZE)
        for (i in 0 until IV_SIZE) {
            iv[i] = (0..255).random().toByte()
        }
        return iv
    }

    actual fun encrypt(plain: ByteArray): ByteArray {
        val iv = generateRandomIV()

        // Implementación simple de XOR (NO ES SEGURA PARA PRODUCCIÓN)
        // Solo para que compile y funcione básicamente
        val encrypted = ByteArray(plain.size)
        for (i in plain.indices) {
            encrypted[i] = (plain[i].toInt() xor fixedKey[i % fixedKey.size].toInt() xor iv[i % iv.size].toInt()).toByte()
        }

        // Concatenar IV + datos cifrados
        return iv + encrypted
    }

    actual fun decrypt(cipherMessage: ByteArray): ByteArray {
        if (cipherMessage.size < IV_SIZE) {
            throw IllegalArgumentException("Cipher message too short")
        }

        val iv = cipherMessage.copyOfRange(0, IV_SIZE)
        val encryptedData = cipherMessage.copyOfRange(IV_SIZE, cipherMessage.size)

        // Desencriptar usando XOR
        val decrypted = ByteArray(encryptedData.size)
        for (i in encryptedData.indices) {
            decrypted[i] = (encryptedData[i].toInt() xor fixedKey[i % fixedKey.size].toInt() xor iv[i % iv.size].toInt()).toByte()
        }

        return decrypted
    }
}