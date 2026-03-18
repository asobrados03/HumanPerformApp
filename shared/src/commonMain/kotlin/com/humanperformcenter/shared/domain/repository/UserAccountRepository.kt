package com.humanperformcenter.shared.domain.repository

interface UserAccountRepository {
    suspend fun deleteUser(email: String): Result<Unit>
}
