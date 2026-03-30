package com.humanperformcenter.shared.domain.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.data.model.user.User
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.Flow

object SecureStorage {
    private lateinit var prefs: DataStore<Preferences>

    /** Inicializa el storage. Llama esto desde Android con tu prefs */
    fun initialize(prefs: DataStore<Preferences>) {
        this.prefs = prefs
    }

    /** Devuelve el access token actual o null si no hay */
    fun getAccessToken(): String? = runBlocking { AuthStorageCore.getAccessToken(prefs) }

    /** Devuelve el refresh token actual o null si no hay */
    fun getRefreshToken(): String? = runBlocking { AuthStorageCore.getRefreshToken(prefs) }

    /** Guarda ambos tokens (suspende) */
    suspend fun saveTokens(access: String, refresh: String) {
        AuthStorageCore.saveTokens(prefs, access, refresh)
    }

    @NativeCoroutines
    fun accessTokenFlow(): Flow<String> = AuthStorageCore.accessTokenFlow(prefs)

    suspend fun saveUser(user: User) {
        AuthStorageCore.saveUser(prefs, user)
    }

    @NativeCoroutines
    fun userFlow(): Flow<User?> = AuthStorageCore.userFlow(prefs)

    /** Borra tokens (logout) */
    suspend fun clear() {
        AuthStorageCore.clear(prefs)
    }
}
