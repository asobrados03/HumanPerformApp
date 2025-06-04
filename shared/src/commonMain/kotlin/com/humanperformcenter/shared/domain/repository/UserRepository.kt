package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.LoginResponse

interface UserRepository {
    suspend fun updateUser(user: LoginResponse): Result<LoginResponse>
}