package com.humanperformcenter.shared.domain.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlin.test.Test
import kotlin.test.assertNull
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest

class AuthStorageCoreTest {

    @Test
    fun getAccessToken_returnsNull_whenDataStoreNeverEmits() = runTest {
        val prefs = NeverEmittingPreferencesDataStore()

        val token = AuthStorageCore.getAccessToken(prefs)

        assertNull(token)
    }

    @Test
    fun getRefreshToken_returnsNull_whenDataStoreNeverEmits() = runTest {
        val prefs = NeverEmittingPreferencesDataStore()

        val token = AuthStorageCore.getRefreshToken(prefs)

        assertNull(token)
    }

    private class NeverEmittingPreferencesDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { awaitCancellation() }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            throw UnsupportedOperationException("No se usa en este test")
        }
    }
}
