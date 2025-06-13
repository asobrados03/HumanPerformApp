package com.humanperformcenter.shared.domain.usecase.validation

sealed class ChangePasswordException(message: String) : Exception(message) {
    object CurrentRequired   : ChangePasswordException("La contraseña actual es requerida")
    object NewRequired       : ChangePasswordException("La nueva contraseña es requerida")
    object ConfirmRequired   : ChangePasswordException("La confirmación de la nueva contraseña es requerida")
    object TooShort          : ChangePasswordException("La nueva contraseña debe tener al menos 8 caracteres")
    object NotMatching       : ChangePasswordException("Las contraseñas no coinciden")
    object NoNumber          : ChangePasswordException("La nueva contraseña debe contener al menos un número")
    object NoUppercase       : ChangePasswordException("La nueva contraseña debe contener al menos una mayúscula")
    object NoLowercase       : ChangePasswordException("La nueva contraseña debe contener al menos una minúscula")
    object SameAsCurrent     : ChangePasswordException("La nueva contraseña debe ser diferente a la actual")
    object ContainsSpace     : ChangePasswordException("La contraseña no puede contener espacios")
    class  RepoFailure(cause: Throwable) : ChangePasswordException(cause.message ?: "Error al cambiar contraseña")
}
