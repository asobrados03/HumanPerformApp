package com.humanperformcenter.shared.domain.security

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.humanperformcenter.shared.data.model.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

object AuthPreferences {
    internal interface CryptoAdapter {
        fun encrypt(plain: ByteArray): ByteArray
        fun decrypt(cipherMessage: ByteArray): ByteArray
    }

    internal var cryptoAdapter: CryptoAdapter = object : CryptoAdapter {
        override fun encrypt(plain: ByteArray): ByteArray = Crypto.encrypt(plain)
        override fun decrypt(cipherMessage: ByteArray): ByteArray = Crypto.decrypt(cipherMessage)
    }

    private val KEY_ACCESS  = stringPreferencesKey("access_token_enc")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token_enc")
    private val KEY_USER_JSON = stringPreferencesKey("key_user_json")

    suspend fun saveTokens(
        prefs: DataStore<Preferences>,
        access: String,
        refresh: String
    ) {
        prefs.edit { m ->
            val ab = access.encodeToByteArray()
            val rb = refresh.encodeToByteArray()

            val ea = cryptoAdapter.encrypt(ab)
            val er = cryptoAdapter.encrypt(rb)

            m[KEY_ACCESS] = Base64.encode(ea)
            m[KEY_REFRESH] = Base64.encode(er)
        }
    }

    private fun safePrefsFlow(
        prefs: DataStore<Preferences>
    ): Flow<Preferences> =
        prefs.data
            .catch { e ->
                println("DEBUG: Error leyendo DataStore: ${e.message}")
                if (e is IOException) {
                    emit(emptyPreferences())
                } else {
                    try {
                        prefs.edit { it.clear() }
                    } catch (_: Exception) { }
                    emit(emptyPreferences())
                }
            }


    fun accessTokenFlow(prefs: DataStore<Preferences>): Flow<String> =
        safePrefsFlow(prefs)
            .map { m ->
                try {
                    m[KEY_ACCESS]?.let { b64 ->
                        val cipherBytes = Base64.decode(b64)
                        val decryptedBytes = cryptoAdapter.decrypt(cipherBytes)
                        decryptedBytes.decodeToString()
                    } ?: ""
                } catch (_: Exception) {
                    ""
                }
            }

    fun refreshTokenFlow(prefs: DataStore<Preferences>): Flow<String> =
        safePrefsFlow(prefs)
            .map { m ->
                m[KEY_REFRESH]?.let { b64 ->
                    Base64.decode(b64).let { cipherBytes ->
                        cryptoAdapter.decrypt(cipherBytes)
                    }.decodeToString()
                }.orEmpty()
            }
            .catch { e ->
                if (e is CryptoException.DecryptionFailed) {
                    prefs.edit { it.clear() }
                    emit("")
                } else {
                    throw e
                }
            }


    suspend fun saveUser(prefs: DataStore<Preferences>, user: User) {
        prefs.edit { m ->
            val json = Json.encodeToString(user)

            val bytes = json.encodeToByteArray()

            val encrypted = cryptoAdapter.encrypt(bytes)

            m[KEY_USER_JSON] = Base64.encode(encrypted)
        }
    }

    fun userFlow(prefs: DataStore<Preferences>): Flow<User?> =
        safePrefsFlow(prefs)
            .map { m ->
                try {
                    m[KEY_USER_JSON]?.let { b64 ->
                        val cipherBytes = Base64.decode(b64)
                        val jsonBytes = cryptoAdapter.decrypt(cipherBytes)
                        val json = jsonBytes.decodeToString()
                        Json.decodeFromString<User>(json)
                    }
                } catch (e: Exception) {
                    println("DEBUG: Error descifrando usuario: ${e.message}")
                    null
                }
            }


    suspend fun clear(prefs: DataStore<Preferences>) {
        prefs.edit { preferences ->
            preferences.clear()
        }
    }
}
