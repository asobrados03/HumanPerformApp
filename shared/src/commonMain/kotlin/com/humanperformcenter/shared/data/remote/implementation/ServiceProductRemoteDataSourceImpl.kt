package com.humanperformcenter.shared.data.remote.implementation

import com.humanperformcenter.shared.data.model.payment.AssignProductResponse
import com.humanperformcenter.shared.data.model.product_service.AssignProductRequest
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.ServiceProductRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ServiceProductRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : ServiceProductRemoteDataSource {
    override suspend fun getAllServices(): Result<List<ServiceAvailable>> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/services").body()
    }

    override suspend fun getServiceProducts(serviceId: Int, userId: Int): Result<List<Product>> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/service-products") {
            parameter("primary_service_id", serviceId)
            parameter("user_id", userId)
        }.body()
    }

    override suspend fun getUserProducts(userId: Int): Result<List<Product>> = runCatching {
        clientProvider.apiClient.get(
            "${clientProvider.baseUrl}/mobile/users/$userId/products"
        ).body()
    }

    override suspend fun assignProductToUser(userId: Int, productId: Int, paymentMethod: String, couponCode: String?): Result<Int> = runCatching {
        val body = clientProvider.apiClient.post(
            "${clientProvider.baseUrl}/mobile/users/$userId/products"
        ) {
            contentType(ContentType.Application.Json)
            setBody(AssignProductRequest(productId, paymentMethod, couponCode))
        }.body<AssignProductResponse>()
        body.assignedId ?: 0
    }

    override suspend fun unassignProductFromUser(userId: Int, productId: Int): Result<Unit> = runCatching {
        clientProvider.apiClient.delete(
            "${clientProvider.baseUrl}/mobile/users/$userId/products/$productId"
        )
    }

    override suspend fun getActiveProductDetail(userId: Int, productId: Int): Result<ProductDetailResponse> = runCatching {
        clientProvider.apiClient.get(
            "${clientProvider.baseUrl}/mobile/active-product-detail"
        ) {
            parameter("user_id", userId)
            parameter("product_id", productId)
        }.body()
    }

    override suspend fun getProductDetailHireProduct(productId: Int): Result<Product> = runCatching {
        clientProvider.apiClient.get(
            "${clientProvider.baseUrl}/mobile/products/$productId"
        ).body()
    }
}
