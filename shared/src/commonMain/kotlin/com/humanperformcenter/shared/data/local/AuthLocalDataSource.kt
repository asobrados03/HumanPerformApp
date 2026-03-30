package com.humanperformcenter.shared.data.local

import com.humanperformcenter.shared.data.model.user.User

interface AuthLocalDataSource {
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clearTokens()
    suspend fun saveUser(user: User)
    suspend fun clearUser()
}
