package com.humanperformcenter.shared.domain.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.humanperformcenter.shared.data.model.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthPreferencesTest {

    private val originalCryptoAdapter = AuthPreferences.cryptoAdapter

    @AfterTest
    fun tearDown() {
        AuthPreferences.cryptoAdapter = originalCryptoAdapter
    }

    @Test
    fun saveTokens_and_accessFlow_roundtrip_with_controlled_crypto() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        AuthPreferences.cryptoAdapter = ReversibleCryptoAdapter

        AuthPreferences.saveTokens(dataStore, access = "access-token", refresh = "refresh-token")

        assertEquals("access-token", AuthPreferences.accessTokenFlow(dataStore).first())
        assertEquals("refresh-token", AuthPreferences.refreshTokenFlow(dataStore).first())
    }

    @Test
    fun saveTokensAndUser_persists_tokens_and_user_in_same_snapshot() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        AuthPreferences.cryptoAdapter = ReversibleCryptoAdapter
        val user = sampleUser()

        AuthPreferences.saveTokensAndUser(
            prefs = dataStore,
            access = "access-token",
            refresh = "refresh-token",
            user = user,
        )

        assertEquals("access-token", AuthPreferences.accessTokenFlow(dataStore).first())
        assertEquals("refresh-token", AuthPreferences.refreshTokenFlow(dataStore).first())
        assertEquals(user, AuthPreferences.userFlow(dataStore).first())
    }

    @Test
    fun userFlow_when_decrypt_fails_returns_null_and_does_not_crash() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        AuthPreferences.cryptoAdapter = ReversibleCryptoAdapter
        AuthPreferences.saveUser(dataStore, sampleUser())

        AuthPreferences.cryptoAdapter = object : AuthPreferences.CryptoAdapter {
            override fun encrypt(plain: ByteArray): ByteArray = plain
            override fun decrypt(cipherMessage: ByteArray): ByteArray {
                throw CryptoException.DecryptionFailed
            }
        }

        assertNull(AuthPreferences.userFlow(dataStore).first())
    }

    @Test
    fun refreshTokenFlow_when_crypto_fails_clears_preferences_and_emits_empty() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        AuthPreferences.cryptoAdapter = ReversibleCryptoAdapter
        AuthPreferences.saveTokens(dataStore, access = "access-token", refresh = "refresh-token")

        AuthPreferences.cryptoAdapter = object : AuthPreferences.CryptoAdapter {
            override fun encrypt(plain: ByteArray): ByteArray = plain
            override fun decrypt(cipherMessage: ByteArray): ByteArray {
                throw CryptoException.DecryptionFailed
            }
        }

        assertEquals("", AuthPreferences.refreshTokenFlow(dataStore).first())
        assertEquals(emptyPreferences(), dataStore.data.first())
    }

    private object ReversibleCryptoAdapter : AuthPreferences.CryptoAdapter {
        override fun encrypt(plain: ByteArray): ByteArray = plain.reversedArray()
        override fun decrypt(cipherMessage: ByteArray): ByteArray = cipherMessage.reversedArray()
    }

    private class InMemoryPreferencesDataStore : DataStore<Preferences> {
        private val state = MutableStateFlow<Preferences>(emptyPreferences())

        override val data: Flow<Preferences> = state

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }
    }

    private fun sampleUser() = User(
        id = 9,
        fullName = "Ana",
        email = "ana@test.com",
        phone = "600000000",
        sex = "F",
        dateOfBirth = "1990-01-01",
        postcode = 28001,
        postAddress = "Calle Mayor 1",
        dni = "12345678A",
        profilePictureName = null,
    )
}
