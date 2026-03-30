package com.humanperformcenter.shared.data.local.impl

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.local.UserProfileLocalDataSource
import com.humanperformcenter.shared.data.model.user.User
import kotlinx.coroutines.flow.firstOrNull

class UserProfileLocalDataSourceImpl(
    private val authLocalDataSource: AuthLocalDataSource,
) : UserProfileLocalDataSource {
    override suspend fun saveUser(user: User) {
        authLocalDataSource.saveUser(user)
    }

    override suspend fun getUser(): User? = authLocalDataSource.userFlow().firstOrNull()

    override suspend fun clearUser() {
        authLocalDataSource.clear()
    }
}
