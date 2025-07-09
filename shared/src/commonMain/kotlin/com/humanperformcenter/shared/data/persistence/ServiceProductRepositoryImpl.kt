package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ProductDetailResponse
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.ServiceItem
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

object ServiceProductRepositoryImpl: ServiceProductRepository {
    override suspend fun getAllServices(): List<ServiceAvailable> {
        return try {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/services")
            val text = response.bodyAsText()

            if (text.contains("error", ignoreCase = true)) {
                println("❌ Error del backend: $text")
                emptyList()
            } else {
                Json.decodeFromString(text)
            }
        } catch (e: Exception) {
            println("❌ Excepción al obtener servicios: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getServiceProducts(serviceId: Int): List<ServiceItem> {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/service-products") {
            parameter("service_id", serviceId)
        }
        return response.body()
    }

    override suspend fun getUserProducts(customerId: Int): List<ServiceItem> {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-products") {
            parameter("user_id", customerId)
        }
        return response.body()
    }

    override suspend fun assignProductToUser(userId: Int, productId: Int): Boolean {
        return try {
            val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/assign-product") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("user_id" to userId, "product_id" to productId))
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            println("❌ Error HTTP al contratar producto: ${e.message}")
            false
        }
    }

    override suspend fun unassignProductFromUser(userId: Int, productId: Int): Boolean {
        return try {
            val response = ApiClient.apiClient.delete("${ApiClient.baseUrl}/mobile/unassign-product") {
                parameter("user_id", userId)
                parameter("product_id", productId)
            }
            println("✅ DELETE status: ${response.status}")
            response.status.value in 200..299
        } catch (e: Exception) {
            println("❌ Error al desasignar producto: ${e.message}")
            false
        }
    }

    override suspend fun getProductDetails(userId: Int, productId: Int): ProductDetailResponse? {
        return try {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/product-details") {
                parameter("user_id", userId)
                parameter("product_id", productId)
            }
            val text = response.bodyAsText()

            println("📦 Respuesta del backend (detalles): $text") // ✅ Añade esta línea

            if (text.contains("error", ignoreCase = true)) {
                println("❌ Error backend: $text")
                null
            } else {
                Json.decodeFromString<ProductDetailResponse>(text)
            }
        } catch (e: Exception) {
            println("❌ Excepción en getProductDetails: ${e.message}")
            null
        }
    }
}