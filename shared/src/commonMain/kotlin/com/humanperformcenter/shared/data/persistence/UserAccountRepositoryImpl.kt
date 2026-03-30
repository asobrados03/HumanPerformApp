package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.remote.UserAccountRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserAccountRepository

class UserAccountRepositoryImpl(
    private val remoteDataSource: UserAccountRemoteDataSource,
) : UserAccountRepository {
    override suspend fun deleteUser(email: String): Result<Unit> = remoteDataSource.deleteUser(email)
}
