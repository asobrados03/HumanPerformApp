package com.humanperformcenter.shared.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val surnames: String,
    val email: String,
    val phone: String,
    val password: String,
    val sex: String,
    val dateOfBirth: String,
    val postCode: String,
    val postAddress: String,
    val dni: String,
    val deviceType: String,
    val profilePicBytes: ByteArray?,
    val profilePicName: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RegisterRequest

        if (name != other.name) return false
        if (surnames != other.surnames) return false
        if (email != other.email) return false
        if (phone != other.phone) return false
        if (password != other.password) return false
        if (sex != other.sex) return false
        if (dateOfBirth != other.dateOfBirth) return false
        if (postCode != other.postCode) return false
        if (dni != other.dni) return false
        if (!profilePicBytes.contentEquals(other.profilePicBytes)) return false
        if (profilePicName != other.profilePicName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + surnames.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + sex.hashCode()
        result = 31 * result + dateOfBirth.hashCode()
        result = 31 * result + postCode.hashCode()
        result = 31 * result + dni.hashCode()
        result = 31 * result + (profilePicBytes?.contentHashCode() ?: 0)
        result = 31 * result + (profilePicName?.hashCode() ?: 0)
        return result
    }
}
