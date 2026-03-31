package com.humanperformcenter.app

import android.app.Application
import com.humanperformcenter.shared.domain.storage.DataStoreProvider
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.presentation.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class HumanPerformApp : Application() {
    override fun onCreate() {
        super.onCreate()

        DataStoreProvider.initialize(this)
        SecureStorage.initialize(DataStoreProvider.get())

        initKoin {
            androidLogger()
            androidContext(this@HumanPerformApp)
        }

        TestOverrides.httpClientProviderOverride?.let { provider ->
            loadKoinModules(
                module {
                    single<HttpClientProvider>(override = true) { provider }
                }
            )
        }
    }
}
