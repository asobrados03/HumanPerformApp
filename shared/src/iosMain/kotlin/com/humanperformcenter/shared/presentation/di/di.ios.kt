package com.humanperformcenter.shared.presentation.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.SessionNotificationManager
import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.domain.storage.DATA_STORE_FILE_NAME
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.domain.storage.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single<DataStore<Preferences>> {
        createDataStore {
            val directory = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null
            )
            requireNotNull(directory).path + "/$DATA_STORE_FILE_NAME"
        }
    }
    single<AuthLocalDataSource> {
        SecureStorage.also { it.initialize(get()) }
    }
    single<SessionNotificationManager> { IOSSessionNotificationManager() }
}

class IOSSessionNotificationManager : SessionNotificationManager {
    override fun cancelNotification(bookingId: Int) {
    }
}
