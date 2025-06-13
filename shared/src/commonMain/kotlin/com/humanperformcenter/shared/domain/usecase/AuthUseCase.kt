package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.data.model.RegisterResponse
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.usecase.validation.ChangePasswordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AuthUseCase(private val authRepository: AuthRepository) {
    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO)  {
        return@withContext authRepository.login(email, password)
    }

    suspend fun register(data: RegisterRequest): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        return@withContext authRepository.register(data)
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        userId: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
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
                    authRepository.changePassword(currentPassword, newPassword, userId)
                        .getOrThrow()
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