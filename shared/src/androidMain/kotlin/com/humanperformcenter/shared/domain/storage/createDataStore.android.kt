package com.humanperformcenter.shared.domain.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

object DataStoreProvider {
    @Volatile private var INSTANCE: DataStore<Preferences>? = null

    fun initialize(context: Context) {
        if (INSTANCE == null) {
            INSTANCE = createDataStore(context)
        }
    }

    fun get(): DataStore<Preferences> =
        requireNotNull(INSTANCE) { "DataStoreProvider must be initialized before use." }

    private fun createDataStore(context: Context): DataStore<Preferences> {
        return createDataStore {
            context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
        }
    }
}
