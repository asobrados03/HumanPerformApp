package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginResponse>
    suspend fun register(data: RegisterRequest): Result<RegisterResponse>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int): Result<Unit>
    suspend fun logout(): Result<Unit>
}
