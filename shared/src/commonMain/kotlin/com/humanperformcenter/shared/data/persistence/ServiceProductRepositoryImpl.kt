package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.remote.ServiceProductRemoteDataSource
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository

class ServiceProductRepositoryImpl(
    private val remoteDataSource: ServiceProductRemoteDataSource,
) : ServiceProductRepository {
    override suspend fun getAllServices(): Result<List<ServiceAvailable>> = remoteDataSource.getAllServices()
    override suspend fun getServiceProducts(serviceId: Int, userId: Int): Result<List<Product>> =
        remoteDataSource.getServiceProducts(serviceId, userId)

    override suspend fun getUserProducts(userId: Int): Result<List<Product>> = remoteDataSource.getUserProducts(userId)

    override suspend fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String?,
    ): Result<Int> = remoteDataSource.assignProductToUser(userId, productId, paymentMethod, couponCode)

    override suspend fun unassignProductFromUser(userId: Int, productId: Int): Result<Unit> =
        remoteDataSource.unassignProductFromUser(userId, productId)

    override suspend fun getActiveProductDetail(userId: Int, productId: Int): Result<ProductDetailResponse> =
        remoteDataSource.getActiveProductDetail(userId, productId)

    override suspend fun getProductDetailHireProduct(productId: Int): Result<Product> =
        remoteDataSource.getProductDetailHireProduct(productId)
}
