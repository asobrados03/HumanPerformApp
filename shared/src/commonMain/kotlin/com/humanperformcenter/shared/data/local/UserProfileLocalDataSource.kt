package com.humanperformcenter.shared.data.local

import com.humanperformcenter.shared.data.model.user.User

interface UserProfileLocalDataSource {
    suspend fun saveUser(user: User)
    suspend fun getUser(): User?
    suspend fun clearUser()
}
