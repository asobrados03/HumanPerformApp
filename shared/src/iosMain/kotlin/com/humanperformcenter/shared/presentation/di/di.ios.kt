package com.humanperformcenter.shared.presentation.di

import com.humanperformcenter.shared.SessionNotificationManager
import org.koin.core.module.Module
import org.koin.dsl.module

// En iosMain/kotlin/...
actual val platformModule: Module = module {
    // Si aún no tienes la implementación de iOS
    single<SessionNotificationManager> { IOSSessionNotificationManager() }
}

class IOSSessionNotificationManager : SessionNotificationManager {
    override fun cancelNotification(bookingId: Int) {

    }

}
