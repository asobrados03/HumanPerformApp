package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.data.model.UserResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginResponse> // Esto tiene que ser UserResponse
    suspend fun registrar(data: RegisterRequest): Result<UserResponse>
}