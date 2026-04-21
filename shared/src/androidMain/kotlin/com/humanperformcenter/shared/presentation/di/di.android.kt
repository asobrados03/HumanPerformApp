package com.humanperformcenter.shared.presentation.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.AndroidSessionNotificationManager
import com.humanperformcenter.shared.SessionNotificationManager
import com.humanperformcenter.shared.domain.storage.DATA_STORE_FILE_NAME
import com.humanperformcenter.shared.domain.storage.createDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DataStore<Preferences>> {
        createDataStore {
            androidContext().filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
        }
    }
    single<SessionNotificationManager> { AndroidSessionNotificationManager(get()) }
}
