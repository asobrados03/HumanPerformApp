package com.humaneperformcenter.shared.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {
    // Inicializamos HttpClient con ContentNegotiation para JSON
    val httpClient = HttpClient() {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true }) // config: ignorar campos desconocidos
        }
        expectSuccess = true  // lanza excepciones en respuestas no 2XX:contentReference[oaicite:11]{index=11}:contentReference[oaicite:12]{index=12}
    }
    val baseUrl = "http://163.172.67.59:5001"  // URL base de la API
}