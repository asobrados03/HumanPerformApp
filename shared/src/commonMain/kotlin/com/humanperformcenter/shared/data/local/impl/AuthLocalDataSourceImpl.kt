package com.humanperformcenter.shared.data.local.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.storage.AuthStorageCore
import kotlinx.coroutines.flow.Flow

class AuthLocalDataSourceImpl(
    private val prefs: DataStore<Preferences>,
) : AuthLocalDataSource {
    override suspend fun getAccessToken(): String? = AuthStorageCore.getAccessToken(prefs)

    override suspend fun getRefreshToken(): String? = AuthStorageCore.getRefreshToken(prefs)

    override fun accessTokenFlow(): Flow<String> = AuthStorageCore.accessTokenFlow(prefs)

    override fun userFlow(): Flow<User?> = AuthStorageCore.userFlow(prefs)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        AuthStorageCore.saveTokens(prefs, accessToken, refreshToken)
    }

    override suspend fun clearTokens() {
        AuthStorageCore.saveTokens(prefs, "", "")
    }

    override suspend fun saveUser(user: User) {
        AuthStorageCore.saveUser(prefs, user)
    }

    override suspend fun clearUser() {
        AuthStorageCore.clear(prefs)
    }

    override suspend fun clearSession() {
        AuthStorageCore.clear(prefs)
    }
}
