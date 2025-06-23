package com.humanperformcenter.shared.domain.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.humanperformcenter.shared.data.model.User
import kotlinx.coroutines.flow.Flow
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

    /** Flujo que emite el access token desencriptado */
    fun accessTokenFlow(prefs: DataStore<Preferences>): Flow<String> =
        prefs.data
            .map { m: Preferences ->
                m[KEY_ACCESS]?.let { b64 ->
                    // 1) Base64 String -> ByteArray criptográfico
                    val cipherBytes = Base64.decode(b64)
                    // 2) Desencriptar
                    val plainBytes  = Crypto.decrypt(cipherBytes)
                    // 3) ByteArray -> String
                    plainBytes.decodeToString()
                }.orEmpty()
            }

    /** Flujo que emite el refresh token desencriptado */
    fun refreshTokenFlow(prefs: DataStore<Preferences>): Flow<String> =
        prefs.data
            .map { m: Preferences ->
                m[KEY_REFRESH]?.let { b64 ->
                    val cipherBytes = Base64.decode(b64)
                    val plainBytes  = Crypto.decrypt(cipherBytes)
                    plainBytes.decodeToString()
                }.orEmpty()
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
        prefs.data.map { m ->
            m[KEY_USER_JSON]?.let { b64 ->
                // a) Base64 -> bytes cifrados
                val cipherBytes = Base64.decode(b64)
                // b) cifrados -> JSON bytes
                val jsonBytes   = Crypto.decrypt(cipherBytes)
                // c) JSON bytes -> String
                val json        = jsonBytes.decodeToString()
                // d) String -> User
                Json.decodeFromString<User>(json)
            }
        }

    /** Borra ambos tokens e info user (logout) */
    suspend fun clear(prefs: DataStore<Preferences>) {
        prefs.edit { it.clear() }
    }
}
