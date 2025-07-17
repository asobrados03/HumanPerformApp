package com.humanperformcenter.shared.domain.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

object DataStoreProvider {
    // Volatile para visibilidad entre hilos
    @Volatile private var INSTANCE: DataStore<Preferences>? = null

    /**
     * Devuelve la única instancia de DataStore<Preferences> apuntando a
     * DATA_STORE_FILE_NAME.
     */
    fun get(context: Context): DataStore<Preferences> {
        return INSTANCE
            ?: synchronized(this) {
                INSTANCE
                    ?: createDataStore(context).also { INSTANCE = it }
            }
    }

    // Tu función original, puede quedarse igual
    private fun createDataStore(context: Context): DataStore<Preferences> {
        return createDataStore {
            context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
        }
    }
}