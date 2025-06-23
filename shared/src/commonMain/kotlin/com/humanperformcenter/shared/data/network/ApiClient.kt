package com.humanperformcenter.shared.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {
    // Inicializamos HttpClient con ContentNegotiation para JSON
    val httpClient = HttpClient() {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true }) // config: ignorar campos desconocidos
        }
        expectSuccess = true  // lanza excepciones en respuestas no 2XX:contentReference[oaicite:11]{index=11}:contentReference[oaicite:12]{index=12}
    }
    val baseUrl = "http://163.172.67.59:5020"  // URL base de la API
    val apibaseUrl = "http://163.172.71.195:8085"
}