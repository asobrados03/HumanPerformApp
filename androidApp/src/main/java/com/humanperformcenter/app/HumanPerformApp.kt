package com.humanperformcenter.app

import android.app.Application
import com.humanperformcenter.shared.domain.storage.DataStoreProvider
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.presentation.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class HumanPerformApp : Application() {
    override fun onCreate() {
        super.onCreate()

        DataStoreProvider.initialize(this)
        SecureStorage.initialize(DataStoreProvider.get())

        initKoin {
            androidLogger()
            androidContext(this@HumanPerformApp)
        }
    }
}
