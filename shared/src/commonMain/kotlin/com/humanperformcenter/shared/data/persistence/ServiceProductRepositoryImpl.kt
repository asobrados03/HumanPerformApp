package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ServicioDispo
import com.humanperformcenter.shared.data.model.ServicioItembien
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

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
}