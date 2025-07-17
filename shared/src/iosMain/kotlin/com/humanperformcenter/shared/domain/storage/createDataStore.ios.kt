package com.humanperformcenter.shared.domain.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import kotlin.native.concurrent.ThreadLocal

/**
 * En Kotlin/Native no tenemos synchronized(), así que marcamos
 * el objeto como @ThreadLocal para que INSTANCE viva siempre
 * en el mismo hilo (el main de iOS).
 */
@ThreadLocal
object DataStoreProvider {
    private var INSTANCE: DataStore<Preferences>? = null

    /**
     * Devuelve la única instancia de DataStore<Preferences>
     * para iOS, usando tu createDataStore() existente.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun get(): DataStore<Preferences> {
        return INSTANCE
            ?: createDataStore().also { INSTANCE = it }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun createDataStore(): DataStore<Preferences> {
        return createDataStore {
            val directory = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain      = NSUserDomainMask,
                appropriateForURL = null,
                create        = false,
                error         = null
            )
            requireNotNull(directory).path + "/$DATA_STORE_FILE_NAME"
        }
    }

}
