package com.humanperformcenter.shared.data.local

import com.humanperformcenter.shared.data.model.user.User
import kotlinx.coroutines.flow.Flow

interface AuthLocalDataSource {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    fun accessTokenFlow(): Flow<String>
    fun userFlow(): Flow<User?>
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clearTokens()
    suspend fun saveUser(user: User)
    suspend fun clear()
}
