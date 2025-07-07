package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ServicioDispo
import com.humanperformcenter.shared.data.model.ServicioItembien
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

object ServiceProductRepositoryImpl: ServiceProductRepository {
    override suspend fun getAllServices(): List<ServicioDispo> {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/services")
        return response.body()
    }

    override suspend fun getServiceProducts(serviceId: Int): List<ServicioItembien> {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/service-products") {
            parameter("service_id", serviceId)
        }
        return response.body()
    }

    override suspend fun getUserProducts(customerId: Int): List<ServicioItembien> {
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
}