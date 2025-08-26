package com.humanperformcenter.shared.domain.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.domain.security.AuthPreferences
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

object SecureStorage {
    private lateinit var prefs: DataStore<Preferences>

    /** Inicializa el storage. Llama esto desde Android con tu prefs */
    fun initialize(prefs: DataStore<Preferences>) {
        this.prefs = prefs
    }

    /** Devuelve el access token actual o null si no hay */
    fun getAccessToken(): String? = runBlocking {
        AuthPreferences.accessTokenFlow(prefs)
            .firstOrNull()
            .takeIf { !it.isNullOrBlank() }
    }

    /** Devuelve el refresh token actual o null si no hay */
    fun getRefreshToken(): String? = runBlocking {
        AuthPreferences.refreshTokenFlow(prefs)
            .firstOrNull()
            .takeIf { !it.isNullOrBlank() }
    }

    /** Guarda ambos tokens (suspende) */
    suspend fun saveTokens(access: String, refresh: String) {
        AuthPreferences.saveTokens(prefs, access, refresh)
    }

    @NativeCoroutines
    fun accessTokenFlow(): Flow<String> {
        return AuthPreferences.accessTokenFlow(prefs)
    }

    suspend fun saveUser(user: User) {
        AuthPreferences.saveUser(prefs, user)
    }

    fun userFlow(): Flow<User?> = AuthPreferences.userFlow(prefs)

    /** Borra tokens (logout) */
    suspend fun clear() {
        AuthPreferences.clear(prefs)
    }
}