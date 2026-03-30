package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.domain.usecase.validation.ChangePasswordException

class AuthUseCase(
    private val authRepository: AuthRepository,
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return authRepository.login(email, password)
    }

    suspend fun register(data: RegisterRequest): Result<RegisterResponse> {
        return authRepository.register(data)
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return authRepository.resetPassword(email)
    }

    suspend fun logout(): Result<Unit> {
        val remoteLogoutResult = authRepository.logout()
        authLocalDataSource.clear()
        return remoteLogoutResult
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        userId: Int
    ): Result<Unit> {
        return try {
            // — Validaciones de negocio; lanzan ChangePasswordException
            when {
                currentPassword.isBlank()       -> throw ChangePasswordException.CurrentRequired
                newPassword.isBlank()           -> throw ChangePasswordException.NewRequired
                confirmPassword.isBlank()       -> throw ChangePasswordException.ConfirmRequired
                newPassword.length < 8          -> throw ChangePasswordException.TooShort
                newPassword != confirmPassword  -> throw ChangePasswordException.NotMatching
                !newPassword.contains("\\d".toRegex())  -> throw ChangePasswordException.NoNumber
                !newPassword.contains("[A-Z]".toRegex()) -> throw ChangePasswordException.NoUppercase
                !newPassword.contains("[a-z]".toRegex()) -> throw ChangePasswordException.NoLowercase
                currentPassword == newPassword  -> throw ChangePasswordException.SameAsCurrent
                newPassword.contains(" ")       -> throw ChangePasswordException.ContainsSpace
                else -> {
                    // Intentamos el cambio en el repositorio; forzamos excepción si falla
                    authRepository.changePassword(currentPassword, newPassword, userId).getOrThrow()
                }
            }
            Result.success(Unit)
        } catch (e: ChangePasswordException) {
            // Ya es de nuestro tipo: lo devolvemos directamente
            Result.failure(e)
        } catch (e: Throwable) {
            // Cualquier otro fallo de red, parseo, etc.
            Result.failure(ChangePasswordException.RepoFailure(e))
        }
    }

}