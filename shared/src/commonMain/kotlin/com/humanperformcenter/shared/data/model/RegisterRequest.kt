package com.humanperformcenter.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val nombre: String,
    val apellidos: String,
    val email: String,
    val telefono: String,
    val password: String,
    val sexo: String,
    @SerialName("fecha_nacimiento")
    val fechaNacimiento: String, // formato "YYYY-MM-DD"
    val codigoPostal: String,
    val direccionPostal: String,
    val dni: String,
    val profilePicBytes: ByteArray?,
    val profilePicName: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RegisterRequest

        if (nombre != other.nombre) return false
        if (apellidos != other.apellidos) return false
        if (email != other.email) return false
        if (telefono != other.telefono) return false
        if (password != other.password) return false
        if (sexo != other.sexo) return false
        if (fechaNacimiento != other.fechaNacimiento) return false
        if (codigoPostal != other.codigoPostal) return false
        if (dni != other.dni) return false
        if (!profilePicBytes.contentEquals(other.profilePicBytes)) return false
        if (profilePicName != other.profilePicName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nombre.hashCode()
        result = 31 * result + apellidos.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + telefono.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + sexo.hashCode()
        result = 31 * result + fechaNacimiento.hashCode()
        result = 31 * result + codigoPostal.hashCode()
        result = 31 * result + dni.hashCode()
        result = 31 * result + (profilePicBytes?.contentHashCode() ?: 0)
        result = 31 * result + (profilePicName?.hashCode() ?: 0)
        return result
    }
}
