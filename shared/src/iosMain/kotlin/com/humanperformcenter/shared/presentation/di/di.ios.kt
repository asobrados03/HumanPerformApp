package com.humanperformcenter.shared.presentation.di

import com.humanperformcenter.shared.SessionNotificationManager
import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.domain.storage.DataStoreProvider
import com.humanperformcenter.shared.domain.storage.SecureStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DataStoreProvider.get() }
    single<AuthLocalDataSource> {
        SecureStorage.also { it.initialize(get()) }  // get() resuelve el DataStore de arriba
    }
    single<SessionNotificationManager> { IOSSessionNotificationManager() }
}

class IOSSessionNotificationManager : SessionNotificationManager {
    override fun cancelNotification(bookingId: Int) {
    }
}
