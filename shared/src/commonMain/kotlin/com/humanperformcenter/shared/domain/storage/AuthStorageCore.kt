package com.humanperformcenter.shared.domain.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.security.AuthPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull

internal object AuthStorageCore {
    suspend fun getAccessToken(prefs: DataStore<Preferences>): String? =
        withTimeoutOrNull(TOKEN_READ_TIMEOUT_MS) {
            AuthPreferences.accessTokenFlow(prefs)
                .firstOrNull()
                .takeIf { !it.isNullOrBlank() }
        }

    suspend fun getRefreshToken(prefs: DataStore<Preferences>): String? =
        withTimeoutOrNull(TOKEN_READ_TIMEOUT_MS) {
            AuthPreferences.refreshTokenFlow(prefs)
                .firstOrNull()
                .takeIf { !it.isNullOrBlank() }
        }

    fun accessTokenFlow(prefs: DataStore<Preferences>): Flow<String> =
        AuthPreferences.accessTokenFlow(prefs)

    fun userFlow(prefs: DataStore<Preferences>): Flow<User?> =
        AuthPreferences.userFlow(prefs)

    suspend fun saveTokens(prefs: DataStore<Preferences>, access: String, refresh: String) {
        AuthPreferences.saveTokens(prefs, access, refresh)
    }

    suspend fun saveUser(prefs: DataStore<Preferences>, user: User) {
        AuthPreferences.saveUser(prefs, user)
    }

    suspend fun clear(prefs: DataStore<Preferences>) {
        AuthPreferences.clear(prefs)
    }

    private const val TOKEN_READ_TIMEOUT_MS = 1_500L
}
