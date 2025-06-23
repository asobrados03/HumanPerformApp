package com.humanperformcenter.shared.domain.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.domain.security.AuthPreferences
import kotlinx.coroutines.flow.Flow

actual object SecureStorage {
    private lateinit var prefs: DataStore<Preferences>

    actual fun init(prefs: DataStore<Preferences>) { }
    actual fun getAccessToken(): String? = ""
    actual fun getRefreshToken(): String? = ""
    actual suspend fun saveTokens(access: String, refresh: String) { }
    actual suspend fun saveUser(user: User) {}
    actual fun userFlow(): Flow<User?> = AuthPreferences.userFlow(prefs)
    actual suspend fun clear(){ }
}