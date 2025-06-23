package com.humanperformcenter.shared.domain.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.data.model.User
import kotlinx.coroutines.flow.Flow

expect object SecureStorage {
    fun init(prefs: DataStore<Preferences>)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    suspend fun saveTokens(access: String, refresh: String)
    suspend fun saveUser(user: User)
    fun userFlow(): Flow<User?>
    suspend fun clear()
}