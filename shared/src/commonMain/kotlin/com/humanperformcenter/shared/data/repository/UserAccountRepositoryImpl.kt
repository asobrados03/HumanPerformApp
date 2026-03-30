package com.humanperformcenter.shared.data.repository

import com.humanperformcenter.shared.data.remote.UserAccountRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserAccountRepository

class UserAccountRepositoryImpl(
    private val remote: UserAccountRemoteDataSource,
) : UserAccountRepository {
    override suspend fun deleteUser(email: String): Result<Unit> = remote.deleteUser(email)
}
