package com.humanperformcenter.app

import com.humanperformcenter.shared.data.network.HttpClientProvider

object TestOverrides {
    @Volatile
    var httpClientProviderOverride: HttpClientProvider? = null

    fun reset() {
        httpClientProviderOverride = null
    }
}
