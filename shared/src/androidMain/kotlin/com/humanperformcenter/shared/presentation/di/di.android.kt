package com.humanperformcenter.shared.presentation.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.humanperformcenter.shared.AndroidSessionNotificationManager
import com.humanperformcenter.shared.SessionNotificationManager
import com.humanperformcenter.shared.data.network.dataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DataStore<Preferences>> {
        androidContext().dataStore
    }
    single<SessionNotificationManager> { AndroidSessionNotificationManager(get()) }
}
