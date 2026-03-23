package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.domain.repository.UserAccountRepository

class UserAccountUseCase(
    private val userAccountRepository: UserAccountRepository,
) {
    suspend fun deleteUser(email: String): Result<Unit> {
        return userAccountRepository.deleteUser(email)
    }
}
