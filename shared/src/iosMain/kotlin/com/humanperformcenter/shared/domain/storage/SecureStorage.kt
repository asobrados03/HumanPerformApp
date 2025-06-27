package com.humanperformcenter.shared.domain.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.domain.security.AuthPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

actual object SecureStorage {
    private lateinit var prefs: DataStore<Preferences>

    actual fun init(prefs: DataStore<Preferences>) {
        this.prefs = prefs
    }

    actual fun getAccessToken(): String? = runBlocking {
        AuthPreferences.accessTokenFlow(prefs)
            .firstOrNull()
            .takeIf { !it.isNullOrBlank() }
    }

    actual fun getRefreshToken(): String? = runBlocking {
        AuthPreferences.refreshTokenFlow(prefs)
            .firstOrNull()
            .takeIf { !it.isNullOrBlank() }
    }

    actual suspend fun saveTokens(access: String, refresh: String) {
        AuthPreferences.saveTokens(prefs, access, refresh)
    }

    actual suspend fun saveUser(user: User) {
        AuthPreferences.saveUser(prefs, user)
    }

    actual fun userFlow(): Flow<User?> = AuthPreferences.userFlow(prefs)

    actual suspend fun clear() {
        AuthPreferences.clear(prefs)
    }
}