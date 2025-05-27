package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.data.model.UserResponse
import com.humanperformcenter.shared.domain.repository.AuthRepository

class AuthUseCase(private val authRepository: AuthRepository) {
    suspend fun login(email: String, password: String): Result<LoginResponse>  {
        return authRepository.login(email, password)
    }

    suspend fun register(data: RegisterRequest): Result<UserResponse> {
        return authRepository.register(data)
    }
}