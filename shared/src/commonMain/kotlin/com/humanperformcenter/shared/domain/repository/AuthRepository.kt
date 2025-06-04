package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.data.model.RegisterResponse
import com.humanperformcenter.shared.data.model.LoginResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginResponse> // Esto tiene que ser UserResponse
    suspend fun register(data: RegisterRequest): Result<RegisterResponse>
}