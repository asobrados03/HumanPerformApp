package com.humanperformcenter.shared.domain.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.humanperformcenter.shared.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

object AuthPreferences {
    // 1) Defino mis claves
    private val KEY_ACCESS  = stringPreferencesKey("access_token_enc")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token_enc")
    private val KEY_USER_JSON = stringPreferencesKey("key_user_json")

    /** Guarda ambos tokens cifrados y codificados en Base64 */
    suspend fun saveTokens(
        prefs: DataStore<Preferences>,
        access: String,
        refresh: String
    ) {
        prefs.edit { m ->
            // a) Texto -> bytes UTF-8
            val ab = access.encodeToByteArray()
            val rb = refresh.encodeToByteArray()
            // b) Cifrar
            val ea = Crypto.encrypt(ab)
            val er = Crypto.encrypt(rb)
            // c) Codificar a Base64 con tu wrapper
            m[KEY_ACCESS] = Base64.encode(ea)
            m[KEY_REFRESH] = Base64.encode(er)
        }
    }

    // 2) Helper: un Flow de Preferences “seguro” que limpia el DataStore si detecta error de descifrado
    private fun safePrefsFlow(
        prefs: DataStore<Preferences>
    ): Flow<Preferences> =
        prefs.data
            .catch { e ->
                if (e is CryptoException.DecryptionFailed) {
                    // 2.1) Limpiamos TODO el DataStore
                    prefs.edit { it.clear() }
                    // 2.2) Emitimos un emptyPreferences() para que siga el flujo con valores por defecto
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }


    /** Flujo que emite el access token desencriptado */
    fun accessTokenFlow(prefs: DataStore<Preferences>): Flow<String> =
        safePrefsFlow(prefs)
            .map { m ->
                m[KEY_ACCESS]?.let { b64 ->
                    Base64.decode(b64).let { cipherBytes ->
                        Crypto.decrypt(cipherBytes)
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

    fun refreshTokenFlow(prefs: DataStore<Preferences>): Flow<String> =
        safePrefsFlow(prefs)
            .map { m ->
                m[KEY_REFRESH]?.let { b64 ->
                    Base64.decode(b64).let { cipherBytes ->
                        Crypto.decrypt(cipherBytes)
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
            // a) Object -> JSON
            val json = Json.encodeToString(user)
            // b) JSON -> bytes UTF-8
            val bytes = json.encodeToByteArray()
            // c) bytes -> cifrado
            val encrypted = Crypto.encrypt(bytes)
            // d) cifrado -> Base64 String
            m[KEY_USER_JSON] = Base64.encode(encrypted)
        }
    }

    fun userFlow(prefs: DataStore<Preferences>): Flow<User?> =
        safePrefsFlow(prefs)
            .map { m ->
                m[KEY_USER_JSON]?.let { b64 ->
                    val cipherBytes = Base64.decode(b64)
                    val jsonBytes   = Crypto.decrypt(cipherBytes)
                    val json        = jsonBytes.decodeToString()
                    Json.decodeFromString<User>(json)
                }
            }
            .catch { e ->
                if (e is CryptoException.DecryptionFailed) {
                    prefs.edit { it.clear() }
                    emit(null)
                } else {
                    throw e
                }
            }

    /**
     * Clears ALL user-related data from DataStore
     */
    suspend fun clear(prefs: DataStore<Preferences>) {
        prefs.edit { preferences ->
            preferences.clear()
        }
    }
}
