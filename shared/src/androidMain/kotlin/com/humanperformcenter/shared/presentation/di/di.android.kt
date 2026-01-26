package com.humanperformcenter.shared.presentation.di

import com.humanperformcenter.shared.AndroidSessionNotificationManager
import com.humanperformcenter.shared.SessionNotificationManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SessionNotificationManager> { AndroidSessionNotificationManager(get()) }
}