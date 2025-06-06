package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.domain.repository.UserRepository

class UserUseCase(private val userRepository: UserRepository) {
    /**
     * Si la validación pasó, envía la actualización al repositorio.
     * La llamada al repositorio puede devolver Result.success(nuevoUser) o Result.failure(t).
     */
    suspend fun updateUser(user: LoginResponse): Result<LoginResponse> {
        return userRepository.updateUser(user)
    }
}