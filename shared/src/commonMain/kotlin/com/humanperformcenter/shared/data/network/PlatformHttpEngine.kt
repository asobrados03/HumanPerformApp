package com.humanperformcenter.shared.data.network

import io.ktor.client.engine.HttpClientEngine

expect fun createPlatformEngine(): HttpClientEngine
