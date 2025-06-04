package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.domain.repository.UserRepository

class UserUseCase(private val userRepository: UserRepository) {
    suspend fun updateUser(user: LoginResponse): Result<LoginResponse> {
        return userRepository.updateUser(user)
    }
}