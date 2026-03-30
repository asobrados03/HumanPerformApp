package com.humanperformcenter.shared.data.local.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.security.AuthPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AuthLocalDataSourceImpl(
    private val prefs: DataStore<Preferences>,
) : AuthLocalDataSource {
    override suspend fun getAccessToken(): String? =
        AuthPreferences.accessTokenFlow(prefs)
            .firstOrNull()
            .takeIf { !it.isNullOrBlank() }

    override suspend fun getRefreshToken(): String? =
        AuthPreferences.refreshTokenFlow(prefs)
            .firstOrNull()
            .takeIf { !it.isNullOrBlank() }

    override fun accessTokenFlow(): Flow<String> = AuthPreferences.accessTokenFlow(prefs)

    override fun userFlow(): Flow<User?> = AuthPreferences.userFlow(prefs)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        AuthPreferences.saveTokens(prefs, accessToken, refreshToken)
    }

    override suspend fun clearTokens() {
        AuthPreferences.saveTokens(prefs, "", "")
    }

    override suspend fun saveUser(user: User) {
        AuthPreferences.saveUser(prefs, user)
    }

    override suspend fun clearUser() {
        AuthPreferences.clear(prefs)
    }

    override suspend fun clearSession() {
        AuthPreferences.clear(prefs)
    }
}
