package com.humanperformcenter.shared.data.local.impl

import com.humanperformcenter.shared.data.local.UserProfileLocalDataSource
import com.humanperformcenter.shared.data.model.user.User
import kotlinx.coroutines.flow.firstOrNull

object UserProfileLocalDataSourceImpl : UserProfileLocalDataSource {
    override suspend fun saveUser(user: User) {
        AuthLocalDataSourceImpl.saveUser(user)
    }

    override suspend fun getUser(): User? = AuthLocalDataSourceImpl.userFlow().firstOrNull()

    override suspend fun clearUser() {
        AuthLocalDataSourceImpl.clearUser()
    }
}
