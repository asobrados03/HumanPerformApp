package com.humanperformcenter.shared.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.flow.MutableSharedFlow

// plugin que detecta 401 y emite por logoutEvents
class LogoutPlugin private constructor(
    private val sink: MutableSharedFlow<Unit>
) {
    // Configuración para pasar el sink al plugin
    class Config {
        lateinit var sink: MutableSharedFlow<Unit>
    }

    companion object Feature : HttpClientPlugin<Config, LogoutPlugin> {
        override val key = AttributeKey<LogoutPlugin>("LogoutPlugin")

        // Prepara la instancia a partir de la configuración
        override fun prepare(block: Config.() -> Unit): LogoutPlugin {
            val config = Config().apply(block)
            val sink = requireNotNull(config.sink) { "LogoutPlugin: debes asignar sink" }
            return LogoutPlugin(sink)
        }

        // Instala el interceptor en el pipeline de recepción
        override fun install(plugin: LogoutPlugin, scope: HttpClient) {
            scope.receivePipeline.intercept(HttpReceivePipeline.After) { response ->
                if (response.status == HttpStatusCode.Unauthorized) {
                    // emitimos el evento sin lanzar excepción
                    plugin.sink.tryEmit(Unit)
                }
                proceedWith(response)
            }
        }
    }
}