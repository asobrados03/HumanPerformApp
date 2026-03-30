package com.humanperformcenter.shared.domain.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.user.User
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow

object SecureStorage : AuthLocalDataSource {
    private lateinit var prefs: DataStore<Preferences>

    /** Inicializa el storage. Llama esto desde Android con tu prefs */
    fun initialize(prefs: DataStore<Preferences>) {
        this.prefs = prefs
    }

    override suspend fun getAccessToken(): String? = AuthStorageCore.getAccessToken(prefs)

    override suspend fun getRefreshToken(): String? = AuthStorageCore.getRefreshToken(prefs)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        AuthStorageCore.saveTokens(prefs, accessToken, refreshToken)
    }

    @NativeCoroutines
    override fun accessTokenFlow(): Flow<String> = AuthStorageCore.accessTokenFlow(prefs)

    override suspend fun saveUser(user: User) {
        AuthStorageCore.saveUser(prefs, user)
    }

    @NativeCoroutines
    override fun userFlow(): Flow<User?> = AuthStorageCore.userFlow(prefs)

    override suspend fun clearTokens() {
        AuthStorageCore.saveTokens(prefs, "", "")
    }

    /** Borra tokens (logout) */
    override suspend fun clear() {
        AuthStorageCore.clear(prefs)
    }
}
