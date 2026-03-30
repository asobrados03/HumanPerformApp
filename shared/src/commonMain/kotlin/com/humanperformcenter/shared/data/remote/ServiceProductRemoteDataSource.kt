package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable

interface ServiceProductRemoteDataSource {
    suspend fun getAllServices(): Result<List<ServiceAvailable>>
    suspend fun getServiceProducts(serviceId: Int, userId: Int): Result<List<Product>>
    suspend fun getUserProducts(userId: Int): Result<List<Product>>
    suspend fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String? = null,
    ): Result<Int>
    suspend fun unassignProductFromUser(userId: Int, productId: Int): Result<Unit>
    suspend fun getActiveProductDetail(userId: Int, productId: Int): Result<ProductDetailResponse>
    suspend fun getProductDetailHireProduct(productId: Int): Result<Product>
}
