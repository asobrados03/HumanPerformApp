package com.humanperformcenter.shared.domain.storage

import androidx.datastore.core.DataStore
import com.humanperformcenter.shared.domain.security.AuthPreferences
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

actual object SecureStorage {
    private lateinit var prefs: DataStore<Preferences>

    /** Inicializa el storage. Llama esto desde Android con tu prefs */
    actual fun init(prefs: DataStore<Preferences>) {
        this.prefs = prefs
    }

    /** Devuelve el access token actual o null si no hay */
    actual fun getAccessToken(): String? = runBlocking {
        AuthPreferences.accessTokenFlow(prefs)
            .firstOrNull()
            .takeIf { !it.isNullOrBlank() }
    }

    /** Devuelve el refresh token actual o null si no hay */
    actual fun getRefreshToken(): String? = runBlocking {
        AuthPreferences.refreshTokenFlow(prefs)
            .firstOrNull()
            .takeIf { !it.isNullOrBlank() }
    }

    /** Guarda ambos tokens (suspende) */
    actual suspend fun saveTokens(access: String, refresh: String) {
        AuthPreferences.saveTokens(prefs, access, refresh)
    }

    actual suspend fun saveUser(user: User) {
        AuthPreferences.saveUser(prefs, user)
    }

    actual fun userFlow(): Flow<User?> = AuthPreferences.userFlow(prefs)

    /** Borra tokens (logout) */
    actual suspend fun clear() {
        AuthPreferences.clear(prefs)
    }
}
