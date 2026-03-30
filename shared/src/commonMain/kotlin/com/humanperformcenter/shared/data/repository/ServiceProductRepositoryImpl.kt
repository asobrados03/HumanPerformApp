package com.humanperformcenter.shared.data.repository

import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.remote.ServiceProductRemoteDataSource
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository

class ServiceProductRepositoryImpl(
    private val remote: ServiceProductRemoteDataSource,
) : ServiceProductRepository {
    override suspend fun getAllServices(): Result<List<ServiceAvailable>> = remote.getAllServices()
    override suspend fun getServiceProducts(serviceId: Int, userId: Int): Result<List<Product>> =
        remote.getServiceProducts(serviceId, userId)

    override suspend fun getUserProducts(userId: Int): Result<List<Product>> = remote.getUserProducts(userId)

    override suspend fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String?,
    ): Result<Int> = remote.assignProductToUser(userId, productId, paymentMethod, couponCode)

    override suspend fun unassignProductFromUser(userId: Int, productId: Int): Result<Unit> =
        remote.unassignProductFromUser(userId, productId)

    override suspend fun getActiveProductDetail(userId: Int, productId: Int): Result<ProductDetailResponse> =
        remote.getActiveProductDetail(userId, productId)

    override suspend fun getProductDetailHireProduct(productId: Int): Result<Product> =
        remote.getProductDetailHireProduct(productId)
}
