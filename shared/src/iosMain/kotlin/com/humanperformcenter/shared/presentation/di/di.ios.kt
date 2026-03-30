package com.humanperformcenter.shared.presentation.di

import com.humanperformcenter.shared.SessionNotificationManager
import com.humanperformcenter.shared.domain.storage.DataStoreProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DataStoreProvider.get() }
    single<SessionNotificationManager> { IOSSessionNotificationManager() }
}

class IOSSessionNotificationManager : SessionNotificationManager {
    override fun cancelNotification(bookingId: Int) {
    }
}
