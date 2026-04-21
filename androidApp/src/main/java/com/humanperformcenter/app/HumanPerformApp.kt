package com.humanperformcenter.app

import android.app.Application
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.presentation.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class HumanPerformApp : Application() {
    override fun onCreate() {
        super.onCreate()


        initKoin {
            androidLogger()
            androidContext(this@HumanPerformApp)
            allowOverride(true)
        }

        TestOverrides.httpClientProviderOverride?.let { provider ->
            loadKoinModules(
                module {
                    single<HttpClientProvider> { provider }
                }
            )
        }
    }
}
